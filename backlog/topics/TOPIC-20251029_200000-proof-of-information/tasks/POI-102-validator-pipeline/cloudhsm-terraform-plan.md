## CloudHSM Terraform Plan

### Resources
- `aws_cloudhsm_v2_cluster` (multi-AZ).
- `aws_cloudhsm_v2_hsm` instances (min 2 for HA).
- `aws_cloudhsm_v2_cluster_certificate` export for client install.
- `aws_iam_role` + `aws_iam_policy` for CloudHSM management.
- `aws_security_group` allowing validator subnets.

### Workflow
1. Terraform apply staging cluster (single AZ) to validate network + client.
2. Integrate with Rust validator via `cloudhsm-client` docker image.
3. Promote to production multi-AZ cluster after load testing.

### Backlog Items
- [ ] Create Terraform module repository (`infra/cloudhsm`).
- [ ] Configure GitHub Actions plan/apply with manual approval.
- [ ] Document HSM client install for validator pods.
