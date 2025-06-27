# 🎓 Elective Course Registration System (Java + Vert.x + MongoDB)

This project is a backend API system for student elective course registration.  
Built using **Java**, **Vert.x Web**, **MongoDB**, and **Gmail SMTP for password delivery**.

---

## ✅ Features

- 🧑 Student registration with name & email
- 🔐 Auto-generated random password emailed to student
- 🔑 Login system using email & password
- 📚 View available elective courses
- 📥 Register for a course (auto-decrements available seats)

---

## 🛠️ Tech Stack

- Java 17  
- Vert.x Web (v4.5)  
- MongoDB  
- Vert.x MailClient (SMTP)  
- Postman (for testing APIs)

---

## 📬 SMTP Setup Instructions (Gmail)

1. Go to: [https://myaccount.google.com/security](https://myaccount.google.com/security)
2. Enable **2-Step Verification**
3. Create an **App Password** for "Mail"
4. Use that 16-digit password in `EmailService.java` like this:

```java
.setUsername("your-email@gmail.com")
.setPassword("your-app-password")
