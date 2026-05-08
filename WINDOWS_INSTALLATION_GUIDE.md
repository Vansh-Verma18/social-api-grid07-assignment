# Windows Installation Guide

Your system status:
- ✅ Java 17 - **INSTALLED** (OpenJDK 17.0.13)
- ❌ Maven - **NOT INSTALLED**
- ❌ Docker - **NOT INSTALLED**

Follow these steps to install the missing prerequisites.

---

## Step 1: Install Maven

### Option A: Using Chocolatey (Easiest - Recommended)

If you have Chocolatey package manager:

```powershell
# Run PowerShell as Administrator
choco install maven
```

### Option B: Manual Installation

1. **Download Maven:**
   - Go to: https://maven.apache.org/download.cgi
   - Download: **apache-maven-3.9.6-bin.zip** (Binary zip archive)

2. **Extract Maven:**
   - Extract the zip file to: `C:\Program Files\Apache\maven`
   - You should have: `C:\Program Files\Apache\maven\bin\mvn.cmd`

3. **Add Maven to PATH:**
   
   **Method 1: Using GUI**
   - Press `Win + X` → Select "System"
   - Click "Advanced system settings"
   - Click "Environment Variables"
   - Under "System variables", find "Path"
   - Click "Edit"
   - Click "New"
   - Add: `C:\Program Files\Apache\maven\bin`
   - Click "OK" on all dialogs

   **Method 2: Using PowerShell (Run as Administrator)**
   ```powershell
   [Environment]::SetEnvironmentVariable("Path", $env:Path + ";C:\Program Files\Apache\maven\bin", "Machine")
   ```

4. **Verify Installation:**
   - **Close and reopen PowerShell** (important!)
   - Run:
   ```powershell
   mvn -version
   ```
   - You should see Maven version information

---

## Step 2: Install Docker Desktop

### Download and Install

1. **Download Docker Desktop:**
   - Go to: https://www.docker.com/products/docker-desktop
   - Click "Download for Windows"
   - Download: **Docker Desktop Installer.exe**

2. **Install Docker Desktop:**
   - Run the installer
   - Check "Use WSL 2 instead of Hyper-V" (recommended)
   - Follow the installation wizard
   - **Restart your computer when prompted**

3. **Start Docker Desktop:**
   - Open Docker Desktop from Start Menu
   - Wait for it to start (you'll see "Docker Desktop is running")
   - Accept the terms if prompted

4. **Verify Installation:**
   - Open PowerShell
   - Run:
   ```powershell
   docker --version
   docker-compose --version
   ```
   - You should see version information for both

### Troubleshooting Docker Installation

**Issue: WSL 2 installation is incomplete**
- Docker may prompt you to install WSL 2
- Follow the link provided or run:
```powershell
wsl --install
```
- Restart your computer

**Issue: Virtualization not enabled**
- You need to enable virtualization in BIOS
- Restart computer → Enter BIOS (usually F2, F10, or Del key)
- Enable "Intel VT-x" or "AMD-V"
- Save and exit

---

## Step 3: Verify All Prerequisites

After installing Maven and Docker, verify everything:

```powershell
# Check Java (should already work)
java -version

# Check Maven (should work after installation)
mvn -version

# Check Docker (should work after installation)
docker --version
docker-compose --version
```

**Expected output:**
```
✅ Java: openjdk version "17.0.13"
✅ Maven: Apache Maven 3.9.6
✅ Docker: Docker version 24.x.x
✅ Docker Compose: Docker Compose version v2.x.x
```

---

## Step 4: Run the Project

Once all prerequisites are installed:

### 4.1 Start Docker Desktop
- Open Docker Desktop application
- Wait until it shows "Docker Desktop is running"

### 4.2 Navigate to Project
```powershell
cd C:\Users\vansh\Desktop\PROJECT_ASSIGNMENT
```

### 4.3 Start Infrastructure
```powershell
docker-compose up -d
```

**Expected output:**
```
Creating network "project_assignment_social-api-network" with driver "bridge"
Creating social-api-postgres ... done
Creating social-api-redis    ... done
```

### 4.4 Build the Project
```powershell
mvn clean install
```

**Expected output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 30-60 seconds
```

### 4.5 Run the Application
```powershell
mvn spring-boot:run
```

**Expected output:**
```
Started SocialApiApplication in X seconds
```

---

## Quick Installation Commands (Summary)

### If you have Chocolatey:
```powershell
# Run PowerShell as Administrator
choco install maven
choco install docker-desktop

# Restart computer
# Start Docker Desktop
# Verify installations
mvn -version
docker --version
```

### Manual Installation:
1. Download Maven from https://maven.apache.org/download.cgi
2. Extract to `C:\Program Files\Apache\maven`
3. Add `C:\Program Files\Apache\maven\bin` to PATH
4. Download Docker Desktop from https://www.docker.com/products/docker-desktop
5. Install and restart computer
6. Start Docker Desktop
7. Verify installations

---

## Alternative: Use Maven Wrapper (No Maven Installation Needed)

If you don't want to install Maven globally, you can use Maven Wrapper:

### Create Maven Wrapper in your project:

```powershell
# Download Maven Wrapper
Invoke-WebRequest -Uri https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar -OutFile maven-wrapper.jar

# Create wrapper files (you'll need Maven installed once to do this)
# Or download from a project that already has it
```

### Use Maven Wrapper:
```powershell
# Instead of 'mvn', use './mvnw' (on Windows: mvnw.cmd)
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run
```

---

## Next Steps After Installation

1. ✅ Verify all prerequisites are installed
2. ✅ Start Docker Desktop
3. ✅ Run `docker-compose up -d`
4. ✅ Run `mvn clean install`
5. ✅ Run `mvn spring-boot:run`
6. ✅ Test with Postman or curl

---

## Need Help?

### Maven Installation Issues:
- Make sure you closed and reopened PowerShell after adding to PATH
- Verify PATH: `$env:Path -split ';' | Select-String maven`
- Try running as Administrator

### Docker Installation Issues:
- Make sure virtualization is enabled in BIOS
- Install WSL 2: `wsl --install`
- Restart computer after installation
- Check Docker Desktop is running (system tray icon)

### Still Having Issues?
- Check if antivirus is blocking installations
- Run PowerShell as Administrator
- Restart computer after installations
- Check Windows version (Docker requires Windows 10/11 Pro or Enterprise for Hyper-V)

---

## Contact Information

If you're still stuck after following this guide:
1. Check the error messages carefully
2. Google the specific error
3. Check Docker Desktop logs (Settings → Troubleshoot)
4. Verify Windows version compatibility
