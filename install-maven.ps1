# Maven Installation Script for Windows
# Run this script in PowerShell as Administrator

Write-Host "Maven Installation Script" -ForegroundColor Green
Write-Host "=========================" -ForegroundColor Green
Write-Host ""

# Check if Maven is already installed
Write-Host "Checking if Maven is already installed..." -ForegroundColor Yellow
try {
    $mavenVersion = mvn -version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Maven is already installed:" -ForegroundColor Green
        Write-Host $mavenVersion
        exit 0
    }
} catch {
    Write-Host "Maven is not installed. Proceeding with installation..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Installation Options:" -ForegroundColor Cyan
Write-Host "1. Install using Chocolatey (Recommended - Easiest)"
Write-Host "2. Manual Installation (Download and configure)"
Write-Host "3. Exit"
Write-Host ""

$choice = Read-Host "Enter your choice (1, 2, or 3)"

switch ($choice) {
    "1" {
        Write-Host ""
        Write-Host "Installing Maven using Chocolatey..." -ForegroundColor Yellow
        
        # Check if Chocolatey is installed
        try {
            choco --version | Out-Null
            Write-Host "Chocolatey is installed. Installing Maven..." -ForegroundColor Green
            choco install maven -y
            
            Write-Host ""
            Write-Host "Maven installation completed!" -ForegroundColor Green
            Write-Host "Please close and reopen PowerShell, then run: mvn -version" -ForegroundColor Cyan
        } catch {
            Write-Host "Chocolatey is not installed." -ForegroundColor Red
            Write-Host ""
            Write-Host "Would you like to install Chocolatey first? (Y/N)" -ForegroundColor Yellow
            $installChoco = Read-Host
            
            if ($installChoco -eq "Y" -or $installChoco -eq "y") {
                Write-Host "Installing Chocolatey..." -ForegroundColor Yellow
                Set-ExecutionPolicy Bypass -Scope Process -Force
                [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
                Invoke-Expression ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
                
                Write-Host ""
                Write-Host "Chocolatey installed! Now installing Maven..." -ForegroundColor Green
                choco install maven -y
                
                Write-Host ""
                Write-Host "Maven installation completed!" -ForegroundColor Green
                Write-Host "Please close and reopen PowerShell, then run: mvn -version" -ForegroundColor Cyan
            } else {
                Write-Host "Installation cancelled. Please choose option 2 for manual installation." -ForegroundColor Yellow
            }
        }
    }
    
    "2" {
        Write-Host ""
        Write-Host "Manual Installation Instructions:" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "1. Download Maven from: https://maven.apache.org/download.cgi" -ForegroundColor Cyan
        Write-Host "   - Download: apache-maven-3.9.6-bin.zip" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "2. Extract to: C:\Program Files\Apache\maven" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "3. Add to PATH:" -ForegroundColor Cyan
        Write-Host "   - Press Win + X -> System -> Advanced system settings" -ForegroundColor White
        Write-Host "   - Environment Variables -> System variables -> Path -> Edit" -ForegroundColor White
        Write-Host "   - Add: C:\Program Files\Apache\maven\bin" -ForegroundColor White
        Write-Host ""
        Write-Host "4. Close and reopen PowerShell" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "5. Verify: mvn -version" -ForegroundColor Cyan
        Write-Host ""
        
        Write-Host "Would you like to open the Maven download page? (Y/N)" -ForegroundColor Yellow
        $openBrowser = Read-Host
        
        if ($openBrowser -eq "Y" -or $openBrowser -eq "y") {
            Start-Process "https://maven.apache.org/download.cgi"
            Write-Host "Browser opened. Follow the instructions above after downloading." -ForegroundColor Green
        }
    }
    
    "3" {
        Write-Host "Installation cancelled." -ForegroundColor Yellow
        exit 0
    }
    
    default {
        Write-Host "Invalid choice. Please run the script again." -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "Script completed!" -ForegroundColor Green
