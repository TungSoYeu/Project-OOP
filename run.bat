@echo off
setlocal

set "PROJECT_JDK=C:\Program Files\Java\jdk-26"
if exist "%PROJECT_JDK%\bin\java.exe" (
    set "JAVA_HOME=%PROJECT_JDK%"
    set "PATH=%PROJECT_JDK%\bin;%PATH%"
)

where java >nul 2>nul
if errorlevel 1 (
    echo Java chua co trong PATH. Hay cai JDK 26 roi chay lai.
    exit /b 1
)

java -version

call "%~dp0mvnw.cmd" javafx:run
