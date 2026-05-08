# How to Run This Project - Complete Guide

## 🚀 Quick Start (If Everything is Already Set Up)

### Step 1: Start Docker Desktop
1. Open **Docker Desktop** application
2. Wait until it shows "Docker Desktop is running"

### Step 2: Start Infrastructure (PostgreSQL & Redis)
```powershell
cd C:\Users\vansh\Desktop\PROJECT_ASSIGNMENT
docker-compose up -d
```

**Expected output:**
```
Creating social-api-postgres ... done
Creating social-api-redis    ... done
```

### Step 3: Run the Application
```powershell
mvn spring-boot:run
```

**Expected output:**
```
Started SocialApiApplication in X seconds
Tomcat started on port(s): 8081 (http)
```

**✅ Your application is now running on http://localhost:8081**

---

## 🧪 Test the Application

### Option 1: Use the Test Script
Open a **NEW** PowerShell window:
```powershell
cd C:\Users\vansh\Desktop\PROJECT_ASSIGNMENT
.\test-api.ps1
```

**Expected:**
```
🎉 ALL TESTS PASSED!
Your API is working perfectly!
```

### Option 2: Use Postman
1. Open Postman
2. Import `postman_collection.json`
3. Send requests from the collection

### Option 3: Use PowerShell Commands
```powershell
# Create a post
Invoke-RestMethod -Uri "http://localhost:8081/api/posts" -Method POST -ContentType "application/json" -Body '{"authorId": 1, "content": "Hello World!"}'

# Like a post
Invoke-RestMethod -Uri "http://localhost:8081/api/posts/1/like" -Method POST -ContentType "application/json" -Body '{"userId": 2}'

# Add a comment
Invoke-RestMethod -Uri "http://localhost:8081/api/posts/1/comments" -Method POST -ContentType "application/json" -Body '{"authorId": 2, "content": "Great!", "depthLevel": 1, "isBot": false, "parentCommentId": null}'
```

---

## 🛑 Stop the Application

### Stop Spring Boot
In the terminal where `mvn spring-boot:run` is running:
- Press **Ctrl + C**

### Stop Docker Containers
```powershell
docker-compose down
```

---

## 🔄 Restart the Application

### Quick Restart (Keep Data)
```powershell
# Start Docker containers
docker-compose up -d

# Start Spring Boot
mvn spring-boot:run
```

### Clean Restart (Fresh Data)
```powershell
# Stop and remove everything
docker-compose down -v

# Start containers
docker-compose up -d

# Start Spring Boot
mvn spring-boot:run
```

---

## ✅ Verify Everything is Working

### Check Docker Containers
```powershell
docker ps
```
**Expected:** 2 containers running (postgres and redis)

### Check Application Logs
Look at the terminal where Spring Boot is running:
- Should show "Started SocialApiApplication"
- Should show "Tomcat started on port(s): 8081"

### Check Redis Data
```powershell
docker exec -it social-api-redis redis-cli KEYS "*"
```

### Check PostgreSQL Data
```powershell
docker exec -it social-api-postgres psql -U postgres -d socialdb -c "SELECT * FROM users;"
```

---

## 🎯 Complete Workflow

### First Time Setup (One Time Only)
1. ✅ Install Java 17 (already done)
2. ✅ Install Maven (already done)
3. ✅ Install Docker Desktop (already done)
4. ✅ Build project: `mvn clean install` (already done)

### Every Time You Want to Run
1. **Start Docker Desktop**
2. **Start containers:** `docker-compose up -d`
3. **Run application:** `mvn spring-boot:run`
4. **Test:** `.\test-api.ps1` or use Postman

### When You're Done
1. **Stop application:** Ctrl + C
2. **Stop containers:** `docker-compose down`

---

## 📊 What's Running

When the project is running, you have:

| Component | Port | Purpose |
|-----------|------|---------|
| Spring Boot API | 8081 | REST API endpoints |
| PostgreSQL | 5432 | Database (persistent storage) |
| Redis | 6379 | Cache (guardrails, virality scores) |

---

## 🔍 Monitoring

