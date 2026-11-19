# Giỏ hàng, sửa đơn và thanh toán DEMO

## Phạm vi

Bài thực hành Android (Java + XML), mở rộng trên nhánh feature/fresh-market-ui-20261119. Đây là chức năng mô phỏng để cô và sinh viên kiểm tra luồng UI/UX; ứng dụng không kết nối ngân hàng, không phát sinh giao dịch hoặc giao hàng thật. Nên sử dụng tên, số điện thoại, địa chỉ giả khi demo.

- ProductDetailActivity: Thêm sản phẩm đã lấy từ Firestore vào giỏ trên máy.
- ProductDetailActivity: Nút Mua ngay đi đến checkout riêng cho một mặt hàng; không gộp hay xóa giỏ đã có.
- CartActivity: Danh sách giỏ hàng, thay đổi số lượng (1–99), xóa sản phẩm qua hộp thoại xác nhận, xem tạm tính.
- CheckoutActivity: Nhập tên người nhận, số điện thoại, địa chỉ, ghi chú; lựa chọn COD demo hoặc chuyển khoản mô phỏng; xác nhận trước khi lưu.
- OrdersActivity: Lịch sử đơn của username hiện đăng nhập; xem trạng thái và tổng tiền.
- OrderDetailActivity: Xem thông tin đơn, sửa/hủy đơn đang chờ, xác nhận thanh toán/nhận hàng mô phỏng.

## Quy tắc dữ liệu

1. Sản phẩm đọc từ Firestore fruits, giữ nguyên các hàm/bài cũ; không ghi ngược Firestore.
2. OrderRepository lưu giỏ hàng và lịch sử đơn cục bộ bằng SharedPreferences, phân tách theo username demo. Giá được chụp vào từng dòng sản phẩm, nên việc sửa catalog về sau không tự sửa giá đơn đã đặt.
3. Đơn COD có trạng thái Chờ xác nhận; đơn chuyển khoản có trạng thái Chờ thanh toán (demo).
4. Hai trạng thái chờ được sửa số lượng (và xóa dòng khi đơn còn hơn 1 dòng), tên người nhận, điện thoại, địa chỉ, ghi chú và phương thức thanh toán. Có thể hủy sau bước xác nhận.
5. Xác nhận nhận hàng demo (COD) hoặc xác nhận thanh toán demo (chuyển khoản) chuyển sang trạng thái hoàn tất tương ứng. Đơn hoàn tất hoặc đã hủy không thể sửa/hủy lần nữa.
6. Tạo đơn thành công sẽ ghi đơn và xóa giỏ trong cùng một lần lưu SharedPreferences. Nếu lưu thất bại, không tự xóa giỏ hoặc đơn cũ. Không xóa lịch sử khi hủy đơn.

## Checklist kiểm thử bằng Android Studio

1. Đăng nhập user demo A, mở Sản phẩm và xác nhận card hiển thị thành lưới 2 cột.
2. Mở chi tiết trái cây, chọn Thêm vào giỏ; đổi số lượng, thử giảm ở mức 1 và tăng ở mức 99; xóa có hộp thoại xác nhận.
3. Đặt hàng với giỏ có ít nhất hai sản phẩm; thử tên quá ngắn, điện thoại không đủ chữ số và địa chỉ quá ngắn để thấy thông báo form.
4. Chọn COD demo, tạo đơn: giỏ trống và lịch sử có đơn Chờ xác nhận.
5. Mở đơn rồi sửa địa chỉ, ghi chú và số lượng, thử xóa một dòng; bấm Quay lại trước khi lưu và kiểm tra dữ liệu chưa đổi; vào sửa lại rồi Lưu thay đổi.
6. Chuyển phương thức từ COD sang chuyển khoản mô phỏng trong màn sửa: trạng thái trở thành Chờ thanh toán (demo).
7. Xác nhận thanh toán mô phỏng, kiểm tra không còn nút Sửa/Hủy. Đơn COD có thể mô phỏng Đã nhận hàng để kiểm tra trạng thái còn lại.
8. Tạo đơn khác rồi Hủy, xác nhận đơn vẫn xuất hiện trong lịch sử ở trạng thái Đã hủy và bị khóa chỉnh sửa.
9. Đăng xuất user A, đăng nhập user B để đảm bảo lịch sử/giỏ không lẫn; đăng nhập lại A để kiểm tra dữ liệu vẫn còn.
10. Xoay màn hình khi đang sửa số lượng để đảm bảo nội dung nháp giữ nguyên; kiểm tra thiết bị 360–390dp, font lớn, mở bàn phím trong form và cuộn danh sách dài.
11. Thử nút Mua ngay khi giỏ đã có món khác: chỉ đơn hàng đang mua mới được đặt; sau khi đặt, giỏ cũ vẫn nguyên.

## Lưu ý khi review

- Không có backend đơn hàng, vận chuyển, thanh toán thật, QR, tài khoản ngân hàng hoặc callback giao dịch. Nhãn demo phải được giữ khi thuyết trình.
- Cần kiểm tra BUILD SUCCESSFUL và thao tác bằng máy/emulator Android Studio trước khi merge.
- Các phần đã comment trong Home/Detail về SQLite và upload dữ liệu cũ được giữ nguyên theo yêu cầu.
