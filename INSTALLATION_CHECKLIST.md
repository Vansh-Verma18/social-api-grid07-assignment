# Installation Checklist

## Current Status (Your System)

✅ **Java 17** - INSTALLED (OpenJDK 17.0.13)
❌ **Maven** - NOT INSTALLED
❌ **Docker** - NOT INSTALLED

---

## What You Need to Install

### 1. Maven (Required for building the project)

**Choose ONE method:**

#### Method A: Using PowerShell Script (Easiest)
```powershell
# Run PowerShell as Administrator
# Navigate to project folder
cd C:\Users\vansh\Desktop\PROJECT_ASSIGNMENT

# Run the installation script
.\install-maven.ps1
```

#### Method B: Using Chocolatey
```powershell
# Run PowerShell as Administrator
choco install maven -y

# Close and reopen PowerShell
mvn -version
```

#### Method C: Manual Installation
1. Download from: https://maven.apache.org/download.cgi
2. Download file: **apache-maven-3.9.6-bin.zip**
3. Extract to: `C:\Program Files\Apache\maven`
4. Add to PATH: `C:\Program Files\Apache\maven\bin`
5. Close and reopen PowerShell
6. Verify: `mvn -version`

---

### 2. Docker Desktop (Required for PostgreSQL and Redis)

#### Installation Steps:
1. **Download:**
   - Go to: https://www.docker.com/products/docker-desktop
   - Click "Download for Windows"

2. **Install:**
   - Run the installer
   - Check "Use WSL 2 instead of Hyper-V"
   - Complete installation
   - **Restart your computer**

3. **Start Docker Desktop:**
   - Open Docker Desktop from Start Menu
   - Wait for "Docker Desktop is running"

4. **Verify:**
   ```powershell
   docker --version
   docker-compose --version
   ```

---

## Installation Order

Follow this order:

1. ✅ Java 17 - **Already installed**
2. ⬜ Install Maven (choose method above)
3. ⬜ Install Docker Desktop
4. ⬜ Restart computer (after Docker installation)
5. ⬜ Start Docker Desktop
6. ⬜ Verify all installations

---

## Verification Commands

After installing everything, run these commands:

```powershell
# Check Java
java -version
# Expected: openjdk version "17.0.13"

# Check Maven
mvn -version
# Expected: Apache Maven 3.9.6

# Check Docker
docker --version
# Expected: Docker version 24.x.x

# Check Docker Compose
docker-compose --version
# Expected: Docker Compose version v2.x.x
```

---

## After Installation - Run the Project

Once all prerequisites are installed:

### Step 1: Start Docker Desktop
- Open Docker Desktop application
- Wait until it shows "Docker Desktop is running"

### Step 2: Navigate to Project
```powershell
cd C:\Users\vansh\Desktop\PROJECT_ASSIGNMENT
```

### Step 3: Start Infrastructure
```powershell
docker-compose up -d
```

### Step 4: Build Project
```powershell
mvn clean install
```

### Step 5: Run Application
```powershell
mvn spring-boot:run
```

---

## Quick Links

- Maven Download: https://maven.apache.org/download.cgi
- Docker Desktop: https://www.docker.com/products/docker-desktop
- Java (already installed): https://adoptium.net/

---

## Troubleshooting

### Maven not recognized after installation
- Close and reopen PowerShell
- Verify PATH: `$env:Path -split ';' | Select-String maven`
- Run PowerShell as Administrator

### Docker installation fails
- Enable virtualization in BIOS
- Install WSL 2: `wsl --install`
- Restart computer
- Check Windows version (need Windows 10/11)

### Port already in use
```powershell
# Check what's using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID)
taskkill /PID <PID> /F
```

---

## Estimated Time

- Maven installation: 5-10 minutes
- Docker installation: 10-15 minutes (includes restart)
- Project build: 2-3 minutes (first time)
- Total: ~20-30 minutes

---

## Next Steps

After successful installation:

1. ✅ Read **QUICK_START.md** for running the project
2. ✅ Read **TESTING_GUIDE.md** for testing scenarios
3. ✅ Import **postman_collection.json** into Postman
4. ✅ Test the API endpoints

---

## Need Help?

Check these files:
- **WINDOWS_INSTALLATION_GUIDE.md** - Detailed installation instructions
- **SETUP_GUIDE.md** - Complete setup guide
- **QUICK_START.md** - Quick start after installation
