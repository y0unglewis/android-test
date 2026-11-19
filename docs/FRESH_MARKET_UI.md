# Fresh Market UI — Android Practice (19/11/2026)

## Mục tiêu
Làm lại **giao diện** cho project thực hành trái cây theo phong cách sản phẩm: xanh lá – trắng kem, thẻ sản phẩm, phân cấp chữ và thanh điều hướng đồng bộ. Vẫn là ứng dụng bài tập Android Java + XML, **không phải app thương mại**.

## Những điều phải giữ
1. **Không xóa bài cũ.** Toàn bộ 5 layout Activity, `item_product.xml` và `colors.xml` trước khi sửa đã được lưu nguyên văn trong [`docs/legacy-ui/2026-11-19/`](legacy-ui/2026-11-19/). `activity_main_2.xml`, `FruitDatabaseHelper.java`, `DataSource.java` không thay đổi; đoạn SQLite trong `HomeActivity` và `DetailActivity` vẫn là comment.
2. **Không đổi logic.** Login/Register dùng `DemoAuth`, phiên đăng nhập dùng `SharedPreferences`; danh sách trái cây lấy từ Cloud Firestore theo `sort_order`. `uploadFruitsOnce()` vẫn comment và không được gọi tự động. Không sửa Firestore rules/dữ liệu.
3. **Không thêm tính năng khác.** Không giỏ hàng, checkout, tìm kiếm giả, đăng nhập Google hoặc backend mới.
4. **Giữ `onCreate()` ngắn.** `onCreate()` chỉ gọi các hàm thiết lập đã tách: `setup...`, `show...`. Phần này đã có từ bài refactor trước, được giữ nguyên.
5. **`Intent.putExtra()` dùng constants.** `AppConstants.EXTRA_SELECTED_POSITION` và `AppConstants.EXTRA_SELECTED_PRODUCT`, không hardcode chuỗi. `Product` tiếp tục triển khai `Serializable`.

## Màn hình mới
| Màn hình | Tập tin Android đang chạy | Tính năng cũ vẫn giữ |
|---|---|---|
| Home | `app/src/main/res/layout/activity_home.xml` | Lời chào, Profile, Logout, Firestore, click trái cây, BottomNavigation |
| Catalog/Detail | `activity_detail.xml` | Hiển thị toàn bộ trái cây từ Firestore, cuộn đến sản phẩm chọn, Back, BottomNavigation |
| Login | `activity_login.xml` | Tài khoản demo, xác thực cục bộ, link Register |
| Register | `activity_register.xml` | Kiểm tra username/mật khẩu/xác nhận, lưu tài khoản demo |
| Profile | `activity_profile.xml` | Đọc username từ SharedPreferences, full name vẫn chỉ là ô demo disabled |
| Card trái cây | `item_product.xml` | `ProductAdapter` lấy hình/tên/xuất xứ/mô tả/giá như cũ |

Điểm duy nhất thay đổi trong Java là `HomeActivity` **dùng lại `ProductAdapter`** (đã có sẵn ở bài trước) thay cho `ArrayAdapter<String>` mặc định; các card có thể hiển thị thông tin Firestore. Không có request Firebase mới hay thay đổi dữ liệu.

## Kiểm tra trước khi merge
- [ ] `git status` sạch, chạy `.\gradlew.bat assembleDebug` báo `BUILD SUCCESSFUL` trên Android Studio
- [ ] Login bằng `demo / 123456`; tên đăng nhập hiển thị đúng trên Home và Profile
- [ ] Register với username/mật khẩu hợp lệ; quay về Login đăng nhập được
- [ ] Home tải trái cây, hiển thị đúng ảnh và giá trong card; chọn một loại sẽ mở Detail và cuộn đúng vị trí
- [ ] Mở Catalog bằng nút CTA/bottom nav vẫn hiện đủ sản phẩm; nhấn Back trở lại Home
- [ ] Logout xóa đúng username session, trở về Login
- [ ] Màn hình nhỏ khoảng 360–390dp, bàn phím khi Login/Register, danh sách cuộn và thanh điều hướng
- [ ] Firestore không bị ghi đè; code SQLite và `uploadFruitsOnce()` vẫn là comment

**Lưu ý:** Kiểm tra cấu trúc XML và tham chiếu ID/resource không thay thế cho việc chạy build Android và kiểm thử trên thiết bị. Giữ PR ở trạng thái Draft đến khi chạy thử.
