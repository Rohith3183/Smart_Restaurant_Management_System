🍽️ Smart Restaurant Management System
An advanced full-stack web application designed to streamline restaurant operations — from order management to billing — using modern technologies.
Built with Spring Boot, React (Vite), and MySQL, this project integrates real-time features, intelligent automation, and an intuitive user interface to enhance both staff productivity and customer experience. The system is fully deployed and accessible online, ensuring seamless availability for both staff and customers.

------------------------------------------------------------------------

🚀 Tech Stack
Frontend: React (Vite), HTML5, CSS3, JavaScript, Axios
Backend: Spring Boot (Java), Spring Security, JPA/Hibernate, RESTful APIs
Database: MySQL
Tools & Others: Postman, Maven, JWT Authentication, Email Service, WebSocket (for live updates)

Deployment Platforms : Vercel (Frontend), Render (Backend), Railway (MySQL DB)

------------------------------------------------------------------------

⚙️ Features
🔑 Authentication & User Roles
Secure JWT-based login and registration

Separate access levels: Admin, Manager, Waiter/Staff, and Customer

Role-based dashboards and API access control

------------------------------------------------------------------------

🪑 Table & Reservation Management
Add, edit, and delete restaurant tables

Real-time table occupancy tracking

QR-code–based table identification for instant order linking

Reservation scheduling with time slots and auto-cancellation for no-shows

------------------------------------------------------------------------

🍔 Menu & Item Management
Dynamic CRUD operations for menu items (add/edit/delete)

Category-wise organization (Starters, Main Course, Beverages, Desserts)

Image upload for dishes with live preview

Availability toggle (in-stock/out-of-stock)

Auto-update in customer view when the admin modifies items

------------------------------------------------------------------------

🧾 Order Management
Digital table ordering system for dine-in, takeaway, and online modes

Real-time order updates using WebSockets

Item-level modifications before final confirmation

Split billing option for group orders

Order status tracking: Placed → In Progress → Ready → Served

------------------------------------------------------------------------

💳 Billing & Payment
Automated bill generation after order completion

Tax and discount calculations

Support for cash, card, and online payments (fake gateway simulation)

Printable and emailed invoice with restaurant logo

------------------------------------------------------------------------

📊 Admin Dashboard
Overview of daily revenue, total orders, and active tables

Interactive charts (sales by day, top-selling items)

Manage staff accounts and view performance logs

Inventory tracking module that updates based on orders served

------------------------------------------------------------------------

📦 Inventory & Stock Management
Track ingredient stock levels with auto-decrement after serving

Low-stock alerts via email

Supplier management with contact history

------------------------------------------------------------------------

👥 Customer Interaction
Customer portal for browsing menu, placing orders, and tracking

Feedback & ratings system post order completion

Email confirmation and digital receipt

Reservation reminders via email

------------------------------------------------------------------------

📬 Notifications & Reports
Real-time notifications (new order, low stock, reservation updates)

PDF report generation for sales summary and inventory logs

Weekly email summaries to admin

------------------------------------------------------------------------

🧠 Smart/Unique Integrations
AI-based dish suggestion (recommend items based on past orders)

Voice-based order search (experimental)

QR-based self-service table ordering

Dynamic role management (Admin can create custom role permissions)

Real-time activity logs panel with WebSocket streaming

------------------------------------------------------------------------

🧩 System Architecture
text
Frontend (React + Vite)
     ↕ REST API calls (Axios)
Backend (Spring Boot)
     ↕ JPA/Hibernate ORM
Database (MySQL)
Includes WebSocket server for live updates and JWT middleware for authentication.

------------------------------------------------------------------------

🧪 API Testing
All backend endpoints tested with Postman and documented for integration testing.
Includes endpoints for authentication, users, tables, menu, orders, and billing.

------------------------------------------------------------------------

🧰 Setup Instructions
Clone the repository

bash
git clone https://github.com/Dhanush3183/Smart_Restaurant_Management_System.git
Backend setup

Open in Eclipse/IntelliJ, update MySQL credentials in application.properties, then run mvn spring-boot:run.

Frontend setup

bash
cd frontend
npm install
npm run dev

----------------------------------------------------------------------------

Access the app at ( https://smart-restaurant-frontend-gray.vercel.app )

Credentials for logging in into Sample Dashboards :-

Owner :
Code : RTD3183,
Role : Owner,
Username : RTD_Owner,
Password : Password123

Inventory:
Code : RTD3183,
Role : Inventory,
Username : RTD_Inventory,
Password : Password123

Accountant:
Code : RTD3183,
Role : Accountant,
Username : RTD_Accountant,
Password : Password123

Chef:
Code : RTD3183,
Role : Chef,
Username : RTD_Chef,
Password : Password123

Waiter:
Code : RTD3183,
Role : Waiter,
Username : RTD_Waiter,
Password : Password123

-----------------------------------------------

Team - RTD Techies | 
Full-Stack Developer | Hyderabad, Telangana, IN | 
📧 dhanushpaidi9@gmail.com
📧 vermanishtha3@gmail.com
📧 rohithmerugu2423@gmail.com
