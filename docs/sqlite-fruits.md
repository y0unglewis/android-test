# SQLite — dữ liệu trái cây (Android Java)

## Mục tiêu
- Database trên thiết bị: `fruits.db`.
- Bảng `fruits`: `id`, `name`, `origin`, `description`, `price`, `image_name`.
- Dữ liệu 15 trái cây ban đầu lấy từ `DataSource.getProducts()` **chỉ khi tạo database lần đầu**.
- Sau đó, Home và Detail đều thực hiện `SELECT` từ SQLite, không lấy trực tiếp từ DataSource nữa.
- `image_name` là tên resource như `fruit_apple` thay vì ID `R.drawable`, để có thể ánh xạ thành ảnh khi đọc.

## Luồng hoạt động

1. `HomeActivity` tạo `FruitDatabaseHelper`, gọi `getAllNames()` để hiển thị danh sách tên.
2. Android tạo `fruits.db` lần đầu nếu chưa có; `onCreate()` chạy `CREATE TABLE` và lưu 15 dòng bằng `ContentValues + insertOrThrow()`.
3. Chọn trái cây trên Home: gửi `selected_position` qua Intent như cũ.
4. `DetailActivity` gọi `getAllProducts()`; `Cursor` chuyển các dòng SQL thành `ArrayList<Product>`.
5. `ProductAdapter` vẫn hiển thị ảnh, tên, giá, xuất xứ, mô tả như cũ.

## Xem dữ liệu bằng Database Inspector

Chạy ứng dụng trên Emulator, đăng nhập, mở Home hoặc Detail ít nhất một lần. Trong Android Studio mở **View > Tool Windows > App Inspection > Database Inspector** (đường dẫn có thể khác giữa phiên bản Android Studio). Chọn tiến trình app đang chạy, mở `fruits.db`, bảng `fruits`.

Ví dụ câu SQL để thử:

```sql
SELECT * FROM fruits;

SELECT name, price FROM fruits ORDER BY price DESC;

SELECT name, origin FROM fruits WHERE price > 50000;
```

Với Database Inspector, có thể thử UPDATE:

```sql
UPDATE fruits SET price = 60000 WHERE name = 'Apple';
SELECT name, price FROM fruits WHERE name = 'Apple';
```

Sau đó quay lại Home/Detail để tải lại màn hình, kiểm tra giá thay đổi. Vì app **không seed lại** khi đã có database, giá lưu trong SQLite không bị trở về 50000 khi mở lại app.

## Những điểm cần nhớ

- **SQLite** lưu dữ liệu trái cây; **SharedPreferences** vẫn lưu username phiên đăng nhập. Hai phần độc lập.
- SQLite mặc định lưu cục bộ trong dữ liệu riêng của ứng dụng. Đóng app/mở lại không làm mất dữ liệu; xóa dữ liệu ứng dụng hoặc cài mới sẽ tạo lại database mẫu (việc khôi phục còn tùy cơ chế sao lưu hệ thống).
- Không cần thay đổi `Product.java`, `ProductAdapter.java` hoặc XML giao diện.
- Không có thư viện mới trong Gradle; dùng `SQLiteOpenHelper` có sẵn của Android.
- Khi tăng `DATABASE_VERSION` cần tự viết migration trong `onUpgrade()`; hiện app vẫn dùng version 1.
- Vì đây là bài tập với 15 dòng, truy vấn đang chạy trên UI thread. Dự án lớn nên chuyển I/O database sang background thread hoặc dùng Room.

## Bài tự kiểm tra

1. Đếm bản ghi: `SELECT COUNT(*) FROM fruits;` → 15 nếu chưa tự thêm/xóa.
2. Đổi giá Apple bằng `UPDATE`, mở lại Detail để kiểm tra dữ liệu còn được lưu.
3. Giải thích vì sao `DataSource.getProducts()` chỉ chạy lúc database được tạo.
4. Giải thích sự khác nhau giữa `ContentValues` (INSERT) và `Cursor` (đọc SELECT).
