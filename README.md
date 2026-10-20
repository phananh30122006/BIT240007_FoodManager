# BIT240007_FoodManager - Ứng Dụng Quản Lý Món Ăn

- **Họ và tên sinh viên:** Nguyễn Phan Anh
- **Mã số sinh viên (MSSV):** BIT240007
- **Tên dự án:** `BIT240007_FoodManager`
- **Công nghệ phát triển:** Android Native (Kotlin)
- **Hình thức lưu trữ:** SQLite Local Database + MockAPI REST API (Hỗ trợ Offline & Tự động đồng bộ)

---

## 1. Thiết Kế Dữ Liệu (Quy định tiền tố MSSV)

Toàn bộ tên bảng (`Table`) và tên trường dữ liệu (`Column`) khi lưu trữ trong cơ sở dữ liệu đều có tiền tố **`BIT240007_`**:

### Bảng 1: `BIT240007_DishCategory` (Loại món ăn)
| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
| :--- | :--- | :--- | :--- |
| `BIT240007_Id` | TEXT | PRIMARY KEY | Mã định danh loại món |
| `BIT240007_Name` | TEXT | NOT NULL | Tên loại món (Khai vị, Món chính, Tráng miệng & Đồ uống...) |
| `BIT240007_Description` | TEXT | | Mô tả chi tiết |

### Bảng 2: `BIT240007_Dish` (Món ăn)
| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
| :--- | :--- | :--- | :--- |
| `BIT240007_Id` | TEXT | PRIMARY KEY | Mã định danh món ăn |
| `BIT240007_Name` | TEXT | NOT NULL | Tên món ăn |
| `BIT240007_Price` | REAL | NOT NULL | Đơn giá món ăn (> 0) |
| `BIT240007_PreparationTime` | INTEGER | NOT NULL | Thời gian chế biến tính bằng phút (> 0) |
| `BIT240007_IsAvailable` | INTEGER | NOT NULL | Trạng thái (1: Đang bán, 0: Ngừng bán) |
| `BIT240007_Description` | TEXT | | Mô tả món ăn |
| `BIT240007_DishCategoryId` | TEXT | NOT NULL, FOREIGN KEY | Khóa ngoại liên kết tới `BIT240007_DishCategory` |
| `BIT240007_DishImages` | TEXT | NOT NULL (JSON) | Danh sách ảnh: `[{"BIT240007_ImageUrl":"...", "BIT240007_IsThumbnail":true}]` |
| `BIT240007_SyncStatus` | TEXT | NOT NULL | Trạng thái đồng bộ (`SYNCED`, `PENDING_CREATE`, `PENDING_UPDATE`) |
| `BIT240007_UpdatedAt` | INTEGER | NOT NULL | Timestamp cập nhật lần cuối |

---

## 2. Các Chức Năng Đã Hoàn Thiện

### 2.1. Quản lý món ăn & danh mục (4.5 điểm cơ bản)
1. **Giao diện & Danh sách món ăn:**
   - Hiển thị danh sách món ăn dạng Card trực quan với ảnh Thumbnail, tên món, loại món, giá, huy hiệu chế biến và trạng thái bán.
   - Thêm món mới (Create) và Chỉnh sửa món (Edit) với form chọn loại món trực tiếp từ SQLite.
   - Xóa món với Dialog xác nhận an toàn.
2. **Tìm kiếm, lọc và sắp xếp nâng cao:**
   - Tìm kiếm theo một phần tên món (substring realtime).
   - Phối hợp đồng thời: Lọc theo loại món, lọc trạng thái đang bán, lọc khoảng giá (Giá tối thiểu - Giá tối đa).
   - Sắp xếp: Mặc định, Giá tăng dần, Giá giảm dần, Thời gian chế biến nhanh nhất.
   - Giữ nguyên trạng thái tìm kiếm, bộ lọc khi chuyển sang màn hình khác và quay trở lại.
3. **Quản lý ảnh & thời gian chế biến:**
   - Nhập Image URL để thêm ảnh cho món. Màn hình chi tiết hiển thị danh sách nhiều ảnh.
   - Cho phép thay đổi ảnh đại diện (Thumbnail) trực tiếp từ giao diện chi tiết hoặc form sửa (chỉ 1 ảnh được đánh dấu `IsThumbnail = true`).
   - Tự động hiển thị huy hiệu:
     - `Quick / Nhanh` khi Thời gian chế biến $\le 15$ phút.
     - `Regular / Thông thường` khi Thời gian chế biến $> 15$ phút.
4. **Ràng buộc và xử lý lỗi (Data Integrity):**
   - Không cho phép xóa loại món đang có món ăn sử dụng (hiển thị thông báo lỗi rõ ràng).
   - Món ngừng bán vẫn hiển thị trong danh sách quản lý, nhưng bị ẩn khi bật bộ lọc "Chỉ món đang bán".
   - Kiểm tra tên món không được trùng trong cùng một loại.
   - Validate giá $> 0$, thời gian chế biến $> 0$, tên không để trống, khoảng giá hợp lệ ($Min \le Max$).
