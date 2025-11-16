# syntax=docker/dockerfile:1

ARG RUST_VERSION=nightly
FROM rustlang/rust:${RUST_VERSION} AS builder
WORKDIR /workspace

RUN apt-get update && apt-get install -y --no-install-recommends protobuf-compiler && rm -rf /var/lib/apt/lists/*

COPY proto ./proto
COPY validator/Cargo.toml validator/Cargo.lock ./validator/
COPY validator/build.rs ./validator/
COPY validator/src ./validator/src

WORKDIR /workspace/validator
RUN cargo build --release

FROM debian:12-slim AS runtime
RUN apt-get update && apt-get install -y --no-install-recommends ca-certificates && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=builder /workspace/validator/target/release/poi-validator /usr/local/bin/validator

EXPOSE 50051
ENV RUST_LOG=info

CMD ["validator"]
