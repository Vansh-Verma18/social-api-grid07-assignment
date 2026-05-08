# Testing Guide for Social API

This guide provides step-by-step instructions for testing all features of the Social API.

## Prerequisites

1. Start the infrastructure:
```bash
docker-compose up -d
```

2. Start the Spring Boot application:
```bash
mvn spring-boot:run
```

3. Import `postman_collection.json` into Postman

## Test Scenarios

### Scenario 1: Basic Post Creation and Interaction

#### Step 1: Create a Post by User
```bash
POST http://localhost:8080/api/posts
Content-Type: application/json

{
  "authorId": 1,
  "content": "Just learned about Spring Boot and Redis! Amazing combination!"
}
```

**Expected**: HTTP 201, post created with ID (e.g., 1)

#### Step 2: Human Likes the Post
```bash
POST http://localhost:8080/api/posts/1/like
Content-Type: application/json

{
  "userId": 2
}
```

**Expected**: 
- HTTP 200
- Virality score increased by 20
- Check Redis: `GET post:1:virality_score` should show 20

#### Step 3: Human Comments on Post
```bash
POST http://localhost:8080/api/posts/1/comments
Content-Type: application/json

{
  "authorId": 2,
  "content": "Great post! Very informative.",
  "depthLevel": 1,
  "isBot": false,
  "parentCommentId": null
}
```

**Expected**:
- HTTP 201
- Virality score increased by 50
- Check Redis: `GET post:1:virality_score` should show 70

---

### Scenario 2: Bot Interaction with Guardrails

#### Step 1: Bot Comments on Human's Post (First Time)
```bash
POST http://localhost:8080/api/posts/1/comments
Content-Type: application/json

{
  "authorId": 100,
  "content": "Interesting perspective! As an AI, I find this fascinating.",
  "depthLevel": 1,
  "isBot": true,
  "parentCommentId": null
}
```

**Expected**:
- HTTP 201
- Bot count incremented: `GET post:1:bot_count` = 1
- Virality score increased by 1: `GET post:1:virality_score` = 71
- Cooldown set: `EXISTS cooldown:bot_100:human_1` = 1
- Notification logged or batched

#### Step 2: Same Bot Tries to Comment Again (Should Fail - Cooldown)
```bash
POST http://localhost:8080/api/posts/1/comments
Content-Type: application/json

{
  "authorId": 100,
  "content": "Another comment from the same bot",
  "depthLevel": 1,
  "isBot": true,
  "parentCommentId": null
}
```

**Expected**:
- HTTP 429 (Too Many Requests)
- Error message: "Cooldown active: Bot 100 cannot interact with User 1 yet"
- Bot count NOT incremented (still 1)

#### Step 3: Different Bot Comments (Should Succeed)
```bash
POST http://localhost:8080/api/posts/1/comments
Content-Type: application/json

{
  "authorId": 101,
  "content": "I agree with the previous comments!",
  "depthLevel": 1,
  "isBot": true,
  "parentCommentId": null
}
```

**Expected**:
- HTTP 201
- Bot count incremented: `GET post:1:bot_count` = 2
- Different cooldown key created

---

### Scenario 3: Horizontal Cap Test (100 Bot Limit)

#### Automated Test with Apache Bench
```bash
# Create a file: bot_comment.json
{
  "authorId": 100,
  "content": "Bot comment",
  "depthLevel": 1,
  "isBot": true,
  "parentCommentId": null
}

# Fire 200 concurrent requests
ab -n 200 -c 200 -p bot_comment.json -T application/json \
   http://localhost:8080/api/posts/2/comments
```

**Expected**:
- Exactly 100 requests succeed (HTTP 201)
- Exactly 100 requests fail (HTTP 429)
- Check database: `SELECT COUNT(*) FROM comments WHERE post_id = 2` = 100
- Check Redis: `GET post:2:bot_count` = 100

#### Manual Test (Simpler)
Use Postman Runner:
1. Create a new post (ID = 2)
2. Use different bot IDs (100-199) to avoid cooldown
3. Run the "Add Bot Comment" request 150 times
4. First 100 should succeed, rest should fail

---

### Scenario 4: Vertical Cap Test (Max Depth 20)

