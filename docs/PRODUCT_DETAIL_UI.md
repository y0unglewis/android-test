# Chi tiết sản phẩm Fresh Market

Thiết kế mở rộng để màn ProductDetailActivity giống quy trình xem hàng của sàn thương mại điện tử nhưng vẫn trung thực với nguồn dữ liệu bài thực hành.

## Bố cục và hành vi

1. Header có nút Back và biểu tượng giỏ hàng; ảnh sản phẩm lớn, tên, xuất xứ, giá/kg và điểm đánh giá nếu đã có review trên thiết bị.
2. Ba tab: **Mô tả** lấy nguyên văn mô tả từ Product; **Thông tin** chỉ dùng tên, xuất xứ, giá/kg và ghi chú nguồn dữ liệu; **Đánh giá** cho chọn 1–5 sao và viết 10–500 ký tự.
3. Mỗi username demo được có tối đa một đánh giá cho mỗi sản phẩm: lần sau có thể cập nhật hoặc xóa. Điểm trung bình và số lượt chỉ tính từ những bài viết thực được lưu trên thiết bị, không tạo review, doanh số hay chứng nhận giả.
4. Đánh giá được lưu riêng trong SharedPreferences của ứng dụng; người khác trên cùng thiết bị có thể thấy, nhưng không được đồng bộ lên Firestore hoặc sang điện thoại khác.
5. **Có thể bạn cũng thích** đọc lại danh mục fruits từ Firestore (chỉ đọc), loại trừ sản phẩm hiện tại, ưu tiên cùng xuất xứ rồi tới sản phẩm có giá gần hơn. Tối đa sáu card cuộn ngang; bấm card mở đúng trang chi tiết của món đó. Nếu mạng lỗi có nút thử lại.
6. CTA dưới cùng luôn hiện: **Thêm vào giỏ** và **Mua ngay**. Thêm vào giỏ dùng giỏ hiện có; Mua ngay mở checkout một sản phẩm, không làm mất món khác trong giỏ.

## Checklist kiểm thử

- [ ] Mở từ Home và từ GridView, ảnh/tên/giá/xuất xứ/miêu tả đúng chính sản phẩm được chọn.
- [ ] Chuyển qua lại ba tab; không crash khi mô tả hoặc xuất xứ trống.
- [ ] Chưa có review thì không hiển thị điểm số giả.
- [ ] Chọn từ 1 đến 5 sao; bắt lỗi đánh giá ít hơn 10 ký tự; gửi bài hợp lệ cập nhật điểm trung bình thật.
- [ ] Gửi lại từ cùng username là sửa bài cũ, không tạo bản sao; tài khoản khác trên cùng máy có thể đánh giá riêng.
- [ ] Chỉ tài khoản viết bài nhìn thấy nút Xóa đánh giá của tôi; sau khi xóa, điểm và số lượt được tính lại.
- [ ] Cuộn gợi ý, chọn card kiểm tra mở đúng Product; thử khi mạng lỗi và nút tải lại.
- [ ] Mua ngay với giỏ đã có hàng, đổi số lượng 1–99 và đặt đơn demo; giỏ cũ giữ nguyên.
- [ ] Thử giao diện 360–390dp, font lớn, chế độ ngang và nút Back.

## Giới hạn có chủ đích

- Firestore hiện chỉ cung cấp tên, xuất xứ, mô tả, giá và tên ảnh; không tự thêm khối lượng thực, tồn kho, thành phần, đơn đã bán, mã vận đơn hay thông tin chứng nhận.
- Không dùng dịch vụ đánh giá/đặt hàng bên ngoài. Tất cả thao tác thanh toán/đánh giá đều ghi rõ là demo.
