# Complete Setup Guide - Social API

This guide will walk you through setting up and running the Social API project on Windows.

## Prerequisites Installation

### 1. Install Java 17 or Higher

**Check if Java is installed:**
```bash
java -version
```

**If not installed, download and install:**
1. Go to https://adoptium.net/
2. Download Java 17 (LTS) for Windows
3. Run the installer
4. Verify installation: `java -version`

### 2. Install Maven

**Check if Maven is installed:**
```bash
mvn -version
```

**If not installed:**
1. Go to https://maven.apache.org/download.cgi
2. Download the Binary zip archive (e.g., apache-maven-3.9.5-bin.zip)
3. Extract to `C:\Program Files\Maven`
4. Add to PATH:
   - Open System Properties → Environment Variables
   - Add `C:\Program Files\Maven\bin` to PATH
5. Verify: `mvn -version`

### 3. Install Docker Desktop

**Download and install:**
1. Go to https://www.docker.com/products/docker-desktop
2. Download Docker Desktop for Windows
3. Run the installer
4. Restart your computer
5. Start Docker Desktop
6. Verify: `docker --version` and `docker-compose --version`

### 4. Install Git (Optional, for version control)

**Download and install:**
1. Go to https://git-scm.com/download/win
2. Download and install Git for Windows
3. Verify: `git --version`

### 5. Install Postman (For API Testing)

**Download and install:**
1. Go to https://www.postman.com/downloads/
2. Download Postman for Windows
3. Install and create a free account (optional)

---

## Project Setup

### Step 1: Navigate to Project Directory

Open PowerShell or Command Prompt and navigate to your project folder:

```bash
cd path\to\social-api
```

### Step 2: Verify Project Structure

Make sure you have these files:
```
social-api/
├── src/
├── pom.xml
├── docker-compose.yml
├── README.md
└── postman_collection.json
```

---

## Running the Project

### Step 1: Start Docker Desktop

1. Open Docker Desktop application
2. Wait until it shows "Docker Desktop is running"
3. You should see the Docker icon in the system tray

### Step 2: Start PostgreSQL and Redis

Open PowerShell/Command Prompt in the project directory:

```bash
# Start the containers
docker-compose up -d
```

**Expected output:**
```
Creating network "social-api_social-api-network" with driver "bridge"
Creating social-api-postgres ... done
Creating social-api-redis    ... done
```

**Verify containers are running:**
```bash
docker ps
```

You should see two containers:
- `social-api-postgres` (port 5432)
- `social-api-redis` (port 6379)

**Troubleshooting:**
- If ports are already in use, stop other services using those ports
- Check Docker Desktop logs if containers fail to start

### Step 3: Build the Spring Boot Application

```bash
# Clean and build the project
mvn clean install
```

**Expected output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 30 s
```

**If build fails:**
- Check Java version: `java -version` (should be 17+)
- Check Maven version: `mvn -version`
- Check internet connection (Maven downloads dependencies)

### Step 4: Run the Spring Boot Application

```bash
# Start the application
mvn spring-boot:run
```

**Expected output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

...
Started SocialApiApplication in 5.234 seconds
```

**The application is now running on http://localhost:8080**

**Keep this terminal window open!**

---

## Testing the API

### Method 1: Using Postman (Recommended)

#### Step 1: Import Collection

1. Open Postman
2. Click "Import" button (top left)
3. Select "File" tab
4. Browse to `postman_collection.json` in your project folder
5. Click "Import"

#### Step 2: Test Endpoints

**Test 1: Create a Post**
1. In Postman, select "Posts" → "Create Post"
2. Click "Send"
3. You should get HTTP 201 with the created post

**Test 2: Like a Post**
1. Select "Posts" → "Like Post"
2. Make sure the URL has the correct post ID (e.g., `/api/posts/1/like`)
3. Click "Send"
4. You should get HTTP 200

**Test 3: Add a Comment**
1. Select "Comments" → "Add Human Comment"
2. Click "Send"
3. You should get HTTP 201 with the created comment

