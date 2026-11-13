# Fruit Catalog Demo

> Ứng dụng Android thực hành với **Java + XML**, mô phỏng danh mục 15 loại trái cây, điều hướng giữa các màn hình và lưu trữ dữ liệu cục bộ bằng **SQLite** và **SharedPreferences**.

Đây là repository `android-test`, phục vụ học tập các thành phần cơ bản của Android. **Fruit Catalog Demo không phải ứng dụng học từ vựng tiếng Nhật**, dù cấu hình dự án hiện vẫn sử dụng tên `japanese-vocabulary-learning-app` và package `com.example.japanese_vocabulary_learning_app`.

## 1. Chức năng hiện có

| Màn hình | Chức năng |
| --- | --- |
| **Login** | Đăng nhập bằng tài khoản mẫu hoặc tài khoản đã đăng ký trên thiết bị. |
| **Register** | Đăng ký một tài khoản cục bộ; kiểm tra tên đăng nhập, mật khẩu và xác nhận mật khẩu. |
| **Home** | Hiển thị lời chào theo username và danh sách tên 15 loại trái cây đọc từ SQLite. |
| **Detail / Products** | Hiển thị hình minh họa, tên, xuất xứ, mô tả và giá; có thể cuộn đến trái cây được chọn từ Home. |
| **Profile** | Hiển thị avatar và username đang đăng nhập. Có ô **Họ và tên** nhưng hiện bị khóa, chưa hỗ trợ chỉnh sửa hoặc lưu. |

Điều hướng chính: `Login → Home → Detail`, `Home → Profile`, `Login ↔ Register`. Thanh điều hướng dưới có hai mục **Trang chủ** và **Sản phẩm**; nút Đăng xuất đưa người dùng về màn hình Login.

## 2. Công nghệ sử dụng

- **Ngôn ngữ:** Java (mức ngôn ngữ được cấu hình là Java 11).
- **Giao diện:** Android XML, `LinearLayout`, `ScrollView`, `ListView`, `ImageView`, `EditText`, `BottomNavigationView`.
- **Thành phần Android:** Activity, Intent, Adapter, `SQLiteOpenHelper`, `SharedPreferences`.
- **Cơ sở dữ liệu:** SQLite cục bộ (`fruits.db`).
- **Thư viện giao diện:** AndroidX AppCompat và Material Components (cùng các dependency trong Gradle Version Catalog).
- **Cấu hình hiện tại:** `minSdk = 24`, `targetSdk = 37`, `compileSdk = 37`.
- **Build:** Gradle Kotlin DSL (`.gradle.kts`) và Gradle Wrapper có sẵn trong repo.

Ứng dụng **không yêu cầu máy chủ, tài khoản cloud hay API key**.

## 3. Cài đặt và chạy

### Yêu cầu

- Android Studio có khả năng mở và đồng bộ dự án Gradle.
- Android SDK phù hợp với cấu hình `compileSdk = 37`.
- Một Android Emulator hoặc điện thoại Android dùng để chạy thử (API 24 trở lên).
- Kết nối Internet khi Gradle cần tải dependency lần đầu.

### Các bước

1. Clone repository:

   ```bash
   git clone https://github.com/y0unglewis/android-test.git
   cd android-test
   ```

2. Vì SQLite và màn Profile mới đang ở nhánh tính năng, chuyển sang đúng nhánh:

   ```bash
   git fetch origin
   git switch --track origin/feature/sqlite-fruits-profile-cleanup
   ```

   > Nếu đã có nhánh local tương ứng, dùng `git switch feature/sqlite-fruits-profile-cleanup` và `git pull --ff-only origin feature/sqlite-fruits-profile-cleanup` khi working tree sạch.

3. Trong Android Studio, chọn **Open** và mở thư mục repository.
4. Chờ **Gradle Sync** hoàn tất. Sử dụng Gradle JDK tương thích với phiên bản Android Gradle Plugin đang cấu hình.
5. Chọn thiết bị trong Device Manager, sau đó **Run** cấu hình `app`.

Có thể build bản debug từ Terminal trên Windows:

```powershell
.\gradlew.bat assembleDebug
```

### Tài khoản thử

| Username | Password |
| --- | --- |
| `demo` | `123456` |

Ngoài tài khoản trên, màn hình Register cho phép tạo **một tài khoản cục bộ**. Đăng ký tài khoản mới sẽ thay thế tài khoản cục bộ đã lưu trước đó; tài khoản `demo` vẫn dùng được.

> **Lưu ý bảo mật:** Cơ chế đăng nhập này chỉ dùng để học. Mật khẩu tài khoản đăng ký được xử lý bằng SHA-256 và salt trong mã nguồn demo; đây **không phải** hệ thống xác thực phù hợp cho môi trường production.

## 4. Cấu trúc chính

