param(
    [Parameter(Position = 0)]
    [string]$JavaHome = "C:\Program Files\Java\jdk-21.0.6"
)

Write-Host "=============================================="
Write-Host " Configurando JAVA_HOME para o simulador P2P"
Write-Host "=============================================="
Write-Host "Destino desejado: $JavaHome"
Write-Host

if (-not (Test-Path $JavaHome)) {
    Write-Host "[ERRO] Diretório não encontrado: $JavaHome" -ForegroundColor Red
    Write-Host "Informe o caminho completo da instalação do Java 17+"
    Write-Host "Exemplo: .\scripts\setup-java17.ps1 \"C:\Program Files\Java\jdk-17\""
    exit 1
}

$javaExe = Join-Path $JavaHome "bin\java.exe"
if (-not (Test-Path $javaExe)) {
    Write-Host "[ERRO] Arquivo não encontrado: $javaExe" -ForegroundColor Red
    Write-Host "Verifique se o diretório informado contém a pasta 'bin' com o java.exe"
    exit 1
}

Write-Host "[OK] Java encontrado em $javaExe"

# Configura variáveis no escopo do usuário (não requer admin)
try {
    [System.Environment]::SetEnvironmentVariable("JAVA_HOME", $JavaHome, "User")
    Write-Host "[OK] JAVA_HOME configurado para $JavaHome"

    $currentPath = [System.Environment]::GetEnvironmentVariable("Path", "User")
    if ([string]::IsNullOrWhiteSpace($currentPath)) {
        $currentPath = ""
    }

    $binPath = Join-Path $JavaHome "bin"
    if ($currentPath -notlike "*$binPath*") {
        $newPath = if ($currentPath) { "$binPath;$currentPath" } else { $binPath }
        [System.Environment]::SetEnvironmentVariable("Path", $newPath, "User")
        Write-Host "[OK] PATH atualizado para incluir $binPath"
    } else {
        Write-Host "[INFO] PATH já contém $binPath"
    }
}
catch {
    Write-Host "[ERRO] Não foi possível atualizar as variáveis de ambiente: $_" -ForegroundColor Red
    Write-Host "Execute o PowerShell como administrador ou ajuste manualmente"
    exit 1
}

Write-Host
Write-Host "Tudo pronto! Abra um novo terminal e execute:'" -ForegroundColor Green
Write-Host "    java -version"
Write-Host "para confirmar que o Java 17+ está ativo."
Write-Host
Write-Host "Para executar o simulador depois disso:" -ForegroundColor Yellow
Write-Host "    mvn clean package"
Write-Host "    java -jar target\p2p-simulator.jar"
