# Running on Port 8081 - Quick Reference

## ✅ Changes Made

I've updated your project to run on **port 8081** instead of 8080:

1. ✅ **application.yml** - Changed server port to 8081
2. ✅ **postman_collection.json** - Updated all URLs to use port 8081

---

## 🚀 How to Run

### Step 1: Make sure Docker is running
```powershell
# Check Docker Desktop is running
docker ps
```

### Step 2: Start PostgreSQL and Redis
```powershell
docker-compose up -d
```

### Step 3: Run the Application
```powershell
mvn spring-boot:run
```

**✅ The application will now start on http://localhost:8081**

---

## 🧪 Testing

### Using Postman
1. **Import** the updated `postman_collection.json`
2. All requests are now configured for **port 8081**
3. Click "Send" on any request

### Using cURL
```powershell
# Create a post
curl -X POST http://localhost:8081/api/posts -H "Content-Type: application/json" -d "{\"authorId\": 1, \"content\": \"Hello World!\"}"

# Like a post
curl -X POST http://localhost:8081/api/posts/1/like -H "Content-Type: application/json" -d "{\"userId\": 2}"

# Add a comment
curl -X POST http://localhost:8081/api/posts/1/comments -H "Content-Type: application/json" -d "{\"authorId\": 2, \"content\": \"Great!\", \"depthLevel\": 1, \"isBot\": false, \"parentCommentId\": null}"
```

### Using Browser
Open: http://localhost:8081/api/posts

(You'll get a 405 error because it's a POST endpoint, but this confirms the server is running)

---

## 📝 Important URLs

- **Application**: http://localhost:8081
- **API Base**: http://localhost:8081/api
- **Create Post**: http://localhost:8081/api/posts
- **Add Comment**: http://localhost:8081/api/posts/{postId}/comments
- **Like Post**: http://localhost:8081/api/posts/{postId}/like

---

## 🔄 If You Want to Switch Back to Port 8080

1. **Stop the application** (Ctrl+C)
2. **Kill the process using port 8080**:
   ```powershell
   taskkill /PID 4736 /F
   ```
3. **Edit application.yml**:
   ```yaml
   server:
     port: 8080
   ```
4. **Update Postman collection** (or re-import the original)
5. **Restart**: `mvn spring-boot:run`

---

## ✅ Verification

After starting the application, you should see:

```
Started SocialApiApplication in X seconds (JVM running for Y)
```

And the last line should show:
```
Tomcat started on port(s): 8081 (http)
```

---

## 🎯 Next Steps

1. ✅ Application is running on port 8081
2. ✅ Postman collection is updated
3. ✅ Ready to test!

**Now run**: `mvn spring-boot:run`

Then test with Postman or cURL!
