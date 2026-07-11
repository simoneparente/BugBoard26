# Setup Locale BugBoard26

Questa cartella contiene tutta la guida operativa per avviare il progetto in locale con Podman.

## Contenuto

- setup/.env.example: template variabili ambiente PostgreSQL
- setup/start-db.sh: avvio solo database
- setup/start-stack.sh: build backend + avvio database e app containerizzata
- setup/stop-stack.sh: stop stack compose

## Prerequisiti

- Podman installato
- Maven Wrapper disponibile in bugboard-backend
- Da macOS: Podman machine avviata con `podman machine start`

## Configurazione

1. Copia il template env:
   cp setup/.env.example db/.env

2. Modifica db/.env se vuoi cambiare credenziali/porta.

## Avvio rapido

### Solo PostgreSQL

bash setup/start-db.sh

### Stack completa (db + app)

bash setup/start-stack.sh

Comandi manuali equivalenti:

- ./bugboard-backend/mvnw -q -f ./bugboard-backend/pom.xml -DskipTests clean package
- podman compose --env-file ./db/.env up -d --build app db
- podman compose --env-file ./db/.env ps
- podman logs --tail 120 bugboard26-app-1

## Stop

bash setup/stop-stack.sh

## Verifiche utili

- Stato container:
  podman ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'

- Log app:
  podman logs --tail 120 bugboard26-app-1

- Log db:
  podman logs --tail 120 bugboard26-db-1

- Test connessione postgres:
  podman exec bugboard26-db-1 psql -U bugboard -d bugboard_db -c "SELECT current_user;"

## Troubleshooting

1. Errore role "bugboard" does not exist

Cause comune: volume dati inizializzato in passato con utente diverso.

Fix:
- podman compose --env-file ./db/.env down
- podman volume rm bugboard26_db_data
- podman compose --env-file ./db/.env up -d db

2. Errore Invalid or corrupt jarfile /app/app.jar

Fix:
- ./bugboard-backend/mvnw -q -f ./bugboard-backend/pom.xml -DskipTests clean package
- podman compose --env-file ./db/.env up -d --build app