```text
app/src/main/
├── java/com/example/japanese_vocabulary_learning_app/
│   ├── LoginActivity.java          # Đăng nhập
│   ├── RegisterActivity.java       # Đăng ký tài khoản cục bộ
│   ├── HomeActivity.java           # Danh sách tên trái cây, điều hướng
│   ├── DetailActivity.java         # Danh sách chi tiết trái cây
│   ├── ProfileActivity.java        # Hồ sơ người dùng
│   ├── DemoAuth.java               # Xác thực tài khoản demo
│   ├── FruitDatabaseHelper.java    # Tạo và truy vấn SQLite
│   ├── DataSource.java             # Dữ liệu mẫu để khởi tạo database
│   ├── Product.java                # Model một loại trái cây
│   └── ProductAdapter.java         # Hiển thị từng item trong Detail
├── res/
│   ├── layout/                     # Các giao diện Activity và item_product.xml
│   ├── drawable/                   # Icon và hình trái cây dạng VectorDrawable
│   └── menu/bottom_nav_menu.xml    # Trang chủ / Sản phẩm
└── AndroidManifest.xml             # Khai báo các Activity

docs/sqlite-fruits.md               # Ghi chú thực hành và kiểm tra SQLite
```

## 5. Dữ liệu được lưu như thế nào?

### SQLite — danh mục trái cây

Lớp `FruitDatabaseHelper` kế thừa `SQLiteOpenHelper` và sử dụng database `fruits.db`, bảng `fruits`:

| Cột | Kiểu SQLite | Vai trò |
| --- | --- | --- |
| `id` | `INTEGER PRIMARY KEY AUTOINCREMENT` | Khóa chính |
| `name` | `TEXT` | Tên trái cây (không trùng) |
| `origin` | `TEXT` | Xuất xứ |
| `description` | `TEXT` | Mô tả |
| `price` | `INTEGER` | Giá tham khảo |
| `image_name` | `TEXT` | Tên drawable, ví dụ `fruit_apple` |

- Lần đầu ứng dụng tạo database, `onCreate()` tạo bảng và thêm **15 trái cây mẫu** từ `DataSource.getProducts()`.
- `HomeActivity` gọi `getAllNames()` để đọc tên trái cây bằng truy vấn SQLite.
- `DetailActivity` gọi `getAllProducts()`, chuyển các hàng kết quả `Cursor` thành `ArrayList<Product>` để `ProductAdapter` hiển thị.
- Dữ liệu không được khởi tạo lại ở mỗi lần mở app. Các hình minh họa vẫn là tài nguyên trong `res/drawable`, database chỉ lưu **tên** tài nguyên.

### SharedPreferences — thông tin đăng nhập

- `fruit_demo_account`: lưu thông tin của tài khoản được đăng ký cục bộ (username và dữ liệu kiểm tra mật khẩu).
- `user_session`: lưu username sau khi đăng nhập thành công, để Home và Profile hiển thị đúng người dùng. Đăng xuất sẽ xóa username của phiên này.

SQLite **không** lưu username phiên đăng nhập trong phiên bản hiện tại; hai cơ chế lưu trữ đảm nhiệm hai mục đích khác nhau.

## 6. Kiểm tra database

1. Chạy ứng dụng ở chế độ debug bằng Android Studio và mở màn Home.
2. Vào **View → Tool Windows → App Inspection → Database Inspector** (tùy phiên bản Android Studio, vị trí menu có thể khác).
3. Chọn tiến trình ứng dụng rồi tìm `fruits.db` → bảng `fruits`.
4. Thử các truy vấn:

   ```sql
   SELECT * FROM fruits;
   SELECT COUNT(*) FROM fruits;
   SELECT name, price FROM fruits ORDER BY price DESC;
   ```

Nếu chưa chỉnh sửa database, `SELECT COUNT(*)` sẽ cho kết quả **15**. Xem thêm hướng dẫn tại [`docs/sqlite-fruits.md`](docs/sqlite-fruits.md).

## 7. Phạm vi hiện tại và hướng phát triển

**Đã triển khai:** Đăng nhập/đăng ký cục bộ, điều hướng Activity, danh sách và chi tiết trái cây, màn Profile cơ bản, SQLite để lưu và đọc dữ liệu trái cây, SharedPreferences để lưu username.

**Chưa triển khai:** Giao diện thêm/sửa/xóa trái cây (CRUD), chỉnh sửa và lưu họ tên trong Profile, xác thực qua máy chủ, đồng bộ dữ liệu nhiều thiết bị.

Repo nhằm mục đích **thực hành Android**, chưa được kiểm thử hoặc thiết kế như một ứng dụng production.


---

**Repository:** [github.com/y0unglewis/android-test](https://github.com/y0unglewis/android-test)  
**Mục đích:** Bài thực hành lập trình Android (Java + XML, SQLite và SharedPreferences).
