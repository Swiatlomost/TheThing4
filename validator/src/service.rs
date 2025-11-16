pub mod generated {
    tonic::include_proto!("poi.validator");
}

use crate::play_integrity::PlayIntegrityClient;
use base64::engine::general_purpose::STANDARD as BASE64;
use base64::Engine;
use generated::batch_proof_response::Status;
use generated::po_i_validator_server::PoIValidator;
use generated::{
    AttestationPayload, BatchProofRequest, BatchProofResponse, BatchStatusRequest,
    BatchStatusResponse,
};
use once_cell::sync::Lazy;
use p256::ecdsa::signature::Verifier;
use p256::ecdsa::{Signature as EcdsaSignature, VerifyingKey};
use p256::pkcs8::DecodePublicKey;
use sha2::{Digest, Sha256};
use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use thiserror::Error;
use tonic::{Request, Response, Status as TonicStatus};
use uuid::Uuid;

#[derive(Clone, Default)]
pub struct ValidatorService {
    registry: Arc<Mutex<HashMap<String, BatchState>>>,
}

static PLAY_INTEGRITY_CLIENT: Lazy<PlayIntegrityClient> = Lazy::new(PlayIntegrityClient::new);

#[derive(Clone)]
struct BatchState {
    status: Status,
    reason: String,
    solana_tx: String,
}

impl ValidatorService {
    fn store_state(&self, batch_id: String, state: BatchState) {
        if let Ok(mut map) = self.registry.lock() {
            map.insert(batch_id, state);
        }
    }

    fn get_state(&self, batch_id: &str) -> Option<BatchState> {
        self.registry
            .lock()
            .ok()
            .and_then(|map| map.get(batch_id).cloned())
    }

    async fn validate_batch(batch: &BatchProofRequest) -> Result<(), ValidationError> {
        if batch.metrics.is_empty() {
            return Err(ValidationError::MetricsEmpty);
        }
        if batch.merkle_root.len() != 32 {
            return Err(ValidationError::MerkleRootLength {
                actual: batch.merkle_root.len(),
            });
        }

        let mut hashes = Vec::with_capacity(batch.metrics.len());
        for (idx, metric) in batch.metrics.iter().enumerate() {
            if metric.hash.len() != 32 {
                return Err(ValidationError::HashLengthInvalid {
                    index: idx,
                    actual: metric.hash.len(),
                });
            }
            hashes.push(metric.hash.clone());
        }

        let computed_root = compute_merkle_root(&hashes);
        if computed_root != batch.merkle_root {
            return Err(ValidationError::MerkleMismatch);
        }

        if batch.public_key.is_empty() {
            return Err(ValidationError::PublicKeyMissing);
        }
        if batch.signature.is_empty() {
            return Err(ValidationError::SignatureMissing);
        }

        verify_signature(&batch.public_key, &batch.signature, &batch.merkle_root)?;
        verify_attestation(batch.attestation.as_ref()).await?;

        Ok(())
    }
}

#[tonic::async_trait]
impl PoIValidator for ValidatorService {
    async fn submit_batch(
        &self,
        request: Request<BatchProofRequest>,
    ) -> Result<Response<BatchProofResponse>, TonicStatus> {
        let batch = request.into_inner();
        let merkle_b64 = BASE64.encode(&batch.merkle_root);
        tracing::info!(
            "Received batch: device_id={}, entries={}, merkle_root={}",
            batch.device_id,
            batch.metrics.len(),
            merkle_b64
        );

        let validation_result = Self::validate_batch(&batch).await;
        let (status, reason) = match validation_result {
            Ok(_) => {
                tracing::info!(
                    "Batch validated: device_id={}, merkle_root={}",
                    batch.device_id,
                    merkle_b64
                );
                (Status::Accepted, String::from("validated"))
            }
            Err(err) => {
                tracing::warn!(
                    "Batch rejected: device_id={}, merkle_root={}, reason={}",
                    batch.device_id,
                    merkle_b64,
                    err
                );
                (Status::Rejected, err.to_string())
            }
        };

        let batch_id = Uuid::new_v4().to_string();
        self.store_state(
            batch_id.clone(),
            BatchState {
                status,
                reason: reason.clone(),
                solana_tx: String::new(),
            },
        );

        let response = BatchProofResponse {
            batch_id,
            status: status.into(),
            reason,
            expected_slot: 0,
        };
        Ok(Response::new(response))
    }

