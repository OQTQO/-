@echo off
setlocal
cd /d "%~dp0"

where java >nul 2>nul
if errorlevel 1 (
  echo [SealStudio] 未检测到 Java。请安装 JDK 17 或更高版本。
  pause
  exit /b 1
)

where javac >nul 2>nul
if errorlevel 1 (
  echo [SealStudio] 检测到 Java 运行时，但没有 javac。请安装完整 JDK 17 或更高版本。
  pause
  exit /b 1
)

if not exist out mkdir out
dir /s /b src\*.java > .sealstudio-sources.txt
javac -encoding UTF-8 -d out @.sealstudio-sources.txt
if errorlevel 1 (
  del .sealstudio-sources.txt >nul 2>nul
  echo.
  echo [SealStudio] 编译失败。
  pause
  exit /b 1
)
del .sealstudio-sources.txt >nul 2>nul

java -cp out cn.localhost01.seal.ui.SealStudioApp
endlocal
