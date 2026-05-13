@echo off
echo 🌱 Starting AgroTech AI System...

:: ── Paths ────────────────────────────────────────────────────────────────────
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "ADB=C:\Users\ss\AppData\Local\Android\Sdk\platform-tools\adb.exe"
set "PROJECT_ROOT=c:\Users\ss\OneDrive\Documents\Agriculture-disease-detection-main"
set "BACKEND_DIR=%PROJECT_ROOT%\backend"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo ☕ Java: %JAVA_HOME%
echo 📡 ADB : %ADB%

:: ── Stop gradle daemons ───────────────────────────────────────────────────────
cd /d "%PROJECT_ROOT%"
call gradlew.bat --stop >nul 2>&1

:: ── Free port 5000 ────────────────────────────────────────────────────────────
echo 🔍 Freeing port 5000...
FOR /F "tokens=5" %%T IN ('netstat -a -n -o ^| findstr :5000') DO (
    TaskKill.exe /PID %%T /F >nul 2>&1
)

:: ── Firewall ──────────────────────────────────────────────────────────────────
netsh advfirewall firewall add rule name="AgroTech_Backend" dir=in action=allow protocol=TCP localport=5000 >nul 2>&1
echo ✅ Firewall rule OK.

:: ── ADB reverse ───────────────────────────────────────────────────────────────
echo 📡 ADB reverse tcp:5000...
"%ADB%" reverse tcp:5000 tcp:5000

:: ── Start Flask in a new window ───────────────────────────────────────────────
echo 🚀 Launching Flask backend (new window)...
start "AgroTech Backend" cmd /k "cd /d "%BACKEND_DIR%" && python main.py"
ping 127.0.0.1 -n 5 > nul

:: ── Build Android App ─────────────────────────────────────────────────────────
echo 📱 Building Android app...
cd /d "%PROJECT_ROOT%"
call gradlew.bat :mobile_app:installDebug
if %ERRORLEVEL% neq 0 (
    echo ❌ Android build failed! Check errors above.
    pause
    exit /b 1
)

:: ── Launch App on Device ──────────────────────────────────────────────────────
echo ✨ Launching app on device...
set "CONNECTED_DEVICE="
for /f "tokens=1" %%i in ('"%ADB%" devices ^| findstr /v "List" ^| findstr /v "offline" ^| findstr "device$"') do (
    set "CONNECTED_DEVICE=%%i"
    goto :launch
)

:launch
if defined CONNECTED_DEVICE (
    echo 🎯 Device: %CONNECTED_DEVICE%
    "%ADB%" -s %CONNECTED_DEVICE% shell am start -n com.agrotech.ai/com.agrotech.ai.ui.MainActivity
) else (
    echo ⚠️ No device found. Trying default...
    "%ADB%" shell am start -n com.agrotech.ai/com.agrotech.ai.ui.MainActivity
)

echo.
echo ✅ All done!
echo 🌐 Farmer Portal: http://localhost:5000/portal
echo 📡 IoT Endpoint : http://localhost:5000/api/iot/data
echo.
:: pause
