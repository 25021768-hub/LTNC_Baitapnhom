# Hệ Thống Đấu Giá Trực Tuyến (Online Auction System)

## 1. Mô tả bài toán và phạm vi hệ thống

Ứng dụng desktop mô phỏng hệ thống đấu giá trực tuyến theo mô hình **Client–Server nhiều người dùng**. Người dùng đăng ký tài khoản theo một trong ba vai trò: **Admin**, **Seller** (người bán), **Bidder** (người đấu giá).

**Phạm vi hệ thống:**
- Seller đăng sản phẩm kèm giá khởi điểm, bước giá và thời gian phiên đấu giá
- Admin phê duyệt sản phẩm trước khi phiên đấu giá bắt đầu
- Bidder tham gia đặt giá theo thời gian thực; hỗ trợ Auto Bid (đặt giá tự động đến mức tối đa)
- Hệ thống tự động đóng phiên hết hạn, ghi nhận kết quả, cập nhật số dư
- Nhiều client có thể kết nối đồng thời vào cùng một server qua mạng LAN

---

## 2. Công nghệ sử dụng, môi trường chạy và yêu cầu cài đặt

| Thành phần | Phiên bản |
|---|---|
| Java (JDK) | 21 |
| JavaFX | 21.0.6 |
| MySQL Connector/J | 8.4.0 |
| Maven | 3.6+ |
| MySQL Server | 8.x |

