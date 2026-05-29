@echo off
echo ============================================
echo AI Classroom Platform - Startup Script
echo ============================================
echo.

echo [1/5] Checking PostgreSQL...
pg_isready -h localhost -p 5432 >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: PostgreSQL is not running!
    echo Please start PostgreSQL service
    pause
    exit /b 1
)
echo ✓ PostgreSQL is running

echo.
echo [2/5] Checking Ollama...
curl -s http://localhost:11434/api/tags >nul 2>&1
if %errorlevel% neq 0 (
    echo WARNING: Ollama is not running!
    echo AI features will not work. Start Ollama with: ollama serve
    timeout /t 3 >nul
) else (
    echo ✓ Ollama is running
)

echo.
echo [3/5] Checking database...
psql -U postgres -lqt | findstr ai_classroom >nul 2>&1
if %errorlevel% neq 0 (
    echo Creating database ai_classroom...
    psql -U postgres -c "CREATE DATABASE ai_classroom;"
)
echo ✓ Database ai_classroom exists

echo.
echo [4/5] Building application...
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)
echo ✓ Build successful

echo.
echo [5/5] Starting application...
echo.
echo ============================================
echo Application will start on http://localhost:8082
echo Admin credentials: admin@classroom.com / admin123
echo ============================================
echo.

call mvn spring-boot:run
