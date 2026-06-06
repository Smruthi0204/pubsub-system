# Distributed Pub/Sub Notification Service

This project is a notification system where applications can register as publishers or subscribers. Publishers push events to a REST API, events are queued in AWS SQS, and subscribers receive them in real-time via WebSocket, Email, or Webhook. Includes retry logic, delivery status tracking, and a live dashboard.

---

## Tech Stack

![Java](https://img.shields.io/badge/Java-17-orange?style=flat&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-green?style=flat&logo=springboot)
![AWS SQS](https://img.shields.io/badge/AWS%20SQS-Queue-yellow?style=flat&logo=amazonsqs)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=flat&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Containerized-blue?style=flat&logo=docker)
![AWS EC2](https://img.shields.io/badge/AWS%20EC2-Deployed-orange?style=flat&logo=amazonec2)
![WebSocket](https://img.shields.io/badge/WebSocket-RealTime-purple?style=flat)
![JavaMail](https://img.shields.io/badge/JavaMail-SMTP-red?style=flat)

---

## Features

- Publisher and Subscriber registration via REST API
- Event publishing with topic-based routing
- Real-time delivery via WebSocket for live browser/app clients
- Email delivery via Gmail SMTP
- Webhook delivery via HTTP POST to subscriber endpoints
- AWS SQS as the message broker, decoupling publishing from delivery
- Automatic retry logic with up to 3 attempts per failed delivery
- Delivery status tracking in PostgreSQL
- Simple dashboard showing delivery statistics
- Fully Dockerized and deployed on AWS EC2

---

## System Architecture

<p align="center">
  <img src="https://github.com/user-attachments/assets/4fd85187-fe1e-445c-88d9-175d5d35537f" alt="System Architecture" width="100%">
</p>

---

## API Endpoints

| Method | Endpoint | Description | Request Body |
|----------|----------|----------|----------|
| POST | `/api/pubregister` | Register a publisher | `name`, `emailId` |
| POST | `/api/subregister` | Register a subscriber | `name`, `topics`, `deliverytype`, `endpoint`, `emailId` |
| POST | `/api/events` | Publish an event | `topic`, `payload`, `publisher.id` |
| GET | `/dashboard` | Get delivery statistics as JSON | — |
| GET | `/dashboard.html` | View visual dashboard | — |

---

## Example Requests

### Register Publisher

```http
POST /api/pubregister
```

```json
{
  "name": "App A",
  "emailId": "appa@gmail.com"
}
```

### Register Subscriber

```http
POST /api/subregister
```

```json
{
  "name": "Sub A",
  "topics": ["orders", "payments"],
  "deliverytype": "webhook",
  "endpoint": "https://webhook.site/your-unique-url",
  "emailId": "suba@gmail.com"
}
```

### Publish Event

```http
POST /api/events
```

```json
{
  "topic": "orders",
  "payload": "Order placed for item X",
  "publisher": {
    "id": "your-publisher-uuid"
  }
}
```

---

## Setup and Run Locally

### Prerequisites

- Java 17
- Maven
- PostgreSQL
- Docker Desktop

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/pubsub-system.git
cd pubsub-system
```

### 2. Create PostgreSQL Database

```sql
CREATE DATABASE pubsub_db;
```

### 3. Create application.properties

Create:

```text
src/main/resources/application.properties
```

```properties
spring.application.name=System
server.port=8081

spring.datasource.url=jdbc:postgresql://localhost:5432/pubsub_db
spring.datasource.username=your_username
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_gmail
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

spring.cloud.aws.credentials.access-key=your_access_key
spring.cloud.aws.credentials.secret-key=your_secret_key
spring.cloud.aws.region.static=us-east-1
```

### 4. Run with Docker

```bash
./mvnw clean package -DskipTests
docker compose up --build
```

### 5. Access the Application

| Service | URL |
|----------|----------|
| API | http://localhost:8081 |
| Dashboard | http://localhost:8081/dashboard.html |

---

## Deploy on AWS EC2

### Prerequisites

- AWS account (Free Tier)
- EC2 instance (t2.micro, Ubuntu 22.04)
- Security group with ports 22 and 8081 open
- AWS SQS queue named `pubsub-events`
- IAM user with `AmazonSQSFullAccess` permissions

### 1. SSH into EC2

```bash
ssh -i "your-key.pem" ubuntu@your-ec2-ip
```

### 2. Install Docker

```bash
sudo apt update
sudo apt install docker.io docker-compose -y

sudo systemctl start docker
sudo usermod -aG docker ubuntu
```

### 3. Clone the Repository

```bash
git clone https://github.com/your-username/pubsub-system.git
cd pubsub-system
```

### 4. Configure application.properties

```bash
nano src/main/resources/application.properties
```

### 5. Start the Application

```bash
sudo docker compose up --build
```

### 6. Access the Application

| Service | URL |
|----------|----------|
| API | http://your-ec2-ip:8081 |
| Dashboard | http://your-ec2-ip:8081/dashboard.html |
