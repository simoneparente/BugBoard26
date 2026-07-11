FROM postgres:15-alpine

# Default database configuration (overridable via docker-compose or env file)
ENV POSTGRES_DB=bugboard_db
ENV POSTGRES_USER=bugboard
ENV POSTGRES_PASSWORD=change_me
ENV PGDATA=/var/lib/postgresql/data/pgdata

VOLUME ["/var/lib/postgresql/data"]

# Copy initialization SQL scripts if you place them under db/init/
# Any `*.sql` or `*.sh` in that folder will be executed by the official image entrypoint
# COPY init/ /docker-entrypoint-initdb.d/

EXPOSE 5432

# Use official image entrypoint/CMD