#### Step 1: Create Comment at Depth 20 (Should Succeed)
```bash
POST http://localhost:8080/api/posts/1/comments
Content-Type: application/json

{
  "authorId": 3,
  "content": "Comment at maximum depth",
  "depthLevel": 20,
  "isBot": false,
  "parentCommentId": null
}
```

**Expected**: HTTP 201

#### Step 2: Create Comment at Depth 21 (Should Fail)
```bash
POST http://localhost:8080/api/posts/1/comments
Content-Type: application/json

{
  "authorId": 3,
  "content": "Comment exceeding maximum depth",
  "depthLevel": 21,
  "isBot": false,
  "parentCommentId": null
}
```

**Expected**:
- HTTP 429
- Error message: "Vertical cap exceeded: Comment depth 21 exceeds maximum of 20"

---

### Scenario 5: Notification Batching

#### Step 1: Bot Interacts with User's Post (First Interaction)
```bash
POST http://localhost:8080/api/posts/1/comments
Content-Type: application/json

{
  "authorId": 102,
  "content": "First bot interaction",
  "depthLevel": 1,
  "isBot": true,
  "parentCommentId": null
}
```

**Expected**:
- Console log: "Push Notification Sent to User 1: Bot TechBot replied to your post"
- Cooldown set: `EXISTS user:1:notif_cooldown` = 1

#### Step 2: Another Bot Interacts Within 15 Minutes (Should Batch)
```bash
POST http://localhost:8080/api/posts/1/comments
Content-Type: application/json

{
  "authorId": 103,
  "content": "Second bot interaction",
  "depthLevel": 1,
  "isBot": true,
  "parentCommentId": null
}
```

**Expected**:
- No immediate notification log
- Notification added to list: `LRANGE user:1:pending_notifs 0 -1`

#### Step 3: Wait for Scheduled Sweep (5 Minutes)
After 5 minutes, check console logs.

**Expected**:
- Console log: "Summarized Push Notification: Bot X and 1 others interacted with your posts."
- Pending list cleared: `LRANGE user:1:pending_notifs 0 -1` = empty

---

### Scenario 6: Bot Replying to Another Bot (No Cooldown)

#### Step 1: Bot Creates a Post
```bash
POST http://localhost:8080/api/posts
Content-Type: application/json

{
  "authorId": 100,
  "content": "Post created by a bot"
}
```

**Expected**: HTTP 201, post created (e.g., ID = 3)

#### Step 2: Another Bot Comments on Bot's Post
```bash
POST http://localhost:8080/api/posts/3/comments
Content-Type: application/json

{
  "authorId": 101,
  "content": "Bot replying to another bot",
  "depthLevel": 1,
  "isBot": true,
  "parentCommentId": null
}
```

**Expected**:
- HTTP 201
- No cooldown key created (bot-to-bot interaction)
- No notification sent
- Bot count still incremented
- Virality score updated

#### Step 3: Same Bot Comments Again Immediately (Should Succeed)
```bash
POST http://localhost:8080/api/posts/3/comments
Content-Type: application/json

{
  "authorId": 101,
  "content": "Bot commenting again on bot's post",
  "depthLevel": 1,
  "isBot": true,
  "parentCommentId": null
}
```

**Expected**:
- HTTP 201 (no cooldown for bot-to-bot)
- Bot count incremented

---

## Redis Monitoring Commands

### Check Virality Score
```bash
docker exec -it social-api-redis redis-cli
GET post:1:virality_score
```

### Check Bot Count
```bash
GET post:1:bot_count
```

### Check Cooldown Keys
```bash
KEYS cooldown:*
TTL cooldown:bot_100:human_1
```

### Check Notification Cooldown
```bash
EXISTS user:1:notif_cooldown
TTL user:1:notif_cooldown
```

### Check Pending Notifications
```bash
LRANGE user:1:pending_notifs 0 -1
LLEN user:1:pending_notifs
```

### View All Keys
```bash
KEYS *
```

### Clear All Redis Data (Reset)
```bash
FLUSHALL
```

---

## Database Verification

### Check Comment Count
```sql
SELECT COUNT(*) FROM comments WHERE post_id = 1;
```

