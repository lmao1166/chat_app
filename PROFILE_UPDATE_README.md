# Chức năng Update Profile

## Tổng quan
Tích hợp thành công chức năng update profile vào mobile app, bao gồm:
- Cập nhật tên người dùng (username)
- Cập nhật email 
- Cập nhật ảnh đại diện (profile picture)

## Cấu trúc thêm mới

### Backend
Không cần thay đổi gì vì backend đã hỗ trợ multipart upload từ trước.

### Mobile App

#### 1. Model và API
- **ProfileRequest.kt**: Model cho request update profile
- **UserApi.kt**: Thêm method `updateProfile()` hỗ trợ multipart form data
- **UserRepository.kt**: Thêm method repository cho update profile

#### 2. ViewModel và UI
- **EditProfileViewModel.kt**: ViewModel xử lý logic update profile
- **EditProfileFragment.kt**: UI cho màn hình chỉnh sửa profile
- **ProfileFragment.kt**: Cập nhật để truyền dữ liệu user hiện tại

#### 3. Utilities
- **FileUtils.kt**: Utility xử lý file từ Uri
- **PermissionUtils.kt**: Utility xử lý permissions cho việc chọn ảnh

#### 4. Layout
- **fragment_edit_profile.xml**: Cập nhật layout thêm trường email

## Cách sử dụng

### 1. Truy cập màn hình Edit Profile
- Từ màn hình Profile, nhấn vào "Chỉnh sửa thông tin"
- Dữ liệu hiện tại sẽ được load sẵn

### 2. Cập nhật thông tin
- **Tên người dùng**: Bắt buộc, không được để trống
- **Email**: Tùy chọn, có thể để trống
- **Ảnh đại diện**: Nhấn vào ảnh để chọn từ thư viện

### 3. Lưu thay đổi
- Nhấn "Lưu thay đổi"
- Hệ thống sẽ upload dữ liệu và ảnh (nếu có)
- Quay về màn hình Profile với dữ liệu mới

## Permissions yêu cầu
App sẽ tự động yêu cầu quyền khi cần:
- **READ_EXTERNAL_STORAGE** (Android < 13)
- **READ_MEDIA_IMAGES** (Android >= 13)

## Xử lý lỗi
- Kiểm tra kết nối mạng
- Validate dữ liệu đầu vào
- Xử lý lỗi upload file
- Hiển thị thông báo lỗi phù hợp

## API Endpoint
```
PUT /api/v1/users
Content-Type: multipart/form-data

Fields:
- username (text): Tên người dùng
- email (text, optional): Email
- profilePicture (file, optional): Ảnh đại diện
```

## Testing
Để test chức năng:
1. Đăng nhập vào app
2. Vào màn hình Profile
3. Nhấn "Chỉnh sửa thông tin"
4. Thay đổi username, email hoặc ảnh
5. Nhấn "Lưu thay đổi"
6. Kiểm tra dữ liệu đã được cập nhật

## Notes
- Ảnh sẽ được resize và lưu trên server
- Tên file ảnh được tạo unique dựa trên userId và timestamp
- Ảnh cũ sẽ được xóa khi upload ảnh mới (trừ default avatar)
