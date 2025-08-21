@echo off
setlocal

REM ======== CONFIGURACAO ========
set SERVICE_NAME=ConstructionFinance
set GIT_DIR=C:\apps\construction-finance
set GRADLE_DIR=C:\apps\construction-finance\back-end\ConstructionFinance
set APP_DIR=C:\apps\construction-finance
set FINAL_JAR=%APP_DIR%\app.jar
set NSSM_EXE=nssm
REM ==============================

echo === Parando servico %SERVICE_NAME% ===
%NSSM_EXE% stop %SERVICE_NAME%
timeout /t 3 >nul

echo === Atualizando codigo (git pull) ===
cd /d "%GIT_DIR%"
git pull || goto :fail

echo === Buildando JAR com Gradle ===
cd /d "%GRADLE_DIR%"
call gradlew clean bootJar --no-daemon || goto :fail

echo === Copiando JAR para %FINAL_JAR% ===
for %%f in (build\libs\*.jar) do (
  echo Copiando %%f
  copy /Y "%%f" "%FINAL_JAR%"
  goto :copydone
)

:copydone
echo === Garantindo profile PROD no NSSM ===
%NSSM_EXE% set %SERVICE_NAME% AppDirectory "%APP_DIR%"
%NSSM_EXE% set %SERVICE_NAME% AppParameters "-jar \"%FINAL_JAR%\" --spring.profiles.active=prod"

echo === Iniciando servico %SERVICE_NAME% ===
%NSSM_EXE% start %SERVICE_NAME%
timeout /t 3 >nul

echo.
echo ====== DEPLOY CONCLUIDO ======
goto :end

:fail
echo.
echo ====== FALHA NO DEPLOY ======

:end
pause
endlocal
