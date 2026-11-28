# Đối chiếu tính năng Capcut-Free-5.0 và CapCap

Ngày rà soát: 2026-11-28  
Nhánh rà soát: `main`  
Dự án tham chiếu: https://github.com/notepower2k1/CapCap

## Phạm vi và nguyên tắc

Capcut-Free-5.0 là ứng dụng Android độc lập. CapCap là ứng dụng desktop Windows dùng Python/PySide6. Chỉ đối chiếu năng lực và quy trình sản phẩm; không sao chép mã nguồn, tài nguyên, giao diện độc quyền hoặc giả định rằng thư viện desktop có thể chạy nguyên trạng trên Android.

Trạng thái trong tài liệu này dựa trên mã nguồn đã đọc. “Có trong giao diện” không đồng nghĩa đã được kiểm thử đầy đủ trên thiết bị thật.

## Hiện trạng đã xác minh trong Capcut-Free-5.0

- Phát video bằng Android VideoView/MediaController.
- Nhập và chỉnh sửa phụ đề; bộ phân tích SRT, WebVTT và một phần ASS.
- Nhập/xuất SRT, VTT, ASS; xem phụ đề đồng bộ theo vị trí phát.
- Timeline phụ đề cơ bản: thêm, sửa, xóa, kéo thời gian bắt đầu/kết thúc.
- Nhận dạng phụ đề cứng bằng OCR video qua VideoOcr; OCR ảnh bằng ML Kit Latin.
- Nhận dạng lời thoại qua OpenAI Whisper API; tệp âm thanh giới hạn 24 MB trong lớp Whisper.
- Dịch AI qua OpenAI/Gemini, chia lô 12 câu, giữ ngữ cảnh 3 cặp nguồn/bản dịch gần nhất; hỗ trợ nhiều khóa theo thứ tự, xoay vòng theo batch, fallback khác nhà cung cấp và giữ nguyên các câu chưa dịch khi batch thất bại.
- Dịch máy offline/thiết bị qua ML Kit sau khi tải mô hình.
- TTS OpenAI; TTS offline dựa trên giọng tiếng Việt đã cài trong Android TextToSpeech.
- Xuất WAV, tách âm thanh, thay/trộn track âm thanh vào MP4; có tùy chọn ghi phụ đề, độ phân giải, màu/cỡ/vị trí chữ, nền phụ đề và ducking.
- Xử lý tách âm thanh hàng loạt tối đa 30 video.
- Lưu dự án cục bộ bằng tên và xuất gói ZIP.
- Có ModelStore xác minh SHA-256 và dependency ONNX Runtime, nhưng chưa xác minh được một pipeline ASR/OCR/TTS neural offline hoàn chỉnh đang sử dụng các mô hình ONNX này.

## So sánh năng lực

| Năng lực | CapCap | Capcut-Free-5.0 hiện tại | Khoảng cách |
|---|---|---|---|
| Nhận diện giọng nói | Faster-Whisper, SenseVoice, CPU/GPU | Whisper API | Cần ASR offline thực sự |
| OCR phụ đề | RapidOCR/ONNX | ML Kit, VideoOcr | Cần kiểm tra độ chính xác, tracking và hiệu năng |
| Dịch AI | Gemini, OpenAI, DeepSeek, Ollama, Google/Bing | Gemini, OpenAI, ML Kit | Thiếu provider, quản lý nhiều khóa và fallback sâu |
| Ngữ cảnh hội thoại | Phân tích nhân vật, quy tắc xưng hô hai chiều, rolling context | Prompt dịch theo lô, ngữ cảnh 3 câu | Thiếu phân tích nhân vật và bộ nhớ ngữ cảnh xuyên lô |
| TTS | Piper, Edge, CapCut, ElevenLabs, VieNeu | OpenAI và Android TTS offline | Thiếu engine offline đóng gói, diarization và gán giọng theo nhân vật |
| Tách giọng/nhạc | Vocal/BGM separation | Chưa thấy pipeline tách nguồn | Thiếu tách Vocals/BGM độc lập |
| Chỉnh sửa timeline | Nhiều track/layer: subtitle, blur, logo, mask, text | Timeline phụ đề cơ bản | Thiếu track/layer đa loại và thao tác chuyên nghiệp |
| Preview | libmpv, frame-accurate | VideoView | Cần preview chính xác và điều khiển seek/timeline tốt hơn |
| Chế độ video dọc/ngang | Workspace thích ứng 16:9/9:16 | Chưa thấy workspace thích ứng | Cần thiết kế riêng cho điện thoại |
| Hàng đợi | Quality/Performance, worker riêng, tiếp tục/xuất riêng | Tách âm thanh hàng loạt | Thiếu queue xử lý dự án bền vững |
| Xuất video | FFmpeg, NVENC/CPU fallback, audio stems, CapCut Draft | Media3/custom mux pipeline | Cần kiểm chứng codec, phụ đề, audio sync và xuất ổn định |
| Quản lý dự án | Project workflow đầy đủ | Lưu tên + video URI + SRT trong SharedPreferences | Cần lưu metadata, track, cấu hình, asset và phục hồi URI |

