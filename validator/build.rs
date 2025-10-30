fn main() -> Result<(), Box<dyn std::error::Error>> {
    println!("cargo:rerun-if-changed=../proto/validator.proto");
    tonic_build::configure()
        .build_server(true)
        .compile(&["../proto/validator.proto"], &["../proto"])?;
    Ok(())
}
