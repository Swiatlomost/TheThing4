mod service;

use std::net::SocketAddr;
use tonic::transport::Server;
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
    Server::builder()
        .add_service(PoIValidatorServer::new(service))
        .serve(addr)
        .await?;

    Ok(())
}
