# Operant - Full Stack Multi-Tenant SaaS Platform

Operant is a sophisticated, production-ready SaaS (Software as a Service) platform designed to demonstrate a modern, scalable approach to multi-tenancy. Built with a focus on high-concurrency and event-driven architecture, it allows multiple organizations (tenants) to manage automated workflows within a secure, isolated environment.

The project showcases a complex integration of Spring Boot 3, Angular, and Google Cloud Platform, featuring an AI-driven agent powered by Google Gemini to process workflow events in real-time. Whether it's provisioning new tenants, handling event streams via Kafka, or orchestrating containerized workloads on Kubernetes, Operant represents a complete end-to-end engineering solution.

## ✨ Key Features
- **Logical Multi-Tenancy:** Secure data isolation across tenants using a shared-database approach with tenant-specific identifiers.

- **AI-Powered Automation:** Integrated Google Gemini to analyze and execute complex workflow steps based on natural language instructions.

- **Event-Driven Core:** Utilizes Apache Kafka to decouple workflow events from the main application logic for high scalability.

- **Hybrid API Layer:** Offers both RESTful endpoints for traditional consumption and GraphQL for flexible, client-driven data fetching.

- **Enterprise Security:** Robust security layer using Spring Security with OAuth2/OpenID Connect support.

---

## 🏗 Project Architecture

* **/backend**: Java 17 + Spring Boot 3, Maven, and Docker.
* **/frontend**: Angular 17+ with a responsive UI.
* **/k8s**: Kubernetes manifests for GKE deployment (Workloads, Services, Secrets).
* **/.github**: Automated CI/CD pipelines via GitHub Actions.

--- 
## 🛠 Tech Stack

### Backend
- **Framework:** Spring Boot 3 (Java 17)
- **Database:** PostgreSQL 16
- **Messaging:** Apache Kafka (Confluent)
- **Security:** Spring Security & OAuth2
- **API:** REST & GraphQL (Spring for GraphQL)
- **AI Integration:** Google Gemini

### Frontend
- **Framework:** Angular
- **Styling:** SCSS / Tailwind CSS
- **State Management:** RxJS

### Infrastructure & DevOps
- **Containerization:** Docker & Docker Compose
- **Orchestration:** Google Kubernetes Engine (GKE)
- **CI/CD:** GitHub Actions (Automated Test -> Build -> Push)
- **Registry:** Google Container Registry (GCR)

---

## 🚀 Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 17+
- Node.js & Angular CLI

### Local Development (Docker Compose)
To spin up the entire infrastructure (DB, Kafka, and Backend) locally:

1. Clone the repository.
2. Copy this into a file named ```.env``` in your project root.
    ``` bash
    # --- DATABASE CONFIG ---
    DB_URL=jdbc:postgresql://postgres:5432/operant_db
    DB_USER=operant_user
    DB_PASSWORD=operant_password

    # --- KAFKA CONFIG ---
    SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092

    # --- SECURITY & OAUTH ---
    OAUTH_CLIENT_ID=your_google_oauth_id
    OAUTH_CLIENT_SECRET=your_google_oauth_secret

    # --- EXTERNAL APIS ---
    GEMINI_API_KEY=your_gemini_api_key_here

    # --- MAIL SERVER ---
    SMTP_USER=your_email@example.com
    SMTP_PASSWORD=your_app_specific_password
    ```
3. Run the following command:
   ```bash
   docker-compose up --build
   ```
Access the backend at ```http://localhost:8080```

### Running the Frontend
1. Navigate to the frontend directory:

    ```Bash
    cd frontend
    ```
2. Install dependencies:

    ```Bash
    npm install
    ```
3. Start the dev server:

    ```Bash
    ng serve
    ```
Access the UI at ```http://localhost:4200```

### 🤖 CI/CD Pipeline
The project features a fully automated pipeline:

1. **Test:** Runs Maven unit tests on every push.

2. **Build:** Packages the application into a Docker image.

3. **Push:** Uploads the image to Google Container Registry (GCR).

4. **Deploy:** Deploys the updated image to the GKE cluster.