**Yêu cầu cài đặt:**
- JDK 21 ([https://adoptium.net](https://adoptium.net))
- Maven 3.6+ ([https://maven.apache.org](https://maven.apache.org))
- MySQL Server 8.x ([https://dev.mysql.com/downloads/mysql](https://dev.mysql.com/downloads/mysql))

---

## 3. Cấu trúc thư mục và các module chính

```
LTNC_Baitapnhom/
├── pom.xml
└── src/main/
    ├── java/com/example/onlineauctionsystem/
    │   ├── HelloApplication.java       # Entry point — dialog chọn Host/Client
    │   ├── network/
    │   │   ├── AuctionServer.java      # TCP Server, CachedThreadPool
    │   │   ├── AuctionClient.java      # Giao tiếp với server qua socket
    │   │   └── AuctionMessage.java     # Giao thức truyền tin (Serializable)
    │   ├── model/
    │   │   ├── AuctionService.java     # Business logic (server-side)
    │   │   ├── DataStorage.java        # Truy cập MySQL (JDBC)
    │   │   ├── RemoteDataStorage.java  # Proxy gọi server (client-side)
    │   │   ├── Account.java
    │   │   ├── Product.java
    │   │   └── BidHistory.java
    │   ├── controller/
    │   │   ├── auth/        # Đăng nhập, đăng ký, OTP, đổi mật khẩu
    │   │   ├── admin/       # Duyệt sản phẩm, quản lý tài khoản
    │   │   ├── seller/      # Đăng bán, quản lý, lịch sử bán
    │   │   ├── bidder/      # Đấu giá, lịch sử + biểu đồ biến động giá
    │   │   └── common/      # Component dùng chung (card, row, ...)
    │   └── utils/
    │       ├── Validator.java
    │       ├── SceneConfig.java
    │       └── ProductImage.java
    └── resources/
        ├── com/example/onlineauctionsystem/  # File FXML giao diện
        ├── Kho_anh/                          # Icon & ảnh UI
        └── Product_Image/                    # Ảnh sản phẩm do user upload
```

---

## 4. Cài đặt Database

Tạo database và các bảng trong MySQL:

```sql
CREATE DATABASE online_auction CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE online_auction;

CREATE TABLE accounts (
    username     VARCHAR(50)  NOT NULL PRIMARY KEY,
    fullname     VARCHAR(255) NOT NULL,
    password     VARCHAR(255) NOT NULL,
    role         VARCHAR(20)  NOT NULL,
    id_card      VARCHAR(12)  DEFAULT NULL,
    email        VARCHAR(100) DEFAULT NULL,
    phone_number VARCHAR(15)  DEFAULT NULL,
    balance      DOUBLE       DEFAULT 0,
    is_locked    TINYINT(1)   NOT NULL DEFAULT 0
);

CREATE TABLE products (
    id             VARCHAR(50)  NOT NULL PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    initial_price  DOUBLE       NOT NULL,
    bid_increment  DOUBLE       DEFAULT 0,
    current_price  DOUBLE       NOT NULL,
    seller_name    VARCHAR(50)  NOT NULL,
    image_path     VARCHAR(255) NOT NULL,
    highest_bidder VARCHAR(50)  DEFAULT NULL,
    duration_hours BIGINT(11)   NOT NULL DEFAULT 0,
    start_time     DATETIME     DEFAULT NULL,
    end_time       DATETIME     DEFAULT NULL,
    status         VARCHAR(20)  NOT NULL
);

CREATE TABLE bid_history (
    id           INT(11)      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    bidder_name  VARCHAR(100) NOT NULL,
    product_id   VARCHAR(100) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    my_bid_price DOUBLE       NOT NULL,
    final_price  DOUBLE       NOT NULL,
    end_time     DATETIME     DEFAULT NULL,
    result       VARCHAR(20)  NOT NULL,
    is_paid      TINYINT(1)   NOT NULL DEFAULT 0
);

CREATE TABLE auto_bids (
    username   VARCHAR(50) NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    max_price  DOUBLE      DEFAULT NULL
);

CREATE TABLE product_price_log (
    log_id          INT(11)     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id      VARCHAR(50) DEFAULT NULL,
    bidder_name     VARCHAR(50) DEFAULT NULL,
    price_milestone DOUBLE      DEFAULT NULL,
    recorded_at     DATETIME    DEFAULT current_timestamp()
);
```

Sau đó sửa thông tin kết nối trong `src/main/java/.../model/DataStorage.java`:

```java
private static final String URL  = "jdbc:mysql://localhost:3306/online_auction";
private static final String USER = "root";   // đổi theo máy của bạn
private static final String PASS = "";       // đổi theo máy của bạn
```

> **Cơ chế đăng nhập:** Server theo dõi trạng thái đăng nhập qua bảng `activeSessions` lưu in-memory. Mỗi tài khoản chỉ được đăng nhập trên **một thiết bị tại một thời điểm** — đăng nhập lần 2 bị từ chối cho đến khi thiết bị kia đăng xuất hoặc đóng app. Session mất khi server khởi động lại.

---

## 5. Câu lệnh chạy chương trình

### Build project

```bash
# Windows / Linux / macOS
mvn clean compile
```

### Chạy ứng dụng (chứa cả Server lẫn Client)

```bash
# Windows
mvn javafx:run

# Linux / macOS
mvn javafx:run

# Đổi port (mặc định 5000)
mvn javafx:run -Dauction.port=6000
```

> **Lưu ý macOS/Linux:** Nếu gặp lỗi JavaFX không tìm thấy module, thêm biến môi trường:
> ```bash
> export JAVA_HOME=/path/to/jdk-21
> mvn javafx:run
> ```

---

## 6. Hướng dẫn chạy Server/Client theo thứ tự cụ thể

### Bước 1 — Máy Host (chạy Server + Client)

1. Đảm bảo MySQL đang chạy và đã tạo database ở bước trên
2. Chạy lệnh: `mvn javafx:run`
3. Cửa sổ khởi động hiện ra → chọn **"🖥 Chạy Server + Client (Host)"**
4. Server khởi động tại port **5000**, giao diện đăng nhập mở ra
5. Ghi lại **IP của máy Host** hiển thị trên dialog (ví dụ: `192.168.1.5`)

### Bước 2 — Máy Client (kết nối đến Host)

1. Mở một terminal mới (hoặc trên máy khác trong cùng mạng LAN)
2. Chạy lệnh: `mvn javafx:run`
3. Cửa sổ khởi động hiện ra → Chỉ chạy Client (Kết nối đến Host)"
4. Nhập IP của máy Host → nhấn Kết nối
5. Giao diện đăng nhập mở ra, kết nối thành công

> Để mở nhiều client, lặp lại Bước 2 trong các terminal khác nhau.

### Tài khoản mặc định để test

Tạo thủ công trong MySQL hoặc đăng ký qua giao diện. Ví dụ tạo tài khoản Admin:
```sql
INSERT INTO accounts (username, fullname, password, role, is_locked)
VALUES ('admin', 'Administrator', 'admin123', 'ADMIN', 0);
```

---

## 7. Danh sách chức năng đã hoàn thành

### Xác thực & Tài khoản
-  Đăng ký tài khoản (Seller / Bidder) với xác thực OTP qua email
-  Đăng nhập — mỗi tài khoản chỉ được đăng nhập trên **một thiết bị tại một thời điểm**; đăng nhập lần 2 bị từ chối cho đến khi thiết bị kia đăng xuất hoặc đóng app
-  Đổi mật khẩu, quên mật khẩu (reset qua email)
-  Quản lý hồ sơ cá nhân, nạp/xem số dư

### Admin
-  Phê duyệt / từ chối sản phẩm từ Seller
-  Khóa / mở khóa tài khoản người dùng
-  Xem danh sách toàn bộ tài khoản trong hệ thống

### Seller
-  Đăng sản phẩm mới (tên, giá khởi điểm, bước giá, thời lượng, ảnh)
-  Quản lý sản phẩm đang chờ duyệt / đang bán
-  Xem lịch sử sản phẩm đã bán

### Bidder
-  Xem danh sách sản phẩm đang mở đấu giá (realtime)
-  Đặt giá thủ công
-  Đặt giá tự động (Auto Bid) đến mức giá tối đa
-  Xem và theo dõi các phiên đang tham gia
-  Lịch sử đấu giá kèm **biểu đồ biến động giá** (LineChart)
-  Thống kê tổng lần đấu, số lần thắng, tỷ lệ thắng

### Hệ thống
-  Nhiều client kết nối đồng thời (CachedThreadPool)
-  Tự động đóng phiên hết hạn mỗi 30 giây (background scheduler)
-  Upload & hiển thị ảnh sản phẩm qua socket
-  Đăng xuất tự động khi đóng ứng dụng

---

## 8. Thành viên nhóm

| Thành viên | GitHub |
|---|--|
| giabao848 | [@giabao848](https://github.com/giabao848) |
| Thạch Minh Hiếu | [@25021768-hub](https://github.com/25021768-hub) |
| Nguyễn Huy Hoàng | https://github.com/25021775-NguyenHuyHoang |
| Phu | https://github.com/25020745-afk |

---

## 9. Link báo cáo và video demo

| Tài liệu | Link                                                                                 |
|----------|--------------------------------------------------------------------------------------|
| Drive    | https://drive.google.com/drive/folders/1VCo_vHUA-w5xP27zvCRw2l28K228U23U?usp=sharing |
