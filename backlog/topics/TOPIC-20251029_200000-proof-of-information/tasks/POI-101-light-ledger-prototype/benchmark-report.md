# Light Ledger Benchmark Summary (2025-10-31)

| Device | API | Batch Size | Native avg (ms) | Fallback avg (ms) | Δ native - fallback |
| --- | --- | --- | --- | --- | --- |
| Pixel 5 Emulator | 34 | 1 000 | 211.35 | 233.67 | -22.32 |
| | | 5 000 | 624.90 | 948.74 | -323.84 |
| | | 10 000 | 877.67 | 1 011.73 | -134.06 |
| Xiaomi 13T Pro | 35 | 1 000 | 9.79 | 7.83 | **+1.96** |
| (no warm-up) | | 5 000 | 31.52 | 36.17 | -4.65 |
| | | 10 000 | 36.89 | 59.18 | -22.29 |
| Xiaomi 13T Pro | 35 | 1 000 | 13.43 | 11.93 | **+1.50** |
| warm-up (1 round) | | 5 000 | 37.74 | 40.46 | -2.72 |
| | | 10 000 | 57.05 | 66.79 | -9.74 |

## Observations

- Native hashing is consistently faster than the Kotlin fallback for medium and large batches (≥5 000 items) on both devices.
- For small batches (1 000 items) the Xiaomi 13T Pro shows a ~20% slowdown on the native path versus fallback (likely JNI warm-up / CPU frequency scaling).
- JNI smoke check (`LightLedgerRuntimeSmoke`) successfully loads the native library on both devices.

## Native Usage Policy (Implemented)

- Minimalny batch dla ścieżki JNI: **2 000 odcisków** (mniejsze partie korzystają z fallbacku Kotlin).
- Warm-up: **1 runda** przed pierwszym batch’em, aby zminimalizować narzut rozgrzewki.

## Recommendations

1. **Warm-up / batching policy:** Polityka 2 000 + warm-up wdrożona; monitorować czy potrzebne są dodatkowe rundy w produkcji.
2. **Monitoring:** Extend Kai's QA alerts so a native average per hash larger niż fallback triggers a regression warning (already logged in POI-QA-001 evidence).
3. **Additional hardware:** Capture the same benchmark on a second physical device (e.g., Pixel 7 or Galaxy S23) to confirm the small-batch behaviour is not device-specific.
4. **Future optimisation:** Profile the native hashing path (NDK trace) to check for per-call overhead (e.g., repeated SHA context allocations) that might be optimised for small batches.

## Raw Logcat Snippets

```
Pixel 5 Emulator (API 34)
10-31 10:12:56.425 I LightLedgerBenchmark: Benchmark on sdk_gphone64_x86_64 (API 34)
10-31 10:12:56.427 I LightLedgerBenchmark: Batch 1000: fallback avg=233.67 ms (0.23 ms/hash)
10-31 10:12:56.428 I LightLedgerBenchmark: Batch 1000: native  avg=211.35 ms (0.21 ms/hash)
10-31 10:12:56.428 I LightLedgerBenchmark: Batch 5000: fallback avg=948.74 ms (0.19 ms/hash)
10-31 10:12:56.429 I LightLedgerBenchmark: Batch 5000: native  avg=624.90 ms (0.12 ms/hash)
10-31 10:12:56.429 I LightLedgerBenchmark: Batch 10000: fallback avg=1011.73 ms (0.10 ms/hash)
10-31 10:12:56.430 I LightLedgerBenchmark: Batch 10000: native  avg=877.67 ms (0.09 ms/hash)

Xiaomi 13T Pro (API 35) – bez warm-up
10-31 14:27:03.505 I LightLedgerBenchmark: Benchmark on 23078PND5G (API 35)
10-31 14:27:03.506 I LightLedgerBenchmark: Batch 1000: fallback avg=7.83 ms (0.01 ms/hash)
10-31 14:27:03.506 I LightLedgerBenchmark: Batch 1000: native  avg=9.79 ms (0.01 ms/hash)
10-31 14:27:03.506 I LightLedgerBenchmark: Batch 5000: fallback avg=36.17 ms (0.01 ms/hash)
10-31 14:27:03.506 I LightLedgerBenchmark: Batch 5000: native  avg=31.52 ms (0.01 ms/hash)
10-31 14:27:03.506 I LightLedgerBenchmark: Batch 10000: fallback avg=59.18 ms (0.01 ms/hash)
10-31 14:27:03.506 I LightLedgerBenchmark: Batch 10000: native  avg=36.89 ms (0.00 ms/hash)

Xiaomi 13T Pro (API 35) – warm-up (1 round)
10-31 14:36:11.989 I LightLedgerBenchmark: Benchmark on 23078PND5G (API 35)
10-31 14:36:11.989 I LightLedgerBenchmark: Batch 1000: fallback avg=11.93 ms (0.01 ms/hash)
10-31 14:36:11.989 I LightLedgerBenchmark: Batch 1000: native  avg=13.43 ms (0.01 ms/hash)
10-31 14:36:11.990 I LightLedgerBenchmark: Batch 5000: fallback avg=40.46 ms (0.01 ms/hash)
10-31 14:36:11.990 I LightLedgerBenchmark: Batch 5000: native  avg=37.74 ms (0.01 ms/hash)
10-31 14:36:11.990 I LightLedgerBenchmark: Batch 10000: fallback avg=66.79 ms (0.01 ms/hash)
10-31 14:36:11.990 I LightLedgerBenchmark: Batch 10000: native  avg=57.05 ms (0.01 ms/hash)
```

## Next Steps

- Record follow-up benchmarks after adding a native warm-up sequence to validate the hypothesis.
- Schedule second-device data collection (owner TBD) and aggregate results in this report.
- Synchronise with Kai to define regression thresholds for native hashing in CI / QA dashboards.
