# Fresh Market UI — Android Practice (19/11/2026)

## Mục tiêu
Giao diện Java + XML theo phong cách cửa hàng trái cây: Home, danh sách sản phẩm có tìm kiếm/bộ lọc/sắp xếp, trang chi tiết riêng, Login/Register/Profile đồng bộ. Theo yêu cầu bổ sung, có thêm **giỏ hàng → đặt đơn → sửa/hủy đơn → thanh toán mô phỏng**. Đây vẫn là bài thực hành, **không thực hiện giao dịch hoặc giao hàng thật**.

## Quy tắc giữ nguyên
1. **Không xóa bài cũ.** 7 file XML của bài trước được lưu nguyên nội dung tại [docs/legacy-ui/2026-11-19/](legacy-ui/2026-11-19/). File `FruitDatabaseHelper.java`, `DataSource.java`, các đoạn SQLite **đã comment** trong Home/Detail vẫn được giữ để cô kiểm tra.
2. **Không thay đổi dữ liệu.** Login/Register vẫn dùng `DemoAuth` + `SharedPreferences`. Firestore `fruits` là nguồn dữ liệu sản phẩm, sắp xếp lần đầu theo `sort_order`. Không ghi Firestore và không tự kích hoạt `uploadFruitsOnce()`.
3. **Giới hạn rõ phần demo.** Giỏ hàng/checkout/đơn hàng nay được yêu cầu bổ sung, nhưng chỉ lưu cục bộ trong SharedPreferences theo username. Không cổng thanh toán, không ngân hàng/QR thật, không đánh giá giả, không Google login, không backend mới. Không nhập thông tin cá nhân thật vào bài thực hành.
4. **`onCreate()` ngắn.** Mỗi Activity gọi các hàm nhỏ xử lý UI/Intent/Firestore và không chất logic vào `onCreate()`.
5. **Intent truyền nguyên object.** `Product` và `OrderRepository.Order` đều `implements Serializable`; truyền qua `putExtra()` với key trong `AppConstants`. Khi sửa đơn, dùng ID trong object để đọc lại bản cập nhật từ SharedPreferences, tránh lưu đè dữ liệu cũ.

## Màn hình
| Màn hình | File chính | Hành vi |
| --- | --- | --- |
| Home | `HomeActivity.java`, `activity_home.xml` | Lời chào, hero, trái cây nổi bật, Profile, Logout, bottom nav. Chạm card mở **chi tiết sản phẩm** |
| Danh sách sản phẩm | `DetailActivity.java`, `activity_detail.xml` | **GridView 2 cột** với card ảnh lớn, tên, xuất xứ và giá; một lần lấy Firestore; tìm kiếm theo tên/xuất xứ/mô tả (không phân biệt dấu), lọc xuất xứ và giá, sắp xếp mặc định/giá/tên, xóa lọc, empty/error state |
| Chi tiết sản phẩm | ProductDetailActivity.java, activity_product_detail.xml | Ảnh và giá nổi bật, tab Mô tả/Thông tin/Đánh giá, đánh giá demo người dùng tự nhập (không giả lượt đánh giá), sản phẩm tương tự đọc từ Firestore, thêm giỏ/xem giỏ/mua ngay |
| Giỏ hàng | CartActivity.java, activity_cart.xml, item_cart.xml | Xem các món đã chọn, tăng/giảm số lượng 1–99, xóa có xác nhận, xem tổng tạm tính và chuyển đến checkout |
| Checkout / Sửa đơn | CheckoutActivity.java, activity_checkout.xml | Tạo đơn từ giỏ hoặc sửa đơn đang chờ: số lượng, tên, số điện thoại, địa chỉ, ghi chú, phương thức demo |
| Lịch sử đơn | OrdersActivity.java, activity_orders.xml | Xem đơn của tài khoản đang đăng nhập, trạng thái, thời gian, tổng tiền |
| Chi tiết đơn | OrderDetailActivity.java, activity_order_detail.xml | Sửa/hủy khi còn chờ; đánh dấu thanh toán hoặc nhận hàng **mô phỏng**; khóa sửa khi hoàn tất |
| Login/Register | `activity_login.xml`, `activity_register.xml` | Form Material, xác thực `DemoAuth` cũ |
| Profile | activity_profile.xml | Đọc username từ SharedPreferences, liên kết lịch sử đơn |
| Card Home | `item_product.xml`, `ProductAdapter.java` | Giữ ListView cũ: ảnh, tên, xuất xứ, mô tả, giá, chỉ dẫn xem chi tiết |
| Card danh mục | `item_product_grid.xml`, `GridProductAdapter.java` | Card 2 cột: ảnh lớn, tên, xuất xứ, giá, chỉ dẫn xem chi tiết. Sử dụng drawable trái cây đã có trong project |

