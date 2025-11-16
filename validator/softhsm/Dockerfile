FROM debian:12-slim

RUN apt-get update \
    && apt-get install -y --no-install-recommends softhsm2 ca-certificates \
    && rm -rf /var/lib/apt/lists/*

ENV SOFTHSM2_CONF=/etc/softhsm2/softhsm2.conf
VOLUME ["/var/lib/softhsm"]

# Keep container running so validator can connect via PKCS#11
CMD ["sleep", "infinity"]
