@echo off
cd /d "%~dp0.."

where java >nul 2>&1
if errorlevel 1 (
  echo Нужен JDK 21+. Установите Java и повторите.
  exit /b 1
)
where mvn >nul 2>&1
if errorlevel 1 (
  echo Нужен Apache Maven 3.9+. Установите Maven и повторите.
  exit /b 1
)

echo Запуск «Калькулятор металлурга»...
mvn -q javafx:run
