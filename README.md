# HA Cluster Demo — College Project
**High Availability Cluster System using Spring Boot, Redis, PostgreSQL, Nginx, Docker**

---

## Project Structure

```
ha-cluster-demo/
├── src/
│   └── main/
│       ├── java/com/hacluster/
│       │   ├── HaClusterApplication.java       # Main entry point
│       │   ├── config/
│       │   │   ├── AppConfig.java              # Instance ID bean
│       │   │   ├── RedisConfig.java            # Redis session config
│       │   │   └── SecurityConfig.java         # Spring Security
│       │   ├── controller/
│       │   │   ├── HomeController.java         # Home, architecture, login pages
│       │   │   ├── DashboardController.java    # Dashboard (authenticated)
│       │   │   ├── ClusterNoteController.java  # CRUD notes
│       │   │   └── ApiController.java          # /api/health, /api/simulate
│       │   ├── model/
│       │   │   └── ClusterNote.java            # JPA entity
│       │   ├── repository/
│       │   │   └── ClusterNoteRepository.java
│       │   └── service/
│       │       └── ClusterNoteService.java
│       └── resources/
│           ├── application.yml                 # Main config
│           ├── static/
│           │   ├── css/main.css
│           │   └── js/main.js
│           └── templates/
│               ├── fragments/
│               │   └── layout.html             # Shared Thymeleaf layout
│               └── pages/
│                   ├── home.html
│                   ├── architecture.html
│                   ├── login.html
│                   ├── dashboard.html
│                   ├── cluster-status.html
│                   ├── monitor.html
│                   ├── simulate.html
│                   ├── failover.html
│                   ├── notes.html
│                   └── note-edit.html
├── nginx/
│   └── nginx.conf                              # Load balancer config
├── Dockerfile                                  # Multi-stage build
├── docker-compose.yml                          # Full stack orchestration
├── pom.xml
└── README.md
```

---

## Prerequisites

- **Java 17+**
- **Maven 3.9+**
- **Docker & Docker Compose** (v2)

---

## Run Locally (Two Nodes + Full Stack)

### Option 1 — Full Docker Compose (Recommended)

```bash
# 1. Build and start all 5 services
docker compose up --build

# 2. Open the app via Nginx load balancer
open http://localhost

# 3. Access Node 1 directly
open http://localhost:8080

# 4. Access Node 2 directly
open http://localhost:8081
```

### Login credentials
| Username | Password  | Role  |
|----------|-----------|-------|
| student  | demo123   | USER  |
| admin    | admin123  | ADMIN |

---

## Run Without Docker (Development)

### 1. Start Redis and PostgreSQL

```bash
# Redis
docker run -d -p 6379:6379 --name dev-redis redis:7.2-alpine

# PostgreSQL
docker run -d -p 5432:5432 \
  -e POSTGRES_DB=hacluster \
  -e POSTGRES_USER=hauser \
  -e POSTGRES_PASSWORD=hapassword \
  --name dev-postgres postgres:16-alpine
```

### 2. Run Node 1

```bash
cd ha-cluster-demo

INSTANCE_ID=node-1 SERVER_PORT=8080 mvn spring-boot:run
```

### 3. Run Node 2 (in a second terminal)

```bash
cd ha-cluster-demo

INSTANCE_ID=node-2 SERVER_PORT=8081 mvn spring-boot:run
```

Now open:
- Node 1: http://localhost:8080
- Node 2: http://localhost:8081

Login on Node 1, then open Node 2 — you will still be logged in (Redis session sharing).

---

## Failover Demonstration

```bash
# 1. Start the full cluster
docker compose up --build -d

# 2. Open the app at http://localhost — login as student
# 3. Go to the Failover page in the UI

# 4. Kill Node 1
docker stop ha-node-1

# 5. Refresh http://localhost — Nginx automatically routes to Node 2
#    You are STILL logged in because the session is in Redis

# 6. Restore Node 1
docker start ha-node-1

# 7. Within 30 seconds, Node 1 re-joins the cluster
```

---

## Health Check Endpoints

