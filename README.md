# Social API - Spring Boot Microservice with Redis Guardrails

A robust, high-performance Spring Boot microservice that implements a social media API with Redis-based guardrails for bot interaction management, virality scoring, and smart notification batching.

## 🚀 Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **PostgreSQL 15** (persistent data storage)
- **Redis 7** (distributed state management)
- **Spring Data JPA** (ORM)
- **Spring Data Redis** (Redis integration)
- **Maven** (build tool)

## 📋 Features

### Phase 1: Core API & Database
- RESTful endpoints for posts, comments, and likes
- JPA entities with proper relationships
- PostgreSQL as source of truth

### Phase 2: Redis Virality Engine & Atomic Locks
- **Virality Score Calculation**:
  - Bot Reply: +1 point
  - Human Like: +20 points
  - Human Comment: +50 points

- **Atomic Guardrails** (Thread-Safe):
  - **Horizontal Cap**: Max 100 bot replies per post
  - **Vertical Cap**: Max 20 levels of comment depth
  - **Cooldown Cap**: Bot cannot interact with same human within 10 minutes

### Phase 3: Smart Notification Batching
- Prevents notification spam
- 15-minute cooldown per user
- Scheduled sweeper runs every 5 minutes
- Batches multiple notifications into summarized messages

### Phase 4: Concurrency & Race Condition Handling
- Completely stateless application
- All state stored in Redis
- Atomic Redis operations (INCR, EXISTS, TTL)
- Handles 200+ concurrent requests safely

## 🏗️ Architecture

```
Controller Layer → Service Layer → Repository Layer
                ↓
         Redis Service (Guardrails)
                ↓
         PostgreSQL (Persistent Data)
```

### Thread Safety Approach

The application guarantees thread safety through:

1. **Atomic Redis Operations**:
   - `INCR` for horizontal cap counter (atomic increment)
   - `EXISTS` with `SETEX` for cooldown keys
   - No race conditions even with 200 concurrent requests

2. **Stateless Design**:
   - No in-memory state (HashMap, static variables)
   - All counters and flags stored in Redis
   - Horizontal scaling ready

3. **Transaction Management**:
   - Redis guardrails checked BEFORE database commit
   - Rollback mechanism if transaction fails after Redis increment
   - Data integrity maintained

4. **Key Design Patterns**:
   ```
   post:{postId}:bot_count          → Horizontal cap counter
   post:{postId}:virality_score     → Virality score
   cooldown:bot_{botId}:human_{userId} → Cooldown flag (TTL)
   user:{userId}:notif_cooldown     → Notification cooldown (TTL)
   user:{userId}:pending_notifs     → Pending notifications list
   ```

## 🛠️ Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Docker & Docker Compose

### 1. Start Infrastructure

```bash
docker-compose up -d
```

This starts:
- PostgreSQL on port 5432
- Redis on port 6379

### 2. Build the Application

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

## 📡 API Endpoints

### Create Post
```http
POST /api/posts
Content-Type: application/json

{
  "authorId": 1,
  "content": "This is my first post!"
}
```

### Add Comment
```http
POST /api/posts/{postId}/comments
Content-Type: application/json

{
  "authorId": 2,
  "content": "Great post!",
  "depthLevel": 1,
  "isBot": false
}
```

### Like Post
```http
POST /api/posts/{postId}/like
Content-Type: application/json

{
  "userId": 1
}
```

## 🧪 Testing Concurrency

To test the horizontal cap with concurrent requests:

```bash
# Install Apache Bench or use any load testing tool
# Fire 200 concurrent requests
ab -n 200 -c 200 -p comment.json -T application/json \
   http://localhost:8080/api/posts/1/comments
```

Expected behavior:
- Exactly 100 comments will be created
- 100 requests will receive HTTP 429 (Too Many Requests)
- No race conditions

## 📊 Redis Keys Structure

| Key Pattern | Type | Purpose | TTL |
|------------|------|---------|-----|
| `post:{id}:bot_count` | String | Bot reply counter | None |
| `post:{id}:virality_score` | String | Virality score | None |
| `cooldown:bot_{botId}:human_{userId}` | String | Bot-human cooldown | 10 min |
| `user:{id}:notif_cooldown` | String | Notification cooldown | 15 min |
| `user:{id}:pending_notifs` | List | Pending notifications | None |

## 🔍 Monitoring

### Check Redis Keys
```bash
docker exec -it social-api-redis redis-cli

# View all keys
KEYS *

# Check bot count for post 1
GET post:1:bot_count

# Check virality score
GET post:1:virality_score

# Check pending notifications
LRANGE user:1:pending_notifs 0 -1
```