### View Application Logs
The terminal where `mvn spring-boot:run` is running shows:
- API requests
- Database queries
- Redis operations
- Errors and warnings

### View Docker Logs
```powershell
# PostgreSQL logs
docker logs social-api-postgres

# Redis logs
docker logs social-api-redis
```

---

## 🧪 Testing Features

### Test Virality Engine
```powershell
# Create post
Invoke-RestMethod -Uri "http://localhost:8081/api/posts" -Method POST -ContentType "application/json" -Body '{"authorId": 1, "content": "Test"}'

# Like it (+20 points)
Invoke-RestMethod -Uri "http://localhost:8081/api/posts/1/like" -Method POST -ContentType "application/json" -Body '{"userId": 2}'

# Comment on it (+50 points)
Invoke-RestMethod -Uri "http://localhost:8081/api/posts/1/comments" -Method POST -ContentType "application/json" -Body '{"authorId": 2, "content": "Nice!", "depthLevel": 1, "isBot": false, "parentCommentId": null}'

# Check score (should be 70)
docker exec -it social-api-redis redis-cli GET post:1:virality_score
```

### Test Bot Cooldown
```powershell
# Bot comments (should succeed)
Invoke-RestMethod -Uri "http://localhost:8081/api/posts/1/comments" -Method POST -ContentType "application/json" -Body '{"authorId": 100, "content": "Bot 1", "depthLevel": 1, "isBot": true, "parentCommentId": null}'

# Same bot tries again immediately (should FAIL with 429)
Invoke-RestMethod -Uri "http://localhost:8081/api/posts/1/comments" -Method POST -ContentType "application/json" -Body '{"authorId": 100, "content": "Bot 2", "depthLevel": 1, "isBot": true, "parentCommentId": null}'
```

### Test Vertical Cap
```powershell
# Try depth 21 (should FAIL)
Invoke-RestMethod -Uri "http://localhost:8081/api/posts/1/comments" -Method POST -ContentType "application/json" -Body '{"authorId": 2, "content": "Too deep", "depthLevel": 21, "isBot": false, "parentCommentId": null}'
```

---

## 🐛 Troubleshooting

### Application won't start - Port 8081 in use
```powershell
# Find process using port 8081
netstat -ano | Select-String ":8081"

# Kill the process (replace PID)
taskkill /PID <PID> /F
```

### Docker containers won't start
```powershell
# Check Docker Desktop is running
docker ps

# Restart Docker Desktop
# Or restart containers
docker-compose down
docker-compose up -d
```

### Build fails
```powershell
# Clean and rebuild
mvn clean
mvn clean install
```

### Database connection error
```powershell
# Check PostgreSQL is running
docker ps | Select-String postgres

# Restart PostgreSQL
docker-compose restart postgres
```

### Redis connection error
```powershell
# Check Redis is running
docker ps | Select-String redis

# Restart Redis
docker-compose restart redis
```

---

## 📚 Additional Resources

- **README.md** - Complete project documentation
- **TESTING_GUIDE.md** - Detailed testing scenarios
- **VERIFICATION_CHECKLIST.md** - All PDF requirements
- **PORT_8081_SETUP.md** - Port configuration
- **test-api.ps1** - Automated testing script

---

## 🎯 Quick Commands Reference

```powershell
# Start everything
docker-compose up -d
mvn spring-boot:run

# Test
.\test-api.ps1

# Check status
docker ps
docker exec -it social-api-redis redis-cli KEYS "*"

# Stop everything
# Ctrl+C (in Spring Boot terminal)
docker-compose down
```

---

## ✅ Success Indicators

You know it's working when:
- ✅ Docker shows 2 containers running
- ✅ Spring Boot logs show "Started SocialApiApplication"
- ✅ Test script shows "ALL TESTS PASSED"
- ✅ Postman requests return HTTP 201/200
- ✅ Redis has keys (check with `KEYS *`)
- ✅ PostgreSQL has data (check with `SELECT * FROM posts`)

---

**Your project is ready to run!** 🚀

Just follow the "Quick Start" section at the top of this file.