## Ưu tiên triển khai

### P0 — Độ tin cậy và an toàn dữ liệu

1. Tách xử lý nền khỏi Activity; dùng lifecycle-aware worker, tiến trình, hủy tác vụ và phục hồi sau khi app bị đóng.
2. Lưu project dạng thư mục/manifest có phiên bản; sao chép hoặc quản lý quyền truy cập URI bền vững cho mọi asset.
3. Không ghi đè phụ đề nếu project/video đã đổi; lưu checkpoint sau từng giai đoạn.
4. Sửa pipeline TTS đồng bộ: không cắt câu đang đọc khi chuyển cue; xử lý khoảng lặng, câu chồng thời gian, seek/pause và tốc độ giọng.
5. Bổ sung kiểm tra đầu ra: thời lượng audio/video, sample rate, codec, timestamp, subtitle bounds và lỗi mux.

### P1 — Dịch thuật chất lượng cao

1. Quản lý danh sách API key theo thứ tự; xoay vòng tuần tự khi quota/rate limit hoặc lỗi key; không chuyển sang dịch máy trước khi thử hết key/provider được cấu hình.
2. Phân loại lỗi 400/401/403/404/429/5xx; không retry vô hạn lỗi cấu hình hoặc prompt.
3. Lưu kết quả từng batch nguyên tử; resume từ batch thành công cuối cùng, không dịch lại hoặc mất câu.
4. Thêm bước phân tích nhân vật, quan hệ, giới tính khi có bằng chứng, quy tắc xưng hô hai chiều và rolling context ledger.
5. Có màn hình duyệt/sửa quy tắc nhân vật trước khi dịch; giữ nguyên số câu, thứ tự và timestamp.

### P2 — Media engine và biên tập

1. Đánh giá Media3 Transformer cho export; chỉ dùng FFmpeg/NDK nếu có dependency và giấy phép phù hợp.
2. Xây timeline nhiều track: video, subtitle, voice, BGM, text, logo, blur/mask.
3. Tách Vocals/BGM bằng mô hình on-device có kiểm tra dung lượng RAM, nhiệt độ và khả năng tăng tốc.
4. Preview 16:9/9:16 thích ứng; seek chính xác, waveform và thumbnail tải nền.
5. Xuất riêng voice/BGM/mix và preset độ phân giải, FPS, bitrate; bảo toàn audio gốc khi lồng tiếng.

### P3 — Trải nghiệm hoàn chỉnh

1. Giao diện mobile-first: chọn video → nhận diện → dịch → lồng tiếng → chỉnh sửa → xuất.
2. Hàng đợi nhiều dự án, trạng thái tiến trình, pause/resume/cancel và thông báo nền.
3. Preset theo thể loại/ngôn ngữ, subtitle style, voice profile và template dự án.
4. Kiểm tra khả năng tương thích thiết bị, quyền truy cập tệp, codec và giới hạn tài nguyên.

## Điều kiện phát hành

Không tuyên bố tương đương CapCap chỉ vì các nút chức năng đã xuất hiện. Mỗi hạng mục phải có kiểm thử logic, kiểm thử thiết bị Android thật, video dài, video dọc, audio nhiều kênh, mất mạng, hết quota, thiếu dung lượng, app bị đóng giữa chừng và khôi phục dự án. Bản preview không được gắn nhãn production release cho đến khi các kiểm tra bắt buộc đạt.

## Tài liệu tham chiếu

- CapCap README: https://github.com/notepower2k1/CapCap/blob/main/README.md
- CapCap technical stack: https://github.com/notepower2k1/CapCap/blob/main/docs/technical-stack.md
- CapCap project structure: https://github.com/notepower2k1/CapCap/blob/main/docs/project-structure.md
- CapCap how-to-use: https://github.com/notepower2k1/CapCap/blob/main/docs/how-to-use.md


## Cập nhật sau đợt hoàn thiện AI translation

- API key có thể nhập nhiều dòng hoặc phân tách bằng dấu phẩy/chấm phẩy; khóa được xử lý trong bộ nhớ, không lưu vào dự án.
- Mỗi batch thử lần lượt các khóa của provider chính; lỗi giới hạn tốc độ/lỗi máy chủ được retry có giới hạn rồi chuyển khóa tiếp theo.
- Khi toàn bộ khóa chính không khả dụng, ứng dụng thử danh sách khóa của provider dự phòng nếu người dùng đã cấu hình.
- Nếu batch thất bại sau khi đã dịch một phần, ứng dụng giữ lại các câu đã dịch và giữ nguyên văn bản nguồn của các câu còn lại, đồng thời lưu bản nháp để tránh mất nội dung.
- Đã bổ sung unit tests cho phân tích danh sách khóa, kiểm tra tên model và bảo toàn timestamp/nội dung trong kết quả dịch dở dang.

**Chưa xác nhận:** chưa có kết quả build/lint/unit test/Android Emulator smoke test được kiểm tra trong phiên này. Những thay đổi trên chưa đủ điều kiện để tuyên bố production release.
