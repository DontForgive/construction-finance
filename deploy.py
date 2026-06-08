import subprocess
import shutil
import os
import sys

SERVICE_NAME = "ConstructionFinance"
GRADLE_DIR = r"back-end\ConstructionFinance"
FRONTEND_DIR = r"front-end"
DIST_DIR = os.path.join(FRONTEND_DIR, "dist")
DEPLOY_DIR = r"C:\inetpub\wwwroot\constructionFinanceWeb"
APP_DIR = r"C:\apps\construction-finance"
FINAL_JAR = os.path.join(APP_DIR, "app.jar")
NSSM_EXE = "nssm"

# Detecta o gradlew correto (Windows/Linux)
GRADLEW_CMD = "gradlew.bat" if os.name == "nt" else "./gradlew"


def run(cmd, cwd=None):
    print(f"=== Executando: {cmd}")
    result = subprocess.run(cmd, shell=True, cwd=cwd)
    if result.returncode != 0:
        raise RuntimeError(f"Erro ao executar: {cmd}")


try:
    # 1. Parar serviço
    run(f"{NSSM_EXE} stop {SERVICE_NAME}")

    # 2. Git pull
    run("git pull")

    # 3. Build backend
    run(f"{GRADLEW_CMD} clean bootJar --no-daemon", cwd=GRADLE_DIR)

    # 4. Copiar JAR
    libs_dir = os.path.join(GRADLE_DIR, "build", "libs")
    jar_found = False
    for f in os.listdir(libs_dir):
        if f.endswith(".jar"):
            src = os.path.join(libs_dir, f)
            shutil.copy2(src, FINAL_JAR)
            print(f"=== Copiado JAR: {src} -> {FINAL_JAR}")
            jar_found = True
            break
    if not jar_found:
        raise FileNotFoundError("Nenhum JAR encontrado em build/libs!")

    # 5. Build frontend
    run("npm install", cwd=FRONTEND_DIR)
    run("ng build --configuration production", cwd=FRONTEND_DIR)

    # 6. Copiar dist para inetpub preservando web.config
    if os.path.exists(DEPLOY_DIR):
        webconfig_path = os.path.join(DEPLOY_DIR, "web.config")
        temp_webconfig = None

        if os.path.exists(webconfig_path):
            temp_webconfig = os.path.join(os.getcwd(), "web.config.temp")
            shutil.copy2(webconfig_path, temp_webconfig)

        shutil.rmtree(DEPLOY_DIR)
        shutil.copytree(DIST_DIR, DEPLOY_DIR)

        if temp_webconfig and os.path.exists(temp_webconfig):
            shutil.copy2(temp_webconfig, webconfig_path)
            os.remove(temp_webconfig)
    else:
        shutil.copytree(DIST_DIR, DEPLOY_DIR)

    # 7. Reiniciar serviço
    run(f'{NSSM_EXE} set {SERVICE_NAME} AppDirectory "{APP_DIR}"')
    run(f'{NSSM_EXE} set {SERVICE_NAME} AppParameters "-jar {FINAL_JAR} --spring.profiles.active=prod"')
    run(f"{NSSM_EXE} start {SERVICE_NAME}")

    print("=== DEPLOY CONCLUIDO ===")

except Exception as e:
    print(f"[ERRO] {e}")
    sys.exit(1)