# Test API Script - Easy way to test your Social API

Write-Host "Testing Social API on port 8081..." -ForegroundColor Green
Write-Host ""

# Test 1: Create a Post
Write-Host "Test 1: Creating a post..." -ForegroundColor Yellow
try {
    $body = @{
        authorId = 1
        content = "My first post from PowerShell!"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "http://localhost:8081/api/posts" `
                                   -Method POST `
                                   -ContentType "application/json" `
                                   -Body $body

    Write-Host "✅ SUCCESS! Post created:" -ForegroundColor Green
    Write-Host "Post ID: $($response.data.id)" -ForegroundColor Cyan
    Write-Host "Content: $($response.data.content)" -ForegroundColor Cyan
    Write-Host ""

    $postId = $response.data.id

    # Test 2: Like the Post
    Write-Host "Test 2: Liking the post..." -ForegroundColor Yellow
    $likeBody = @{
        userId = 2
    } | ConvertTo-Json

    $likeResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/posts/$postId/like" `
                                       -Method POST `
                                       -ContentType "application/json" `
                                       -Body $likeBody

    Write-Host "✅ SUCCESS! Post liked!" -ForegroundColor Green
    Write-Host ""

    # Test 3: Add a Comment
    Write-Host "Test 3: Adding a comment..." -ForegroundColor Yellow
    $commentBody = @{
        authorId = 2
        content = "Great post!"
        depthLevel = 1
        isBot = $false
        parentCommentId = $null
    } | ConvertTo-Json

    $commentResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/posts/$postId/comments" `
                                          -Method POST `
                                          -ContentType "application/json" `
                                          -Body $commentBody

    Write-Host "✅ SUCCESS! Comment added:" -ForegroundColor Green
    Write-Host "Comment ID: $($commentResponse.data.id)" -ForegroundColor Cyan
    Write-Host "Content: $($commentResponse.data.content)" -ForegroundColor Cyan
    Write-Host ""

    Write-Host "========================================" -ForegroundColor Green
    Write-Host "🎉 ALL TESTS PASSED!" -ForegroundColor Green
    Write-Host "Your API is working perfectly!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green

} catch {
    Write-Host "❌ ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Make sure:" -ForegroundColor Yellow
    Write-Host "1. The application is running (mvn spring-boot:run)" -ForegroundColor White
    Write-Host "2. Docker containers are running (docker ps)" -ForegroundColor White
    Write-Host "3. The application started successfully" -ForegroundColor White
}