**Test 4: Test Bot Comment**
1. Select "Comments" → "Add Bot Comment"
2. Click "Send"
3. You should get HTTP 201

**Test 5: Test Cooldown (Should Fail)**
1. Select "Guardrail Tests" → "Test Cooldown - Same Bot Same Human"
2. Click "Send" immediately after the previous bot comment
3. You should get HTTP 429 (Too Many Requests)

### Method 2: Using cURL (Command Line)

Open a **new** PowerShell/Command Prompt window:

**Create a Post:**
```bash
curl -X POST http://localhost:8080/api/posts `
  -H "Content-Type: application/json" `
  -d '{\"authorId\": 1, \"content\": \"My first post!\"}'
```

**Like a Post:**
```bash
curl -X POST http://localhost:8080/api/posts/1/like `
  -H "Content-Type: application/json" `
  -d '{\"userId\": 2}'
```

**Add a Comment:**
```bash
curl -X POST http://localhost:8080/api/posts/1/comments `
  -H "Content-Type: application/json" `
  -d '{\"authorId\": 2, \"content\": \"Great post!\", \"depthLevel\": 1, \"isBot\": false, \"parentCommentId\": null}'
```

---

## Monitoring and Verification

### Check Application Logs

In the terminal where Spring Boot is running, you should see:
- Request logs
- Virality score updates
- Notification logs
- Guardrail violations (if any)

### Check Redis Data

Open a new terminal:

```bash
# Connect to Redis CLI
docker exec -it social-api-redis redis-cli

# Inside Redis CLI:
# Check all keys
KEYS *

# Check bot count for post 1
GET post:1:bot_count

# Check virality score
GET post:1:virality_score

# Check cooldown keys
KEYS cooldown:*

# Check pending notifications
LRANGE user:1:pending_notifs 0 -1

# Exit Redis CLI
exit
```

### Check PostgreSQL Data

```bash
# Connect to PostgreSQL
docker exec -it social-api-postgres psql -U postgres -d socialdb

# Inside PostgreSQL:
# View all posts
SELECT * FROM posts;

# View all comments
SELECT * FROM comments;

# View all users
SELECT * FROM users;

# View all bots
SELECT * FROM bots;

# Count comments per post
SELECT post_id, COUNT(*) FROM comments GROUP BY post_id;

# Exit PostgreSQL
\q
```

---

## Testing Concurrency (Race Condition Test)

### Option 1: Using Apache Bench (Recommended)

**Install Apache Bench:**
```bash
# Using Chocolatey (if installed)
choco install apache-httpd

# Or download from https://httpd.apache.org/download.cgi
```

**Create test file:**
Create a file named `comment.json`:
```json
{"authorId": 100, "content": "Test", "depthLevel": 1, "isBot": true, "parentCommentId": null}
```

**Run test:**
```bash
ab -n 200 -c 200 -p comment.json -T application/json http://localhost:8080/api/posts/1/comments
```

**Expected result:**
- Exactly 100 requests succeed (HTTP 201)
- Exactly 100 requests fail (HTTP 429)

### Option 2: Using Postman Runner

1. In Postman, select "Comments" → "Add Bot Comment"
2. Click "Runner" button (top right)
3. Select the collection
4. Set "Iterations" to 150
5. Click "Run"
6. First 100 should succeed, rest should fail

### Option 3: Using Python Script

Create a file named `test_concurrency.py`:
```python
import requests
import concurrent.futures
import json

url = "http://localhost:8080/api/posts/1/comments"
headers = {"Content-Type": "application/json"}

def make_request(i):
    data = {
        "authorId": 100 + i,  # Different bots to avoid cooldown
        "content": f"Concurrent test {i}",
        "depthLevel": 1,
        "isBot": True,
        "parentCommentId": None
    }
    try:
        response = requests.post(url, json=data, headers=headers)
        return response.status_code
    except Exception as e:
        return 0

with concurrent.futures.ThreadPoolExecutor(max_workers=200) as executor:
    results = list(executor.map(make_request, range(200)))

success = results.count(201)
failed = results.count(429)

print(f"Success: {success}, Failed: {failed}")
print(f"Expected: Success: 100, Failed: 100")
```