### Check Bot Comments Only
```sql
SELECT COUNT(*) FROM comments 
WHERE post_id = 1 
AND author_id >= 100;
```

### View All Comments
```sql
SELECT * FROM comments ORDER BY created_at DESC;
```

### View Posts with Comment Count
```sql
SELECT p.id, p.content, COUNT(c.id) as comment_count
FROM posts p
LEFT JOIN comments c ON p.id = c.post_id
GROUP BY p.id, p.content;
```

---

## Performance Testing

### Test Concurrent Requests (Race Condition Test)

#### Using Apache Bench
```bash
# Install Apache Bench
# Ubuntu: sudo apt-get install apache2-utils
# Mac: brew install httpd

# Create request file
echo '{"authorId": 100, "content": "Test", "depthLevel": 1, "isBot": true, "parentCommentId": null}' > comment.json

# Fire 200 concurrent requests
ab -n 200 -c 200 -p comment.json -T application/json \
   http://localhost:8080/api/posts/1/comments
```

#### Using JMeter
1. Create Thread Group with 200 threads
2. Add HTTP Request sampler
3. Configure POST to `/api/posts/1/comments`
4. Add JSON body
5. Run and verify exactly 100 succeed

#### Using Python Script
```python
import requests
import concurrent.futures
import json

url = "http://localhost:8080/api/posts/1/comments"
headers = {"Content-Type": "application/json"}
data = {
    "authorId": 100,
    "content": "Concurrent test",
    "depthLevel": 1,
    "isBot": True,
    "parentCommentId": None
}

def make_request(i):
    data["authorId"] = 100 + i  # Different bots to avoid cooldown
    response = requests.post(url, json=data, headers=headers)
    return response.status_code

with concurrent.futures.ThreadPoolExecutor(max_workers=200) as executor:
    results = list(executor.map(make_request, range(200)))

success = results.count(201)
failed = results.count(429)

print(f"Success: {success}, Failed: {failed}")
# Expected: Success: 100, Failed: 100
```

---

## Troubleshooting

### Issue: Connection Refused
**Solution**: Ensure PostgreSQL and Redis are running
```bash
docker ps
docker-compose up -d
```

### Issue: Redis Keys Not Found
**Solution**: Ensure you're creating bot comments (not human comments)
```bash
# Check if keys exist
docker exec -it social-api-redis redis-cli KEYS "*"
```

### Issue: Cooldown Not Working
**Solution**: Check TTL on cooldown keys
```bash
docker exec -it social-api-redis redis-cli TTL cooldown:bot_100:human_1
# Should show remaining seconds (0-600)
```

### Issue: Notifications Not Batching
**Solution**: 
1. Check if notification cooldown exists
2. Wait for scheduled task (runs every 5 minutes)
3. Check application logs

### Issue: Horizontal Cap Allows > 100 Comments
**Solution**: This indicates a race condition bug
- Verify atomic Redis operations are being used
- Check if rollback is working
- Review transaction management

---

## Success Criteria

✅ **Phase 1**: All CRUD operations work correctly
✅ **Phase 2**: All three guardrails enforce limits correctly
✅ **Phase 3**: Notifications batch correctly and sweep runs
✅ **Phase 4**: Exactly 100 comments created with 200 concurrent requests
✅ **Statelessness**: Application can be restarted without losing Redis state
✅ **Data Integrity**: Database matches Redis counters

---

## Additional Test Cases

### Edge Case 1: Cooldown Expiration
1. Bot comments on human's post
2. Wait 10 minutes
3. Same bot comments again
4. Should succeed (cooldown expired)

### Edge Case 2: Multiple Posts
1. Create 5 different posts
2. Bot comments on all 5
3. Each should have independent bot_count
4. Cooldown should be per-human, not per-post

### Edge Case 3: Notification Sweep with No Pending
1. Ensure no pending notifications exist
2. Wait for sweep to run
3. Should log "No pending notifications found"
4. No errors should occur

### Edge Case 4: Deep Comment Thread
1. Create comments at increasing depths (1-20)
2. All should succeed
3. Attempt depth 21
4. Should fail with vertical cap error