    async fn query_status(
        &self,
        request: Request<BatchStatusRequest>,
    ) -> Result<Response<BatchStatusResponse>, TonicStatus> {
        let batch_id = request.into_inner().batch_id;
        tracing::info!("Query status for batch {}", batch_id);

        let state = self.get_state(&batch_id).unwrap_or(BatchState {
            status: Status::Rejected,
            reason: String::from("batch_not_found"),
            solana_tx: String::new(),
        });

        let response = BatchStatusResponse {
            batch_id,
            status: state.status.into(),
            reason: state.reason,
            solana_tx: state.solana_tx,
        };
        Ok(Response::new(response))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    fn decode_base64(data: &str) -> Vec<u8> {
        BASE64.decode(data).expect("valid base64")
    }

    fn sample_request() -> BatchProofRequest {
        let merkle_root = decode_base64("JH8RuLauG/T1AqholC1AMkKJKFqVBD5iRGDvGd3i/Zg=");
        let signature = decode_base64(
            "MEUCIDF8XnnxaKOVc+BNokofqz8lGd8FOFOsayISacj5cB1dAiEA2/I0LL4m04FA3juWNLdayYLJGwjEVDwhmTsw/0yLpN4=",
        );
        let public_key = decode_base64(
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEjgSymnlgYBmuG82v3kDo1wj+hMNnyskoGGzwUyI4pMbVfSU1akEMOxyTznOQfI+ZDI54E0i/PB/IF0ctaxl8/w==",
        );
        BatchProofRequest {
            device_id: "C0S-XIAOMI-13TPRO".into(),
            batch_start: 1_761_924_794_000,
            batch_end: 1_761_924_795_000,
            merkle_root,
            metrics: vec![generated::BatchEntryMetrics {
                ledger_index: 0,
                entropy: 0.82,
                trust_score: 92.0,
                far: 0.0,
                frr: 0.0,
                hash: decode_base64("JH8RuLauG/T1AqholC1AMkKJKFqVBD5iRGDvGd3i/Zg="),
                timestamp_ms: 1_761_924_794_783,
            }],
            signature,
            attestation: Some(AttestationPayload {
                provider: "play_integrity".into(),
                token: b"dummy-attestation-token".to_vec(),
                nonce: "sample-nonce-20251031".into(),
            }),
            public_key,
        }
    }

    #[tokio::test]
    async fn accept_valid_batch() {
        let service = ValidatorService::default();
        let request = sample_request();
        let response = service
            .submit_batch(Request::new(request))
            .await
            .unwrap()
            .into_inner();
        assert_eq!(response.status, Status::Accepted as i32);
        assert_eq!(response.reason, "validated");
    }

    #[tokio::test]
    async fn reject_empty_metrics() {
        let service = ValidatorService::default();
        let request = BatchProofRequest {
            device_id: "test-device".into(),
            batch_start: 0,
            batch_end: 1,
            merkle_root: vec![],
            metrics: vec![],
            signature: vec![],
            attestation: Some(AttestationPayload {
                provider: "play_integrity".into(),
                token: vec![1, 2, 3],
                nonce: "abc".into(),
            }),
            public_key: vec![],
        };
        let response = service
            .submit_batch(Request::new(request))
            .await
            .unwrap()
            .into_inner();
        assert_eq!(response.status, Status::Rejected as i32);
        assert_eq!(response.reason, "metrics_empty");
    }

    #[tokio::test]
    async fn reject_merkle_mismatch() {
        let service = ValidatorService::default();
        let mut request = sample_request();
        request.merkle_root[0] ^= 0xFF;
        let response = service
            .submit_batch(Request::new(request))
            .await
            .unwrap()
            .into_inner();
        assert_eq!(response.status, Status::Rejected as i32);
        assert_eq!(response.reason, "merkle_mismatch");
    }

    #[tokio::test]
    async fn reject_invalid_signature() {
        let service = ValidatorService::default();
        let mut request = sample_request();
        request.signature[3] ^= 0x01;
        let response = service
            .submit_batch(Request::new(request))
            .await
            .unwrap()
            .into_inner();
        assert_eq!(response.status, Status::Rejected as i32);
        assert!(response.reason.starts_with("signature_invalid"));
    }

    #[tokio::test]
    async fn reject_missing_attestation() {
        let service = ValidatorService::default();
        let mut request = sample_request();
        request.attestation = None;
        let response = service
            .submit_batch(Request::new(request))
            .await
            .unwrap()
            .into_inner();
        assert_eq!(response.status, Status::Rejected as i32);
        assert_eq!(response.reason, "attestation_missing");
    }
}

fn compute_merkle_root(hashes: &[Vec<u8>]) -> Vec<u8> {
    let mut level: Vec<Vec<u8>> = hashes.to_vec();
    while level.len() > 1 {
        let mut next = Vec::with_capacity((level.len() + 1) / 2);
        for pair in level.chunks(2) {
            let left = &pair[0];
            let right = if pair.len() == 2 { &pair[1] } else { &pair[0] };
            let mut hasher = Sha256::new();
            hasher.update(left);
            hasher.update(right);
            next.push(hasher.finalize().to_vec());
        }
        level = next;
    }
    level
        .into_iter()
        .next()
        .expect("at least one hash to compute merkle root")
}

fn verify_signature(
    public_key_der: &[u8],
    signature_der: &[u8],
    message: &[u8],
) -> Result<(), ValidationError> {
    let verifying_key = VerifyingKey::from_public_key_der(public_key_der)
        .map_err(|_| ValidationError::SignatureInvalid("public_key"))?;
    let signature = EcdsaSignature::from_der(signature_der)
        .map_err(|_| ValidationError::SignatureInvalid("signature_format"))?;
    verifying_key
        .verify(message, &signature)
        .map_err(|_| ValidationError::SignatureInvalid("verification_failed"))
}

async fn verify_attestation(payload: Option<&AttestationPayload>) -> Result<(), ValidationError> {
    let attestation = payload.ok_or(ValidationError::AttestationMissing)?;
    if attestation.provider != "play_integrity" {
        return Err(ValidationError::AttestationInvalid(
            "unsupported_provider".into(),
        ));
    }
    if attestation.token.is_empty() {
        return Err(ValidationError::AttestationInvalid("token_empty".into()));
    }
    if attestation.nonce.is_empty() {
        return Err(ValidationError::AttestationInvalid("nonce_empty".into()));
    }
    if attestation.nonce.len() > 128 {
        return Err(ValidationError::AttestationInvalid("nonce_too_long".into()));
    }
    let token = std::str::from_utf8(&attestation.token)
        .map_err(|_| ValidationError::AttestationInvalid("token_utf8".into()))?;
    PLAY_INTEGRITY_CLIENT
        .verify(token, &attestation.nonce)
        .await
        .map_err(ValidationError::AttestationInvalid)?;
    Ok(())
}

#[derive(Debug, Error)]
enum ValidationError {
    #[error("metrics_empty")]
    MetricsEmpty,
    #[error("merkle_root_length_invalid actual={actual}")]
    MerkleRootLength { actual: usize },
    #[error("hash_len_invalid idx={index} actual={actual}")]
    HashLengthInvalid { index: usize, actual: usize },
    #[error("merkle_mismatch")]
    MerkleMismatch,
    #[error("public_key_missing")]
    PublicKeyMissing,
    #[error("signature_missing")]
    SignatureMissing,
    #[error("signature_invalid_{0}")]
    SignatureInvalid(&'static str),
    #[error("attestation_missing")]
    AttestationMissing,
    #[error("attestation_invalid_{0}")]
    AttestationInvalid(String),
}
