@echo off
echo ============================================
echo AI Classroom Platform - Quick Start
echo ============================================
echo.
echo IMPORTANT: Make sure PostgreSQL is running!
echo.
echo Starting application on http://localhost:8082
echo Admin: admin@classroom.com / admin123
echo.
echo ============================================
echo.

cd /d "%~dp0"
call mvn spring-boot:run

pause
