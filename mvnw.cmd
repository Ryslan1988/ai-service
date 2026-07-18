@echo off
where mvn >nul 2>nul
if errorlevel 1 (
  echo Maven is not installed. Install Maven 3.9+ or run the project from IntelliJ IDEA.
  exit /b 1
)
mvn %*
