use reqwest::Client;
use serde::Deserialize;
use std::sync::atomic::{AtomicBool, Ordering};

static WARNED_MISSING_KEY: AtomicBool = AtomicBool::new(false);

pub struct PlayIntegrityClient {
    http: Client,
    config: Option<PlayIntegrityConfig>,
}

#[derive(Clone)]
struct PlayIntegrityConfig {
    api_key: String,
    package_name: String,
}

impl PlayIntegrityClient {
    pub fn new() -> Self {
        let http = Client::builder()
            .build()
            .expect("failed to build reqwest client");
        let api_key = std::env::var("PLAY_INTEGRITY_API_KEY").ok();
        if api_key.is_none() && !WARNED_MISSING_KEY.swap(true, Ordering::SeqCst) {
            tracing::warn!("PLAY_INTEGRITY_API_KEY not set; attestation verification is disabled");
        }
        let package_name = std::env::var("PLAY_INTEGRITY_PACKAGE_NAME")
            .unwrap_or_else(|_| "com.thething.cos".into());
        Self {
            http,
            config: api_key.map(|key| PlayIntegrityConfig {
                api_key: key,
                package_name,
            }),
        }
    }

    pub async fn verify(&self, token: &str, expected_nonce: &str) -> Result<(), String> {
        let Some(config) = &self.config else {
            return Ok(());
        };
        let url = format!(
            "https://playintegrity.googleapis.com/v1/{}:decodeIntegrityToken?key={}",
            config.package_name, config.api_key
        );
        let response = self
            .http
            .post(url)
            .json(&serde_json::json!({ "integrity_token": token }))
            .send()
            .await
            .map_err(|err| format!("decode_http_error:{}", err))?;
        if !response.status().is_success() {
            return Err(format!("decode_http_status:{}", response.status()));
        }
        let payload: DecodeResponse = response
            .json()
            .await
            .map_err(|err| format!("decode_parse_error:{}", err))?;
        let external = payload
            .token_payload_external
            .ok_or_else(|| "payload_missing".to_string())?;
        let request = external
            .request_details
            .ok_or_else(|| "request_details_missing".to_string())?;
        let nonce = request.nonce.ok_or_else(|| "nonce_missing".to_string())?;
        if nonce != expected_nonce {
            return Err("nonce_mismatch".into());
        }
        let app = external
            .app_integrity
            .ok_or_else(|| "app_integrity_missing".to_string())?;
        if app.package_name.as_deref() != Some(&config.package_name) {
            return Err("package_mismatch".into());
        }
        if app.app_recognition_verdict.as_deref() != Some("PLAY_RECOGNIZED") {
            return Err("app_not_recognized".into());
        }
        let device = external
            .device_integrity
            .ok_or_else(|| "device_integrity_missing".to_string())?;
        let verdicts = device.device_recognition_verdict.unwrap_or_default();
        if !verdicts
            .iter()
            .any(|v| v == "MEETS_DEVICE_INTEGRITY" || v == "MEETS_STRONG_INTEGRITY")
        {
            return Err("device_not_trusted".into());
        }
        Ok(())
    }
}

#[derive(Debug, Deserialize)]
struct DecodeResponse {
    #[serde(rename = "tokenPayloadExternal")]
    token_payload_external: Option<TokenPayloadExternal>,
}

#[derive(Debug, Deserialize)]
struct TokenPayloadExternal {
    #[serde(rename = "requestDetails")]
    request_details: Option<RequestDetails>,
    #[serde(rename = "appIntegrity")]
    app_integrity: Option<AppIntegrity>,
    #[serde(rename = "deviceIntegrity")]
    device_integrity: Option<DeviceIntegrity>,
}

#[derive(Debug, Deserialize)]
struct RequestDetails {
    nonce: Option<String>,
}

#[derive(Debug, Deserialize)]
struct AppIntegrity {
    #[serde(rename = "packageName")]
    package_name: Option<String>,
    #[serde(rename = "appRecognitionVerdict")]
    app_recognition_verdict: Option<String>,
}

#[derive(Debug, Deserialize)]
struct DeviceIntegrity {
    #[serde(rename = "deviceRecognitionVerdict")]
    device_recognition_verdict: Option<Vec<String>>,
}
