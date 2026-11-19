# Fresh Market UI — Android Practice (19/11/2026)

## Mục tiêu
Giao diện Java + XML theo phong cách cửa hàng trái cây: Home, danh sách sản phẩm có tìm kiếm/bộ lọc/sắp xếp, **trang chi tiết riêng**, Login/Register/Profile đồng bộ. **Đây là bài thực hành**, không có mua hàng hay thanh toán.

## Quy tắc giữ nguyên
1. **Không xóa bài cũ.** 7 file XML của bài trước được lưu nguyên nội dung tại [docs/legacy-ui/2026-11-19/](legacy-ui/2026-11-19/). File `FruitDatabaseHelper.java`, `DataSource.java`, các đoạn SQLite **đã comment** trong Home/Detail vẫn được giữ để cô kiểm tra.
2. **Không thay đổi dữ liệu.** Login/Register vẫn dùng `DemoAuth` + `SharedPreferences`. Firestore `fruits` là nguồn dữ liệu sản phẩm, sắp xếp lần đầu theo `sort_order`. Không ghi Firestore và không tự kích hoạt `uploadFruitsOnce()`.
3. **Không thêm tính năng chưa học.** Không giỏ hàng, checkout, thanh toán, đánh giá giả, Google login hoặc backend mới.
4. **`onCreate()` ngắn.** Mỗi Activity gọi các hàm nhỏ xử lý UI/Intent/Firestore và không chất logic vào `onCreate()`.
5. **Intent key bằng constants.** `AppConstants.EXTRA_SELECTED_PRODUCT` (đối tượng `Product implements Serializable`) và `AppConstants.EXTRA_SELECTED_POSITION` được dùng thay chuỗi viết trực tiếp.

## Màn hình
| Màn hình | File chính | Hành vi |
| --- | --- | --- |
| Home | `HomeActivity.java`, `activity_home.xml` | Lời chào, hero, trái cây nổi bật, Profile, Logout, bottom nav. Chạm card mở **chi tiết sản phẩm** |
| Danh sách sản phẩm | `DetailActivity.java`, `activity_detail.xml` | Một lần lấy Firestore; tìm kiếm theo tên/xuất xứ/mô tả (không phân biệt dấu), lọc xuất xứ và giá, sắp xếp mặc định/giá/tên, xóa lọc, empty/error state |
| Chi tiết sản phẩm (mới) | `ProductDetailActivity.java`, `activity_product_detail.xml` | Nhận `Product` qua Intent, hiển thị ảnh/tên/giá/xuất xứ/mô tả, nút xem sản phẩm khác. Không gọi Firestore lần nữa |
| Login/Register | `activity_login.xml`, `activity_register.xml` | Form Material, xác thực `DemoAuth` cũ |
| Profile | `activity_profile.xml` | Đọc username từ SharedPreferences |
| Card | `item_product.xml`, `ProductAdapter.java` | Ảnh, tên, xuất xứ, mô tả, giá, chỉ dẫn xem chi tiết |

### Cách hoạt động tìm kiếm/lọc
- `allProducts` giữ nguyên toàn bộ kết quả lấy từ Firestore, theo thứ tự `sort_order`.
- `visibleProducts` là danh sách hiển thị sau khi lọc. `ProductAdapter` trỏ đến danh sách này.
- TextWatcher thay đổi `searchQuery`, rồi gọi `applyFilters()` để lọc **trên bộ nhớ**, không tốn thêm lượt truy vấn Firebase.
- Giá lọc: dưới 30.000 đ/kg, từ 30.000 đến 60.000 đ/kg, trên 60.000 đ/kg. Xuất xứ được tạo từ giá trị thật đang có trong Firestore, không hardcode địa danh.
- Sắp xếp: theo Firestore, giá tăng, giá giảm, tên A–Z. `notifyDataSetChanged()` yêu cầu `ListView` cập nhật.
- Nhấn item: gửi `Product` sang `ProductDetailActivity` qua `putExtra(AppConstants.EXTRA_SELECTED_PRODUCT, product)`.

### Lỗi build đã gặp
Ở bản đầu PR #12, `app:itemActiveIndicatorColor` trong BottomNavigationView gây lỗi `Android resource linking failed` với thư viện Material 1.10.0. Bản này **đã xóa thuộc tính không tương thích ở cả hai layout Home/Detail** nhưng giữ BottomNavigationView và các màu tab còn lại.

## Kiểm tra trước khi merge PR #12
- [ ] `git fetch origin`, cập nhật nhánh `feature/fresh-market-ui-20261119` và `.\gradlew.bat assembleDebug` báo **BUILD SUCCESSFUL**.
- [ ] Login bằng demo; Home hiển thị đúng ảnh, tên, giá sản phẩm Firestore.
- [ ] Chọn card Home ➜ ProductDetail ➜ Back; mở danh sách từ nút CTA và bottom nav.
- [ ] Tìm kiếm tên có dấu/không dấu và theo xuất xứ (ví dụ `Binh Phuoc`), kiểm tra danh sách giảm đúng.
- [ ] Chọn từng lọc giá, xuất xứ, sắp xếp giá thấp/cao/tên. Bấm **Xóa lọc** trả về đủ sản phẩm mặc định.
- [ ] Tìm từ không có ➜ có thông báo rỗng, không crash.
- [ ] Tắt Internet thử lỗi Firestore/Thử tải lại.
- [ ] Chạm card trong Catalog ➜ thông tin trang ProductDetail khớp mặt hàng đã chọn.
- [ ] Register, Profile, Logout, Back stack còn hoạt động; code SQLite/upload đã comment vẫn hiện trong source.
- [ ] Kiểm tra thiết bị 360–390dp, font lớn, bàn phím, danh sách dài và cuộn các chip lọc ngang.

**Trạng thái nghiệm thu:** Code được kiểm tra tĩnh; cần build và thử trực tiếp bằng Android Studio để xác nhận giao diện Android thực tế. Chưa merge PR vào `develop` hay `main`.

