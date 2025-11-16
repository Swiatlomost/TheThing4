use gcp_auth::AuthenticationManager;
use reqwest::Client;
use serde::Deserialize;
use std::sync::atomic::{AtomicBool, Ordering};

static WARNED_MISSING_CREDS: AtomicBool = AtomicBool::new(false);
const PLAY_SCOPE: &str = "https://www.googleapis.com/auth/playintegrity";

pub struct PlayIntegrityClient {
    http: Client,
    config: Option<PlayIntegrityConfig>,
}

#[derive(Clone)]
struct PlayIntegrityConfig {
    api_key: Option<String>,
    package_name: String,
    use_oauth: bool,
}

impl PlayIntegrityClient {
    pub fn new() -> Self {
        let http = Client::builder()
            .build()
            .expect("failed to build reqwest client");
        let api_key = std::env::var("PLAY_INTEGRITY_API_KEY").ok();
        let use_oauth = std::env::var("PLAY_INTEGRITY_SERVICE_ACCOUNT_JSON")
            .ok()
            .map(|path| {
                std::env::set_var("GOOGLE_APPLICATION_CREDENTIALS", path);
                true
            })
            .unwrap_or(false);
        if api_key.is_none() && !use_oauth && !WARNED_MISSING_CREDS.swap(true, Ordering::SeqCst) {
            tracing::warn!("Play Integrity credentials missing: set PLAY_INTEGRITY_SERVICE_ACCOUNT_JSON or PLAY_INTEGRITY_API_KEY");
        }
        let package_name = std::env::var("PLAY_INTEGRITY_PACKAGE_NAME")
            .unwrap_or_else(|_| "com.thething.cos".into());
        Self {
            http,
            config: if api_key.is_none() && !use_oauth {
                None
            } else {
                Some(PlayIntegrityConfig {
                    api_key,
                    package_name,
                    use_oauth,
                })
            },
        }
    }

    pub async fn verify(&self, token: &str, expected_nonce: &str) -> Result<(), String> {
        let Some(config) = &self.config else {
            return Ok(());
        };
        let url = format!(
            "https://playintegrity.googleapis.com/v1/{}:decodeIntegrityToken",
            config.package_name
        );
        let mut request = self
            .http
            .post(url)
            .json(&serde_json::json!({ "integrity_token": token }));
        if config.use_oauth {
            let manager = AuthenticationManager::new()
                .await
                .map_err(|err| format!("decode_oauth_init_error:{}", err))?;
            let bearer = manager
                .get_token(&[PLAY_SCOPE])
                .await
                .map_err(|err| format!("decode_oauth_error:{}", err))?;
            request = request.bearer_auth(bearer.as_str());
        } else if let Some(api_key) = &config.api_key {
            request = request.query(&[("key", api_key)]);
        } else {
            return Err("play_integrity_credentials_missing".into());
        }
        let response = request
            .send()
            .await
            .map_err(|err| format!("decode_http_error:{}", err))?;
        if !response.status().is_success() {
            let status = response.status();
            let body = response.text().await.unwrap_or_default();
            tracing::warn!(
                "PlayIntegrity decode failed: status={} body={}",
                status,
                body
            );
            return Err(format!("decode_http_status:{} body={}", status, body));
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
