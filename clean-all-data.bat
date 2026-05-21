@echo off
chcp 936 >nul
echo ============================================================
echo           Education Platform - Clean All Data
echo ============================================================
echo.
echo WARNING: This operation will DELETE ALL DATA in the database!
echo Including: users, roles, questions, exams, grades, students, etc.
echo.
set /p confirm=Type YES to confirm:
if not "%confirm%"=="YES" (
    echo Operation cancelled.
    pause
    exit /b 0
)
echo.
echo Enter database info:
set /p DB_USER=DB username (default: root):
if "%DB_USER%"=="" set DB_USER=root

set /p DB_PASS=DB password (leave empty if none):

set DB_NAME=edu_platform
set SQL_FILE=%~dp0backend\src\main\resources\db\clean-all-data.sql

echo.
echo Testing database connection...
if "%DB_PASS%"=="" (
    mysql -u %DB_USER% -e "USE %DB_NAME%; SELECT 1;" >nul 2>&1
) else (
    mysql -u %DB_USER% -p%DB_PASS% -e "USE %DB_NAME%; SELECT 1;" >nul 2>&1
)

if %errorlevel% neq 0 (
    echo ERROR: Cannot connect to database '%DB_NAME%'.
    echo Please check: 1) MySQL is running, 2) username/password, 3) database exists.
    pause
    exit /b 1
)

echo.
echo Cleaning data...
if "%DB_PASS%"=="" (
    mysql -u %DB_USER% %DB_NAME% < "%SQL_FILE%"
) else (
    mysql -u %DB_USER% -p%DB_PASS% %DB_NAME% < "%SQL_FILE%"
)

if %errorlevel% equ 0 (
    echo.
    echo ============================================================
    echo Data cleaned successfully!
    echo ============================================================
) else (
    echo.
    echo ============================================================
    echo Clean failed, please check DB connection and permissions.
    echo ============================================================
)
pause
