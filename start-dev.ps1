$ErrorActionPreference = "Stop"

Set-Location $PSScriptRoot

# Configuracion de Spring Boot
$env:SERVER_PORT = "8082"

# Configuracion de MySQL
$env:DB_HOST = "127.0.0.1"
$env:MYSQL_PORT = "3307"
$env:MYSQL_DATABASE = "biblioteca_ivan"
$env:MYSQL_USER = "biblioteca_user"

# Arrancar MySQL
docker start mysql_libreria

if ($LASTEXITCODE -ne 0) {
    throw "No se ha podido arrancar MySQL"
}

# Recuperar la contrasena
$mysqlPassword = docker inspect --format '{{range .Config.Env}}{{println .}}{{end}}' mysql_libreria |
    Where-Object { $_ -match '^MYSQL_PASSWORD=' }

if (-not $mysqlPassword) {
    throw "No se ha encontrado la contrasena de MySQL"
}

$env:MYSQL_PASSWORD = ($mysqlPassword -split '=', 2)[1]

# Esperar a que MySQL este disponible
for ($i = 0; $i -lt 30; $i++) {
    $health = docker inspect --format '{{.State.Health.Status}}' mysql_libreria

    if ($health -eq "healthy") {
        break
    }

    Start-Sleep -Seconds 2
}

if ($health -ne "healthy") {
    throw "MySQL no esta disponible"
}

# Comprobar que el puerto esta libre
$ocupado = Get-NetTCPConnection -LocalPort 8082 -State Listen -ErrorAction SilentlyContinue

if ($ocupado) {
    $ocupado | Select-Object LocalPort, OwningProcess
    throw "El puerto 8082 esta ocupado"
}

Write-Host "MySQL disponible" -ForegroundColor Green
Write-Host "Puerto 8082 libre" -ForegroundColor Green
Write-Host "Arrancando Espresso Library..." -ForegroundColor Cyan

.\mvnw.cmd spring-boot:run