5. **Đa ngôn ngữ (i18n):**
   - Mặc định tiếng Anh (English).
   - Nút chuyển đổi nhanh **English $\leftrightarrow$ Tiếng Việt** ngay trên thanh Toolbar.
   - Áp dụng đầy đủ toàn bộ ứng dụng (tiêu đề, nhãn, nút bấm, thông báo lỗi, dialog xác nhận) và lưu giữ ngôn ngữ đã chọn qua các màn hình và khi khởi động lại.

### 2.2. Hoạt động Offline & Tự động đồng bộ MockAPI (1.5 điểm nâng cao)
- **Khi ngoại tuyến (Mất mạng / Tắt Wifi / Bật Chế độ máy bay):**
  - Banner chuyển sang màu cam hiển thị `Offline Mode (Local Storage)`.
  - Ứng dụng vẫn hoạt động 100% bình thường trên SQLite local: cho phép thêm món mới, sửa món, cập nhật ảnh.
  - Các thay đổi được đánh dấu trạng thái chờ đồng bộ (`Pending Sync`).
- **Khi trực tuyến trở lại (Có mạng Wifi / 4G):**
  - Hệ thống `ConnectivityManager.NetworkCallback` tự động phát hiện kết nối mạng và kích hoạt `SyncManager.triggerAutoSync()` ngay lập tức trong background **mà không cần người dùng bấm bất kỳ nút nào**.
  - Đẩy các bản ghi tạo/sửa mới lên MockAPI REST API và cập nhật trạng thái bản ghi thành `Synced`.
  - Đảm bảo tính nhất quán dữ liệu, không sinh bản ghi trùng lặp.

---

## 3. Hướng Dẫn Mở Dự Án & Chạy Ứng Dụng

1. **Mở dự án:**
   - Khởi động **Android Studio**.
   - Chọn **File** $\rightarrow$ **Open** $\rightarrow$ Chọn thư mục:
     `C:\Users\Phan Anh\AndroidStudioProjects\BIT240007_FoodManager`
   - Đợi Android Studio đồng bộ Gradle hoàn tất.
2. **Chạy ứng dụng:**
   - Chọn thiết bị ảo (Emulator) hoặc cắm điện thoại Android (bật USB Debugging).
   - Nhấn nút **Run** (Shift + F10) hoặc biểu tượng tam giác xanh.
3. **File APK Debug có sẵn:**
   - Đường dẫn APK đã build thành công:
     `app/build/outputs/apk/debug/app-debug.apk`

---

## 4. Kịch Bản Gợi Ý Cho Video Nộp Bài

1. **Mở đầu:** Giới thiệu họ tên **Nguyễn Phan Anh**, MSSV **BIT240007**, project **BIT240007_FoodManager** (chỉ rõ header app có hiển thị tên và MSSV).
2. **Khám phá danh sách & Chi tiết:**
   - Xem 5 món ăn mẫu và 3 danh mục đã seed sẵn.
   - Mở xem chi tiết món "Phở Bò Tái Lăn Hà Nội" (12 phút chế biến $\rightarrow$ hiện huy hiệu **Quick / Nhanh**).
   - Xem các ảnh của món và thao tác bấm đổi ảnh đại diện (Set as Thumbnail).
3. **Thêm / Sửa / Xóa món & Ràng buộc:**
   - Thêm món mới: Nhập thử tên đã tồn tại trong cùng loại để demo thông báo lỗi trùng tên.
   - Thêm món hợp lệ thành công.
   - Thử vào "Quản lý loại món" và bấm xóa loại món "Món chính" $\rightarrow$ App cảnh báo không cho xóa vì đang có món ăn sử dụng.
4. **Tìm kiếm, Lọc & Sắp xếp:**
   - Gõ tìm kiếm tên món.
   - Bật lọc "Chỉ món đang bán" $\rightarrow$ Món ngừng bán sẽ biến mất.
   - Nhập khoảng giá và sắp xếp giá tăng dần.
   - Bấm vào chi tiết một món rồi Back lại $\rightarrow$ Các tiêu chí lọc vẫn được giữ nguyên.
5. **Chuyển đổi ngôn ngữ:** Bấm nút chuyển sang **Tiếng Việt** $\rightarrow$ Mọi nhãn, thông báo chuyển sang Tiếng Việt mượt mà.
6. **Demo Offline & Tự động đồng bộ:**
   - Tắt Wifi/Chế độ máy bay trên thiết bị $\rightarrow$ Banner đổi sang **Offline Mode**.
   - Thêm hoặc chỉnh sửa 1 món ăn khi đang Offline $\rightarrow$ Món được lưu với trạng thái **Pending Sync**.
   - Bật lại Wifi/Internet $\rightarrow$ Banner tự động chuyển sang **Syncing...** rồi sang **Synced** mà không cần bấm nút đồng bộ.
