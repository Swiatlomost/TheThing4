pub mod generated {
    tonic::include_proto!("poi.validator");
}

use generated::batch_proof_response::Status;
use generated::po_i_validator_server::PoIValidator;
use generated::{AttestationPayload, BatchProofRequest, BatchProofResponse, BatchStatusRequest, BatchStatusResponse};
use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use tonic::{Request, Response, Status as TonicStatus};
use uuid::Uuid;

#[derive(Clone, Default)]
pub struct ValidatorService {
    registry: Arc<Mutex<HashMap<String, BatchState>>>,
}

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
}

#[tonic::async_trait]
impl PoIValidator for ValidatorService {
    async fn submit_batch(
        &self,
        request: Request<BatchProofRequest>,
    ) -> Result<Response<BatchProofResponse>, TonicStatus> {
        let batch = request.into_inner();
        tracing::info!(
            "Received batch: device_id={}, entries={}",
            batch.device_id,
            batch.metrics.len()
        );

        let metrics = batch.metrics;
        let attestation = batch.attestation.unwrap_or_default();

        let mut status = Status::Queued;
        let mut reason = String::from("queued");

        if metrics.is_empty() {
            status = Status::Rejected;
            reason = String::from("metrics_empty");
        } else if attestation.token.is_empty() {
            status = Status::Rejected;
            reason = String::from("missing_attestation");
        }

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
            attestation: Some(generated::AttestationPayload {
                provider: "play_integrity".into(),
                token: vec![1, 2, 3],
                nonce: "abc".into(),
            }),
        };
        let response = service.submit_batch(Request::new(request)).await.unwrap().into_inner();
        assert_eq!(response.status, Status::Rejected.into());
        assert_eq!(response.reason, "metrics_empty");
    }
}