Run:
```bash
python test_concurrency.py
```

---

## Stopping the Application

### Stop Spring Boot Application
In the terminal where Spring Boot is running:
- Press `Ctrl + C`

### Stop Docker Containers
```bash
# Stop containers
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

---

## Restarting the Application

### Quick Restart (Keep Data)
```bash
# Start containers
docker-compose up -d

# Start Spring Boot
mvn spring-boot:run
```

### Clean Restart (Fresh Data)
```bash
# Stop and remove everything
docker-compose down -v

# Start containers
docker-compose up -d

# Start Spring Boot
mvn spring-boot:run
```

---

## Common Issues and Solutions

### Issue 1: Port Already in Use

**Error:** `Port 8080 is already in use`

**Solution:**
```bash
# Find process using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID with actual process ID)
taskkill /PID <PID> /F

# Or change port in application.yml:
server:
  port: 8081
```

### Issue 2: Docker Containers Not Starting

**Error:** `Cannot connect to Docker daemon`

**Solution:**
1. Open Docker Desktop
2. Wait until it's fully started
3. Try again: `docker-compose up -d`

### Issue 3: PostgreSQL Connection Failed

**Error:** `Connection refused: localhost:5432`

**Solution:**
```bash
# Check if container is running
docker ps

# Check container logs
docker logs social-api-postgres

# Restart container
docker-compose restart postgres
```

### Issue 4: Redis Connection Failed

**Error:** `Cannot connect to Redis at localhost:6379`

**Solution:**
```bash
# Check if container is running
docker ps

# Check container logs
docker logs social-api-redis

# Test Redis connection
docker exec -it social-api-redis redis-cli ping
# Should return: PONG
```

### Issue 5: Maven Build Fails

**Error:** `Failed to execute goal`

**Solution:**
```bash
# Clean Maven cache
mvn clean

# Delete target folder
rm -rf target

# Try again
mvn clean install
```

### Issue 6: Java Version Mismatch

**Error:** `Unsupported class file major version`

**Solution:**
```bash
# Check Java version
java -version

# Should be 17 or higher
# If not, install Java 17 and set JAVA_HOME
```

---

## Verification Checklist

After starting the application, verify:

- ✅ Docker Desktop is running
- ✅ PostgreSQL container is running (`docker ps`)
- ✅ Redis container is running (`docker ps`)
- ✅ Spring Boot application started successfully
- ✅ Application accessible at http://localhost:8080
- ✅ Can create posts via Postman
- ✅ Can add comments via Postman
- ✅ Can like posts via Postman
- ✅ Redis keys are being created
- ✅ PostgreSQL data is being saved
- ✅ Guardrails are working (test cooldown)
- ✅ Notifications are being logged

---

## Next Steps

1. **Read TESTING_GUIDE.md** for comprehensive testing scenarios
2. **Read VERIFICATION_CHECKLIST.md** to verify all features
3. **Test concurrency** with 200 concurrent requests
4. **Monitor Redis** to see guardrails in action
5. **Check logs** to see notification batching

---

## Quick Reference

### Start Everything
```bash
docker-compose up -d
mvn spring-boot:run
```

### Stop Everything
```bash
# Ctrl+C in Spring Boot terminal
docker-compose down
```

### Check Status
```bash
docker ps                    # Check containers
curl http://localhost:8080   # Check API (should return 404, but means it's running)
```

### View Logs
```bash
# Docker logs
docker logs social-api-postgres
docker logs social-api-redis

# Spring Boot logs are in the terminal
```

---

## Support

If you encounter any issues:
1. Check this guide's "Common Issues" section
2. Check Docker Desktop logs
3. Check Spring Boot application logs
4. Verify all prerequisites are installed correctly
5. Try a clean restart with `docker-compose down -v`

---

**You're all set! The application is ready to test.** 🚀
