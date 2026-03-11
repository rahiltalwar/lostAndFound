@echo off
REM ─────────────────────────────────────────────────────────────────
REM Lost & Found AI Agent — Run Script (Windows)
REM ─────────────────────────────────────────────────────────────────

REM ── 1. Check for API key ─────────────────────────────────────────
if "%ANTHROPIC_API_KEY%"=="" (
  echo.
  echo ^⚠️  ANTHROPIC_API_KEY is not set!
  echo.
  echo    Get a free API key at: https://console.anthropic.com
  echo    Then run:  set ANTHROPIC_API_KEY=sk-ant-...
  echo.
  set /p ANTHROPIC_API_KEY="   Enter your API key now (or press Enter to skip): "
)

REM ── 2. Compile ───────────────────────────────────────────────────
if not exist "out" mkdir out

echo 📦 Compiling...
dir /s /b src\*.java > sources.txt 2>nul
javac --add-exports jdk.httpserver/sun.net.httpserver=ALL-UNNAMED ^
      -d out @sources.txt
if errorlevel 1 (
  echo.
  echo ❌ Compilation failed. Make sure JDK 17+ is installed.
  echo    Download from: https://adoptium.net
  pause
  exit /b 1
)
echo ✅ Compilation done

REM ── 3. Run ───────────────────────────────────────────────────────
echo.
java --add-exports jdk.httpserver/sun.net.httpserver=ALL-UNNAMED ^
     -cp out lostandfound.Main
pause
