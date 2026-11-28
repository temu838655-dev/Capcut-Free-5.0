# Capcut Free 5.0 — Android

An independent Android video subtitle and dubbing workspace inspired by the workflow of [CapCap](https://github.com/notepower2k1/CapCap). This project is not affiliated with CapCut, ByteDance, or the CapCap project.

> **Status: development preview.** This repository is not yet a verified production release. Build, lint, unit tests, and real-device export checks must pass before publishing a production APK.

## Current Android capabilities

- Local video playback and subtitle preview.
- Import and export SRT, WebVTT, and ASS subtitle files.
- Basic subtitle timeline: add, edit, delete, move, and resize cues.
- Video subtitle OCR and still-image OCR using ML Kit.
- Online speech transcription using the OpenAI Whisper API.
- AI subtitle translation through OpenAI or Gemini, with:
  - multiple API keys pasted one per line (commas and semicolons are also accepted);
  - sequential key rotation when a key is rate-limited, unavailable, or rejected;
  - configurable cross-provider fallback;
  - 12-cue batches and a rolling three-cue source/translation context;
  - partial-result preservation so completed translations remain and untouched source cues are not discarded when a later batch fails.
- On-device ML Kit translation after its language model is downloaded.
- OpenAI TTS and Android-installed offline Vietnamese TTS voices.
- WAV voice rendering, audio extraction, audio replacement/mixing, and MP4 export using Android media APIs.
- Local named projects and ZIP project bundles; named projects record a schema version, save time, subtitle count, video URI, and subtitle data. Restore validates subtitle content and handles expired video-file permissions without discarding the subtitles.

## Current limitations

- Whisper transcription is online; a complete bundled offline ASR pipeline is not yet implemented.
- The timeline is currently subtitle-focused, not a complete multi-track NLE.
- Dedicated source separation (Vocals/BGM), per-character voice assignment, and a full offline neural TTS engine are not yet implemented.
- AI translation and cloud TTS send the selected text/audio to the configured provider. If a fallback provider is configured, subtitle text may also be sent to that provider.
- Android TTS offline voice availability depends on the speech engines and Vietnamese voice data installed on the device.
- Named projects currently use Android SharedPreferences and are capped at 1.5 MB of UTF-8 subtitle text per project; a dedicated database/file-backed project library with autosave history is still future work.
- A successful GitHub commit is not proof of a working APK. Production release requires CI and device verification.

## Build and verification

The repository includes an Android GitHub Actions workflow that runs:

```bash
gradle :app:assembleDebug :app:lintDebug :app:testDebugUnitTest
```

It then runs a basic Android Emulator smoke test. A development APK is uploaded as a workflow artifact. A preview release can be requested through the workflow's `publish_preview` input after the build and smoke jobs pass.

## API key handling

- API keys are entered at the time of the operation and held in memory for the translation request.
- The translation helper does not persist or log API keys.
- To rotate keys, paste one key per line in the primary provider field. You may also separate keys with commas or semicolons.
- An optional fallback field accepts multiple keys for the other provider. Configure a fallback only if you consent to sending subtitle text to that provider.
- Never commit API keys, signing files, passwords, or keystore data to this repository.

## Project comparison

See [CapCap feature gap analysis and Android roadmap](docs/capcap-gap-analysis.md).

## License and attribution

Review the repository's license and all third-party dependency licenses before redistribution. This application is an independent project and must not imply endorsement by CapCut or CapCap.
