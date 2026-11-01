@echo off
setlocal EnableExtensions
chcp 65001 >nul
cd /d "%~dp0.."

where java >nul 2>&1
if errorlevel 1 (
  echo Нужен JDK 21 или новее. Установите Java и откройте новое окно командной строки.
  goto :fail
)

where mvn >nul 2>&1
if errorlevel 1 (
  echo Нужен Apache Maven 3.9 или новее.
  goto :fail
)

set "JAVA_VER="
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
  set "JAVA_VER=%%~v"
  goto :ver_ok
)
:ver_ok
if not defined JAVA_VER (
  echo Не удалось прочитать версию Java.
  goto :fail
)

for /f "tokens=1 delims=." %%m in ("%JAVA_VER%") do set "JAVA_MAJOR=%%m"
if "%JAVA_MAJOR%"=="1" set "JAVA_MAJOR=0"
if %JAVA_MAJOR% LSS 21 (
  echo Нужен JDK 21 или новее. Сейчас установлена %JAVA_VER%.
  goto :fail
)

echo Запуск «Калькулятор металлурга»...
rem mvn на Windows — это mvn.cmd. Без call этот файл завершится вместе с Maven.
call mvn -q javafx:run
set "RC=%ERRORLEVEL%"
if not "%RC%"=="0" (
  echo.
  echo Запуск не удался, код %RC%.
  goto :fail
)
endlocal
exit /b 0

:fail
echo.
pause
endlocal
exit /b 1
