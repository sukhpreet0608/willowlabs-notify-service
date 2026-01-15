# WillowLabs Notify Service

A robust notification service built with **Spring Boot 3.5.9**, **Java 21**, and **Maven**. This application is fully containerized and optimized for secure cloud deployments.

## 🚀 Features
- **Java 21 Support**: Leverages the latest LTS features.
- **Multi-Stage Docker Build**: Minimized image size using `eclipse-temurin:21-jre-alpine`.
- **Enhanced Security**: Runs as a non-root `spring` user inside the container.
- **Persistent Storage**: Configured `/data` volume for H2 database persistence.
- **Cross-Platform Compatibility**: Maven wrapper optimized for both Windows and Linux environments.

---

## 🛠️ Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Git](https://git-scm.com/)

---

## 🚦 Getting Started (Docker)

### 1. Clone the Repository
```bash
git clone https://github.com/sukhpreet0608/willowlabs-notify-service.git
cd willowlabs-notify-service
```

### 2. Run Docker Compose
```bash
docker compose up --build
```

---
## 📋 Business Logic & Assumptions

To ensure predictable notification delivery, the following business rules have been implemented in this POC:

### ✉️ Email Notifications
* **Internal Routing Only**: Emails are exclusively sent to internal staff/users.
* **Validation**: The system cross-references recipients against the `internal_users` database table. If a recipient is not found in this table, the email request is rejected to prevent unauthorized external communication.

### 📱 SMS Notifications
* **Dynamic Routing**: The SMS is sent to the `mobileNumber` provided directly in the API request payload.
* **Optionality**: If the `mobileNumber` field is empty or null in the request, the SMS delivery step is skipped (no error is thrown, but the notification is not sent).

### 🔔 Push Notifications
* **Target Audience**: Scheduled Push notifications are designed only for Mobile Application users.
* **Token Requirement**: Delivery requires a valid `deviceToken` to be present in the user profile or request.

---

---

## 🏗️ Production Roadmap & Tradeoffs

This project is currently a **Proof of Concept (POC)**. To transition this service to a **production-grade environment**, the following architectural changes are recommended.

### 1. Infrastructure & Services

| Feature | Current POC Implementation | Production Recommendation |
|-------|---------------------------|---------------------------|
| Database | H2 (In-Memory / Local File) | PostgreSQL or MySQL for persistence and ACID compliance |
| Push Notifications | Log-based Mocking | Firebase Cloud Messaging (FCM) for cross-platform delivery |
| SMS Service | Log-based Mocking | Twilio SMS or AWS SNS integration |
| Email Service | MailHog (Local SMTP testing) | Amazon SES, SendGrid, or Mailgun |
| Secret Management | Hardcoded in `application.yml` | AWS Secrets Manager, Azure Key Vault, or Kubernetes Secrets |

---

### 2. Critical Production Enhancements

#### 🚀 Scalability: Asynchronous Processing
Currently, notifications are processed **synchronously**. In a high-traffic environment, this would block the API.

**Proposed Fix:**  
Integrate **RabbitMQ** or **Apache Kafka**. The API should accept the request and immediately return a `PENDING` status, while a background worker processes the actual delivery.

---

#### 🛡️ Resilience: Error Handling & Retries
External APIs (such as Twilio or Amazon SES) can occasionally fail.

**Proposed Fix:**  
Implement **Resilience4j** to provide **Circuit Breakers** and **Exponential Backoff Retries**. This prevents cascading failures when third-party providers are unavailable.

---

#### 📊 Observability: Monitoring & Logging
Lack of visibility makes production debugging difficult.

**Proposed Fix:**
- Enable **Spring Boot Actuator**
- Export metrics to **Prometheus** and visualize using **Grafana**
- Implement **structured JSON logging** and ship logs to an **ELK Stack (Elasticsearch, Logstash, Kibana)**

---

#### 🔑 Security: API Authentication
Currently, endpoints are unsecured for simplicity.

**Proposed Fix:**  
Secure APIs using **Spring Security with OAuth2/JWT** to ensure only authorized services can trigger notifications.

---

