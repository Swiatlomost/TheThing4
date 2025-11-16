mod play_integrity;
mod service;

use std::{env, fs, net::SocketAddr};
use tonic::transport::{Identity, Server, ServerTlsConfig};
use tracing_subscriber::{EnvFilter, FmtSubscriber};

use service::generated::po_i_validator_server::PoIValidatorServer;
use service::ValidatorService;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let subscriber = FmtSubscriber::builder()
        .with_env_filter(EnvFilter::from_default_env().add_directive("info".parse()?))
        .finish();
    tracing::subscriber::set_global_default(subscriber)?;

    let addr: SocketAddr = "0.0.0.0:50051".parse()?;
    tracing::info!("Starting PoI validator on {}", addr);

    let service = ValidatorService::default();
    let mut builder = match tls_config_from_env()? {
        Some(tls) => {
            tracing::info!("TLS enabled for validator gRPC endpoint");
            Server::builder().tls_config(tls)?
        }
        None => {
            tracing::warn!("VALIDATOR_TLS_CERT/KEY not set; running without TLS");
            Server::builder()
        }
    };
    builder
        .add_service(PoIValidatorServer::new(service))
        .serve(addr)
        .await?;

    Ok(())
}

fn tls_config_from_env() -> Result<Option<ServerTlsConfig>, Box<dyn std::error::Error>> {
    let cert_path = match env::var("VALIDATOR_TLS_CERT") {
        Ok(value) if !value.trim().is_empty() => value,
        _ => return Ok(None),
    };
    let key_path = match env::var("VALIDATOR_TLS_KEY") {
        Ok(value) if !value.trim().is_empty() => value,
        _ => {
            return Err("VALIDATOR_TLS_CERT set but VALIDATOR_TLS_KEY missing".into());
        }
    };
    let cert = fs::read(&cert_path)
        .map_err(|err| format!("failed to read TLS cert {}: {}", cert_path, err))?;
    let key = fs::read(&key_path)
        .map_err(|err| format!("failed to read TLS key {}: {}", key_path, err))?;
    let identity = Identity::from_pem(cert, key);
    Ok(Some(ServerTlsConfig::new().identity(identity)))
}