### Check Logs
```bash
# Application logs show:
# - Push notifications sent
# - Guardrail violations
# - Scheduled sweeper activity
tail -f logs/application.log
```

## 🎯 Key Implementation Details

### Horizontal Cap (Race Condition Safe)
```java
public void checkHorizontalCap(Long postId) {
    String key = String.format("post:%d:bot_count", postId);
    Long currentCount = redisService.increment(key); // Atomic INCR
    
    if (currentCount > horizontalCap) {
        redisService.incrementBy(key, -1); // Rollback
        throw new GuardrailViolationException("Horizontal cap exceeded");
    }
}
```

### Cooldown Cap (TTL-based)
```java
public void checkCooldownCap(Long botId, Long humanId) {
    String key = String.format("cooldown:bot_%d:human_%d", botId, humanId);
    
    if (redisService.exists(key)) {
        throw new GuardrailViolationException("Cooldown active");
    }
    
    // Set key with 10-minute TTL
    redisService.setWithExpiry(key, "1", 10, TimeUnit.MINUTES);
}

// Determine target human for cooldown
private Long determineTargetHuman(Post post, Long parentCommentId) {
    if (parentCommentId != null) {
        Comment parent = commentRepository.findById(parentCommentId).orElseThrow();
        // Check if parent author is human (exists in User table)
        if (userRepository.existsById(parent.getAuthorId())) {
            return parent.getAuthorId();
        }
        return null; // Parent is a bot, no cooldown
    }
    
    // Check if post author is human
    if (userRepository.existsById(post.getAuthorId())) {
        return post.getAuthorId();
    }
    return null; // Post author is a bot, no cooldown
}
```

### Notification Batching
```java
@Scheduled(cron = "0 */5 * * * *") // Every 5 minutes
public void sweepPendingNotifications() {
    Set<String> pendingKeys = redisService.keys("user:*:pending_notifs");
    
    for (String key : pendingKeys) {
        List<Object> notifications = redisService.getList(key);
        // Summarize and log
        log.info("Bot X and {} others interacted with your posts", 
            notifications.size() - 1);
        redisService.delete(key);
    }
}
```

## 📦 Project Structure

```
src/main/java/com/grid07/socialapi/
├── config/
│   └── RedisConfig.java
├── controller/
│   └── PostController.java
├── dto/
│   ├── ApiResponse.java
│   ├── CreateCommentRequest.java
│   ├── CreatePostRequest.java
│   └── LikePostRequest.java
├── entity/
│   ├── Bot.java
│   ├── Comment.java
│   ├── Post.java
│   └── User.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── GuardrailViolationException.java
├── repository/
│   ├── BotRepository.java
│   ├── CommentRepository.java
│   ├── PostRepository.java
│   └── UserRepository.java
├── service/
│   ├── GuardrailService.java
│   ├── NotificationScheduler.java
│   ├── NotificationService.java
│   ├── PostService.java
│   ├── RedisService.java
│   └── ViralityService.java
└── SocialApiApplication.java
```

## 🔒 Security & Best Practices

- ✅ Stateless application design
- ✅ Atomic Redis operations
- ✅ Transaction rollback on failure
- ✅ Input validation with Bean Validation
- ✅ Global exception handling
- ✅ Proper HTTP status codes
- ✅ Structured logging
- ✅ Clean architecture (layered design)
- ✅ Proper author type identification (User vs Bot)
- ✅ Parent comment tracking for nested threads
- ✅ Intelligent cooldown (only bot-to-human interactions)

## 📝 Configuration

Edit `src/main/resources/application.yml` to customize:

```yaml
app:
  redis:
    horizontal-cap: 100
    vertical-cap: 20
    cooldown-minutes: 10
    notification-cooldown-minutes: 15
  scheduler:
    notification-sweep-cron: "0 */5 * * * *"
```

## 📚 Additional Documentation

- **VERIFICATION_CHECKLIST.md** - Complete verification of all PDF requirements
- **TESTING_GUIDE.md** - Comprehensive testing scenarios and commands
- **data.sql** - Initial test data (users and bots)

## 🐛 Troubleshooting

### PostgreSQL Connection Issues
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# View logs
docker logs social-api-postgres
```

### Redis Connection Issues
```bash
# Check if Redis is running
docker ps | grep redis

# Test connection
docker exec -it social-api-redis redis-cli ping
```

## 📄 License

This project is part of the Grid07 Backend Engineering Assignment.

## 👥 Author

Backend Engineering Intern Candidate
