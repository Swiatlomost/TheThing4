## HSM Options – Evaluation

| Provider | Pros | Cons | Est. Monthly Cost (staging/prod) | Notes |
|----------|------|------|----------------------------------|-------|
| AWS CloudHSM (cm-small) | Dedicated hardware, FIPS 140-2 Level 3, low latency with Rust PKCS#11 | Requires VPC tenancy, higher upfront setup | ~$1,500 / cluster | Preferred for production |
| GCP CloudKMS + EKM | Managed service, easy integration, pay-per-use | Higher signing latency (~20 ms), shared tenancy | ~$400 (usage-based) | Good fallback / staging |
| Azure Managed HSM | Native compliance tooling, integrates with Terraform | Limited Rust SDK examples, regional availability | ~$1,000 | Consider if multi-cloud needed |

### Recommendation
- Provision **AWS CloudHSM** for production validator cluster (multi-AZ), initiate quote via Orin procurement.
- Use **GCP CloudKMS** for staging/PoC to control cost while API stabilises.

### Action Items
1. Request capacity confirmation from AWS (ticket opened 2025-10-29).  
2. Draft Terraform module for CloudHSM + networking (Nodus).  
3. Prototype PKCS#11 signing in Rust; measure latency vs. CloudKMS fallback.
