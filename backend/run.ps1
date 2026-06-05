# RepairApplication Startup Script
$ErrorActionPreference = "Continue"

$projectRoot = "C:\Users\24381\Desktop\RepairingSystem\backend"
$mavenRepo = "$env:USERPROFILE\.m2\repository"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  RepairApplication Startup Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check Java version
Write-Host "Checking Java Version..." -ForegroundColor Yellow
$javaVersion = Start-Process -FilePath "java" -ArgumentList "-version" -NoNewWindow -Wait -PassThru
Write-Host ""

# Check if target classes exist
if (-not (Test-Path "$projectRoot\target\classes\com\dorm\repair\RepairApplication.class")) {
    Write-Host "ERROR: Compiled classes not found. Please compile the project first." -ForegroundColor Red
    Write-Host "Hint: Open project in IntelliJ IDEA and build it." -ForegroundColor Yellow
    exit 1
}

Write-Host "Compiled classes found." -ForegroundColor Green
Write-Host ""

# Build classpath with wildcards
Write-Host "Building classpath..." -ForegroundColor Yellow
$cp = @(
    "target\classes",
    "$mavenRepo\org\springframework\boot\spring-boot*\*\*.jar",
    "$mavenRepo\com\baomidou\mybatis*\*\*.jar",
    "$mavenRepo\cn\hutool\hutool*\*\*.jar",
    "$mavenRepo\io\jsonwebtoken\jjwt*\*\*.jar",
    "$mavenRepo\org\apache\poi\*\*.jar",
    "$mavenRepo\com\mysql\mysql-connector-j\*.jar",
    "$mavenRepo\com\fasterxml\jackson\*\*/\*\*.jar",
    "$mavenRepo\org\slf4j\slf4j*\*\*.jar",
    "$mavenRepo\ch\qos\logback\*\*.jar",
    "$mavenRepo\org\apache\logging\log4j\*\*.jar",
    "$mavenRepo\org\apache\tomcat\embed\*\*.jar",
    "$mavenRepo\org\springframework\*\*/\*\*.jar",
    "$mavenRepo\jakarta\*\*.jar",
    "$mavenRepo\com\zaxxer\HikariCP\*\*.jar",
    "$mavenRepo\org\yaml\snakeyaml\*\*.jar",
    "$mavenRepo\org\mybatis\mybatis\*\*.jar",
    "$mavenRepo\org\mybatis\mybatis-spring\*\*.jar",
    "$mavenRepo\com\github\jsqlparser\jsqlparser\*\*.jar",
    "$mavenRepo\io\micrometer\*\*.jar"
)

Write-Host "Classpath built successfully." -ForegroundColor Green
Write-Host ""

# Run the application
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Starting RepairApplication..." -ForegroundColor Green
Write-Host "  Access: http://localhost:8080" -ForegroundColor Green
Write-Host "  Press Ctrl+C to stop" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Start the Java process
$javaProcess = Start-Process -FilePath "java" `
    -ArgumentList "-Xms256m", "-Xmx512m", "-Dfile.encoding=UTF-8", "-cp", ($cp -join ";"), "com.dorm.repair.RepairApplication" `
    -WorkingDirectory $projectRoot `
    -NoNewWindow `
    -PassThru `
    -RedirectStandardOutput "$env:TEMP\repair_stdout.log" `
    -RedirectStandardError "$env:TEMP\repair_stderr.log"

# Monitor the process
try {
    while (-not $javaProcess.HasExited) {
        Start-Sleep -Seconds 2
        Write-Host "." -NoNewline -ForegroundColor Cyan
    }
} 
finally {
    Write-Host ""
    Write-Host ""
    Write-Host "Application stopped." -ForegroundColor Yellow
    
    # Show logs if available
    if (Test-Path "$env:TEMP\repair_stdout.log") {
        Write-Host ""
        Write-Host "===== STDOUT =====" -ForegroundColor Yellow
        Get-Content "$env:TEMP\repair_stdout.log" | Select-Object -Last 50
    }
    
    if (Test-Path "$env:TEMP\repair_stderr.log") {
        $stderrContent = Get-Content "$env:TEMP\repair_stderr.log" -Raw
        if ($stderrContent) {
            Write-Host ""
            Write-Host "===== STDERR =====" -ForegroundColor Red
            Get-Content "$env:TEMP\repair_stderr.log" | Select-Object -Last 50
        }
    }
}
