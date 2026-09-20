@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"

echo [SealStudio] Checking runtime and starting application...
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\Start-SealStudio.ps1"
set "EXIT_CODE=%ERRORLEVEL%"

if not "%EXIT_CODE%"=="0" (
  echo.
  echo [SealStudio] Startup failed with exit code %EXIT_CODE%.
  pause
)

exit /b %EXIT_CODE%
