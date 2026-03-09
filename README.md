# 🏪 Smart Store Platform

Hệ thống quản lý cửa hàng thông minh với AI Integration.

## 🚀 Tech Stack
- **Backend:** Java 21, Spring Boot 3.3, Spring Security
- **Database:** PostgreSQL (Supabase)
- **Auth:** JWT + RBAC (4 roles)
- **AI:** Google Gemini API

## ✨ Tính năng nổi bật
- JWT Authentication với phân quyền 4 cấp (Admin/Manager/Cashier/Customer)
- Quản lý sản phẩm, kho hàng với soft delete
- Tạo hóa đơn tự động trừ kho, xử lý hoàn trả
- Dashboard báo cáo doanh thu theo ngày/tháng
- **Recommendation Engine** bằng Market Basket Analysis (SQL thuần)
- **AI tóm tắt reviews** sản phẩm bằng Gemini API
- **Chatbot** tư vấn sản phẩm với RAG đơn giản

## 📡 API Endpoints
Xem đầy đủ tại: `http://localhost:8080/swagger-ui/index.html`