### Cách hoạt động tìm kiếm/lọc
- `allProducts` giữ nguyên toàn bộ kết quả lấy từ Firestore, theo thứ tự `sort_order`.
- `visibleProducts` là danh sách hiển thị sau khi lọc. `GridProductAdapter` trỏ đến danh sách này; `ProductAdapter` của Home không đổi.
- TextWatcher thay đổi `searchQuery`, rồi gọi `applyFilters()` để lọc **trên bộ nhớ**, không tốn thêm lượt truy vấn Firebase.
- Giá lọc: dưới 30.000 đ/kg, từ 30.000 đến 60.000 đ/kg, trên 60.000 đ/kg. Xuất xứ được tạo từ giá trị thật đang có trong Firestore, không hardcode địa danh.
- Sắp xếp: theo Firestore, giá tăng, giá giảm, tên A–Z. `notifyDataSetChanged()` yêu cầu `GridView` cập nhật.
- Nhấn item: gửi `Product` sang `ProductDetailActivity` qua `putExtra(AppConstants.EXTRA_SELECTED_PRODUCT, product)`.

### Quy trình đặt hàng / thanh toán demo
- Bottom Navigation trên Home và Catalog có **Trang chủ / Sản phẩm / Giỏ hàng / Đơn hàng**.
- ProductDetailActivity thêm 1 phần vào giỏ hoặc chọn Mua ngay một sản phẩm riêng. Mua ngay không làm trống giỏ hiện tại. OrderRepository lưu giỏ theo username, không sửa collection Firestore.
- CartActivity cho chỉnh số lượng và xóa món; CheckoutActivity cho điền **dữ liệu giả**, xem lại tổng, chọn COD demo hoặc chuyển khoản mô phỏng. Giỏ chỉ xóa khi lưu đơn thành công.
- OrdersActivity cho xem lịch sử; OrderDetailActivity cho sửa và hủy đơn chưa xử lý. CheckoutActivity dùng lại khi sửa; bấm Back trước khi lưu không thay đổi đơn cũ.
- Trạng thái có thể sửa: **Chờ xác nhận** (COD) và **Chờ thanh toán (demo)** (chuyển khoản). Khi đánh dấu hoàn tất thì khóa sửa; đơn hủy cũng khóa sửa.
- Giá từng món được chụp tại thời điểm thêm vào giỏ/tạo đơn, không gọi Firestore để thanh toán.

Chi tiết và checklist nghiệm thu: [DEMO_ORDERS.md](DEMO_ORDERS.md).
Thiết kế và checklist màn chi tiết: [PRODUCT_DETAIL_UI.md](PRODUCT_DETAIL_UI.md).

### Lỗi build đã gặp
Ở bản đầu PR #12, `app:itemActiveIndicatorColor` trong BottomNavigationView gây lỗi `Android resource linking failed` với thư viện Material 1.10.0. Bản này **đã xóa thuộc tính không tương thích ở cả hai layout Home/Detail** nhưng giữ BottomNavigationView và các màu tab còn lại.

## Kiểm tra trước khi merge PR #12
- [ ] `git fetch origin`, cập nhật nhánh `feature/fresh-market-ui-20261119` và `.\gradlew.bat assembleDebug` báo **BUILD SUCCESSFUL**.
- [ ] Login bằng demo; Home hiển thị đúng ảnh, tên, giá sản phẩm Firestore.
- [ ] Chọn card Home ➜ ProductDetail ➜ Back; mở danh sách từ nút CTA và bottom nav.
- [ ] Tìm kiếm tên có dấu/không dấu và theo xuất xứ (ví dụ `Binh Phuoc`), kiểm tra danh sách giảm đúng.
- [ ] Chọn từng lọc giá, xuất xứ, sắp xếp giá thấp/cao/tên. Bấm **Xóa lọc** trả về đủ sản phẩm mặc định.
- [ ] Tìm từ không có ➜ có thông báo rỗng, không crash.
- [ ] Catalog xếp đúng **2 card ngang trên mỗi hàng** ở độ rộng 360–390dp, ảnh lớn/giá không bị cắt, cuộn nhiều hàng mượt; Home vẫn dùng danh sách cũ.
- [ ] Tắt Internet thử lỗi Firestore/Thử tải lại.
- [ ] Chạm card trong Catalog ➜ thông tin trang ProductDetail khớp mặt hàng đã chọn.
- [ ] ProductDetail: chuyển 3 tab, đánh giá 1–5 sao thật (cục bộ), sửa/xóa review của chính mình, không tự hiện rating giả; bấm gợi ý mở đúng sản phẩm từ Firestore.
- [ ] Mua ngay từ ProductDetail ➜ checkout 1 sản phẩm, điều chỉnh số lượng; giỏ hàng khác vẫn nguyên sau khi đặt.
- [ ] Register, Profile, Logout, Back stack còn hoạt động; code SQLite/upload đã comment vẫn hiện trong source.
- [ ] ProductDetail → Thêm vào giỏ → tăng/giảm/xóa → checkout → tạo đơn COD demo → lịch sử → sửa đơn → hủy đơn hoặc đánh dấu nhận hàng demo.
- [ ] Tạo đơn chuyển khoản mô phỏng → Chờ thanh toán → đánh dấu thanh toán demo → không còn nút sửa/hủy.
- [ ] Đăng xuất/đăng nhập cùng username: đơn vẫn còn; tài khoản khác không thấy đơn của người trước.
- [ ] Kiểm tra thiết bị 360–390dp, font lớn, bàn phím, danh sách dài và cuộn các chip lọc ngang.

**Trạng thái nghiệm thu:** Code được kiểm tra tĩnh; cần build và thử trực tiếp bằng Android Studio để xác nhận giao diện Android thực tế. Chưa merge PR vào `develop` hay `main`.

