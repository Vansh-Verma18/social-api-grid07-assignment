# Quick Start - 5 Minutes to Running

Follow these steps to get the Social API running quickly.

## ✅ Prerequisites Check

Before starting, make sure you have:

```bash
# Check Java (need 17+)
java -version

# Check Maven
mvn -version

# Check Docker
docker --version
docker-compose --version
```

**Don't have them?** See SETUP_GUIDE.md for installation instructions.

---

## 🚀 Start in 3 Steps

### Step 1: Start Infrastructure (30 seconds)

```bash
# Make sure Docker Desktop is running, then:
docker-compose up -d
```

Wait for:
```
✅ Creating social-api-postgres ... done
✅ Creating social-api-redis    ... done
```

### Step 2: Build & Run Application (1-2 minutes)

```bash
# Build the project
mvn clean install

# Start the application
mvn spring-boot:run
```

Wait for:
```
✅ Started SocialApiApplication in X seconds
```

**Keep this terminal open!**

### Step 3: Test It Works (30 seconds)

Open a **new terminal** and run:

```bash
# Create a post
curl -X POST http://localhost:8080/api/posts -H "Content-Type: application/json" -d "{\"authorId\": 1, \"content\": \"Hello World!\"}"
```

**Expected:** You should see a JSON response with the created post.

---

## 🎯 Quick Test with Postman

1. **Open Postman**
2. **Import** → Select `postman_collection.json`
3. **Select** "Posts" → "Create Post"
4. **Click** "Send"
5. **Result:** HTTP 201 ✅

---

## 📊 Verify Everything Works

### Check Redis
```bash
docker exec -it social-api-redis redis-cli KEYS "*"
```

### Check PostgreSQL
```bash
docker exec -it social-api-postgres psql -U postgres -d socialdb -c "SELECT * FROM posts;"
```

### Check Application Logs
Look at the terminal where Spring Boot is running - you should see request logs.

---

## 🧪 Test Key Features

### Test 1: Create Post and Like It
```bash
# Create post
curl -X POST http://localhost:8080/api/posts -H "Content-Type: application/json" -d "{\"authorId\": 1, \"content\": \"Test post\"}"

# Like post (use ID from response, e.g., 1)
curl -X POST http://localhost:8080/api/posts/1/like -H "Content-Type: application/json" -d "{\"userId\": 2}"
```

### Test 2: Add Bot Comment
```bash
curl -X POST http://localhost:8080/api/posts/1/comments -H "Content-Type: application/json" -d "{\"authorId\": 100, \"content\": \"Bot comment\", \"depthLevel\": 1, \"isBot\": true, \"parentCommentId\": null}"
```

### Test 3: Test Cooldown (Should Fail)
```bash
# Try same bot again immediately - should get HTTP 429
curl -X POST http://localhost:8080/api/posts/1/comments -H "Content-Type: application/json" -d "{\"authorId\": 100, \"content\": \"Another comment\", \"depthLevel\": 1, \"isBot\": true, \"parentCommentId\": null}"
```

**Expected:** Error message about cooldown active ✅

---

## 🛑 Stop Everything

```bash
# Stop Spring Boot: Ctrl+C in the terminal

# Stop Docker containers
docker-compose down
```

---

## 📚 Next Steps

- **Full Testing:** Read TESTING_GUIDE.md
- **Detailed Setup:** Read SETUP_GUIDE.md
- **Verify Features:** Read VERIFICATION_CHECKLIST.md
- **API Documentation:** Read README.md

---

## ⚠️ Troubleshooting

### Application won't start?
```bash
# Check if ports are free
netstat -ano | findstr :8080
netstat -ano | findstr :5432
netstat -ano | findstr :6379
```

### Docker containers won't start?
1. Open Docker Desktop
2. Make sure it's running (green icon)
3. Try: `docker-compose down` then `docker-compose up -d`

### Build fails?
```bash
# Clean and try again
mvn clean
mvn clean install
```

### Can't connect to database?
```bash
# Check containers are running
docker ps

# Should see both postgres and redis containers
```

---

## 🎉 Success Indicators

You know it's working when:

✅ Docker shows 2 containers running
✅ Spring Boot logs show "Started SocialApiApplication"
✅ POST request to create post returns HTTP 201
✅ Redis has keys (check with `docker exec -it social-api-redis redis-cli KEYS "*"`)
✅ PostgreSQL has data (check with psql command above)
✅ Postman requests succeed

---

**That's it! You're ready to test the full application.** 🚀

For detailed testing scenarios, see **TESTING_GUIDE.md**