```bash
# Via Nginx (load balanced)
curl http://localhost/api/health

# Node 1 directly
curl http://localhost:8080/api/health

# Node 2 directly
curl http://localhost:8081/api/health

# Load simulation (returns which node handled it)
curl http://localhost/api/simulate

# Spring Actuator
curl http://localhost:8080/actuator/health
```

---

## Redis Inspection

```bash
# List all active sessions
docker exec ha-redis redis-cli keys "ha:session:*"

# Count sessions
docker exec ha-redis redis-cli dbsize

# Monitor live Redis commands
docker exec ha-redis redis-cli monitor
```

---

## Cloud Deployment (Docker on VPS)

### Step 1 — Provision a VPS
Use any provider: DigitalOcean, AWS EC2, Azure VM, Oracle Free Tier, etc.
Minimum: 2 vCPU, 2 GB RAM, Ubuntu 22.04.

### Step 2 — Install Docker on the VPS

```bash
# SSH into your VPS
ssh user@YOUR_SERVER_IP

# Install Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
newgrp docker

# Install Docker Compose plugin
sudo apt install -y docker-compose-plugin
```

### Step 3 — Copy the project to VPS

```bash
# From your local machine
scp -r ha-cluster-demo/ user@YOUR_SERVER_IP:/home/user/

# Or use git
git clone https://github.com/yourname/ha-cluster-demo.git
cd ha-cluster-demo
```

### Step 4 — Start the cluster

```bash
cd ha-cluster-demo
docker compose up --build -d

# Check all containers are running
docker compose ps
```

### Step 5 — Access via browser
Open `http://YOUR_SERVER_IP` — the Nginx load balancer is on port 80.

### Step 6 — Optional: Add a domain + HTTPS with Certbot

```bash
# Install certbot
sudo apt install -y certbot python3-certbot-nginx

# Get certificate
sudo certbot --nginx -d yourdomain.com

# Auto-renew
sudo certbot renew --dry-run
```

---

## Stopping the Cluster

```bash
# Stop all containers
docker compose down

# Stop and remove volumes (wipe database + sessions)
docker compose down -v
```

---

## How High Availability Is Achieved

| Feature | Implementation |
|---------|---------------|
| **Multiple nodes** | Two identical Spring Boot containers (node-1, node-2) run in parallel |
| **Load balancing** | Nginx distributes requests round-robin across both nodes |
| **Stateless design** | Spring Boot stores NO state locally — every request is independent |
| **Session sharing** | Spring Session + Redis stores all user sessions centrally |
| **Failover** | Nginx detects failed nodes via health checks and removes them from rotation automatically |
| **Persistence** | PostgreSQL is a shared database — both nodes read/write to the same data |
| **Health checks** | `/api/health` endpoint exposes instance ID, heap usage, and uptime |
| **Recovery** | When a node restarts, Nginx re-adds it to rotation after health checks pass |
| **Containerisation** | Docker isolates each component; docker compose orchestrates the full stack |

### Session Sharing Flow

```
User logs in → Node 1 → Writes session to Redis (key: ha:session:ABC)
Next request → Nginx → Routes to Node 2 → Node 2 reads ha:session:ABC from Redis
Result: User is still authenticated ✓
```

### Failover Flow

```
Node 1 crashes
    → Nginx health check fails 3 times (45 seconds)
    → Nginx marks Node 1 as DOWN
    → All traffic goes to Node 2
    → Session still in Redis → users unaffected
Node 1 restarts
    → Health check passes
    → Nginx re-adds Node 1 to rotation
```

---

## Pages Summary

| URL | Description | Auth Required |
|-----|-------------|---------------|
| `/` | Home — concept overview | No |
| `/architecture` | System architecture diagram | No |
| `/cluster-status` | Live node health poll | No |
| `/simulate` | Load balancing simulator | No |
| `/failover` | Failover demo + commands | No |
| `/login` | Login form | No |
| `/dashboard` | User dashboard with metrics | Yes |
| `/monitor` | Live JVM metrics chart | Yes |
| `/notes` | Cluster notes CRUD | Yes |
| `/api/health` | JSON health endpoint | No |
| `/api/simulate` | JSON simulate endpoint | No |
| `/actuator/health` | Spring Actuator health | No |
