# Vietnamese neural TTS

Current shipping path: Android offline installed Vietnamese voices via OfflineVoice and AiVoice.SpeechSource. No paid voice API is required for this path.

Candidate upstream: https://github.com/pnnbao97/VieNeu-TTS (v3 Turbo ONNX and experimental v3 Nano ARM). Do not assume Python SDK runs on Android. Verify exact model revision, weight license, tokenizer/runtime dependencies, redistribution permissions and ARM64 memory usage before bundling. Hosted v4 is not the free offline candidate.

Integration contract: implement AiVoice.SpeechSource with mono signed little-endian PCM at 24000 Hz; preserve timing and honor cancellation. Keep model downloads optional, SHA-256 verified and atomically installed. No silent network fallback.

Acceptance: real ARM64 device in airplane mode; native listeners from north, central and south assess regional accuracy, intelligibility, naturalness and consistency on questions, dialogue, numbers and names. Test WAV and MP4 playback, long jobs, memory, cancellation and cleanup. Do not claim 90–99% human likeness without a defined listening study. Existing Android voices do not guarantee all six region/gender combinations.

Status: research candidate only; neural model runtime not yet integrated.

## Upstream verification

Model card: https://huggingface.co/pnnbao-ump/VieNeu-TTS-v3-Nano/blob/main/README.md — Apache-2.0, SDK 3.5.4 documents 11 presets. Older config still lists six; do not hardcode regional labels without listening tests. Full repository approximately 420 MB including optional cloning assets. Pin exact revision and verify graph dependencies before download. Nano 24 kHz matches SpeechSource output; Turbo 48 kHz requires resampling. Android native inference remains unimplemented; Python desktop benchmark is not Android evidence.
