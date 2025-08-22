@echo off
setlocal

set SERVICE_NAME=ConstructionFinance
set GRADLE_DIR=%~dp0back-end\ConstructionFinance
set APP_DIR=C:\apps\construction-finance
set FINAL_JAR=%APP_DIR%\app.jar
set NSSM_EXE=nssm

echo === Parando servico %SERVICE_NAME% ===
%NSSM_EXE% stop %SERVICE_NAME%
timeout /t 3 >nul

echo === Atualizando codigo (git pull) ===
cd /d "%~dp0"
git pull || goto :fail

echo === Buildando JAR com Gradle ===
cd /d "%GRADLE_DIR%"
call gradlew clean bootJar --no-daemon || goto :fail

echo === Copiando JAR ===
for %%f in (build\libs\*.jar) do (
  copy /Y "%%f" "%FINAL_JAR%"
  goto :copydone
)

:copydone
%NSSM_EXE% set %SERVICE_NAME% AppDirectory "%APP_DIR%"
%NSSM_EXE% set %SERVICE_NAME% AppParameters "-jar \"%FINAL_JAR%\" --spring.profiles.active=prod"
%NSSM_EXE% start %SERVICE_NAME%

echo === DEPLOY CONCLUIDO ===
goto :end

:fail
echo [ERRO] Falha no deploy!

:end
pause
endlocal
