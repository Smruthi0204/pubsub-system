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
- Containerized using Docker and Docker Compose
- Deployed on AWS EC2 cloud infrastructure

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

## Database Design

4 normalized tables are designed to track every event and every delivery attempt independently.

| Table | Purpose |
|---------|---------|
| `publisher` | Stores registered publisher identities |
| `subscriber` | Stores subscriber details including delivery preference and endpoint |
| `subscriber_topics` | Separate join table — one subscriber can subscribe to multiple topics |
| `event` | Every published event with topic, payload, publisher reference and timestamp |
| `delivery_tracking` | One row per subscriber per event — tracks status, retry count and attempt time |

The `delivery_tracking` table is the core of observability in this system. It answers:

- Who received what
- When it was delivered
- How many attempts it took
- Whether it ultimately succeeded or failed

All of this is queryable with simple SQL.

---

## Event Flow

**1. Publisher Publishes an Event**
- Publisher sends `POST /api/events` with topic, payload, and publisher ID
- Event is persisted to the `event` table in PostgreSQL
- Event is serialized to JSON and pushed to AWS SQS

**2. Event Queued in AWS SQS**
- Message is durably stored in SQS — survives consumer downtime
- Publisher is immediately free — no blocking, no waiting for delivery

**3. Consumer Picks Up the Event**
- `@SqsListener` continuously polls the SQS queue
- Incoming message is deserialized from JSON back to an `Event` object
- SQS applies a visibility timeout — message is hidden from other consumers during processing

**4. Subscriber Resolution**
- `subscriber_topics` table is queried to find all subscribers matching the event topic
- A single event can fan out to multiple subscribers
- Each subscriber is processed independently

**5. Delivery by Channel**
- `webhook` → HTTP POST to the subscriber's registered URL via `RestTemplate`
- `email` → Notification sent via Gmail SMTP using `JavaMailSender`
- `websocket` → Payload pushed directly through the subscriber's active `WebSocketSession`

**6. Retry Logic**
- Each delivery is attempted up to 3 times on failure
- A 2-second delay is applied between retry attempts
- After 3 consecutive failures, status is set to `Permanently Failed` and no further attempts are made

**7. Delivery Tracking**
- A record is written to the `delivery_tracking` table after every attempt — success or failure
- Captures event reference, subscriber reference, delivery status, retry count, and timestamp
- Provides a complete audit trail of every delivery in the system

**8. Dashboard Visibility**
- All delivery statuses are aggregated and exposed at `GET /dashboard.html`
- Displays total delivered, failed, and permanently failed counts in real time
- System health is visible at a glance — no database access required

**9. SQS Acknowledgement**
- On successful processing → SQS automatically deletes the message from the queue
- On unhandled exception → message becomes visible again after visibility timeout and is retried by SQS

---

## Deployment

- Containerized using Docker with a multi-stage build
- PostgreSQL and application services orchestrated using Docker Compose
- Deployed on AWS EC2 (Ubuntu)
- AWS SQS used as the managed message broker

---

## Design Decisions

**Why AWS SQS over an in-memory queue?**
An in-memory queue loses all pending events if the application crashes. SQS persists messages durably — nothing is lost regardless of consumer state. It also fully decouples the publisher from the consumer, provides built-in retry via visibility timeout, and scales independently on both sides without any custom infrastructure.

**Why three delivery channels?**
Each channel serves a fundamentally different type of subscriber. Webhook targets backend servers that expose a URL. Email targets humans who want inbox notifications. WebSocket targets live browser or mobile clients that have no URL — they need the server to push through a persistent connection. Supporting all three makes the system useful across real-world integration scenarios.

**Why a separate delivery_tracking table?**
One event can be delivered to many subscribers, each with its own status, retry count, and timestamp. Storing this on the event itself would require complex nested structures. A separate normalized table keeps the schema clean and makes querying straightforward — failed deliveries, retry history, per-subscriber audit trails — all simple SQL.

---

## Future Improvements

- **Dead Letter Queue (DLQ)** — Route permanently failed messages to a dedicated SQS Dead Letter Queue for inspection and reprocessing instead of discarding them after the final retry attempt.

- **JWT Authentication** — Secure publisher and subscriber APIs with JWT-based authentication and authorization to prevent unauthorized event publishing and subscription registration.

- **Rate Limiting** — Apply per-publisher rate limits to protect the system from abuse, prevent queue flooding, and ensure fair resource usage.

- **Payload Filtering** — Extend topic-based subscriptions with payload-based filtering, allowing subscribers to receive only events matching specific criteria.

---
