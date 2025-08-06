# Stage 1: EdgeDB
FROM edgedb/edgedb AS edgedb
WORKDIR /app
ARG EDGEDB_DSN

# Copier les fichiers EdgeDB
COPY dbschema/ ./dbschema/
COPY gel.toml ./
RUN echo "DSN is ${EDGEDB_DSN}" && \
    edgedb instance link --dsn="${EDGEDB_DSN}" --non-interactive --trust-tls-cert db && \
    edgedb migrate -I db

# Stage 2: Build avec Scala
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# Installation des dépendances nécessaires
RUN apt-get update && apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Installation de Coursier et Bleep
RUN curl -fL "https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz" | gzip -d > /usr/local/bin/cs && \
    chmod +x /usr/local/bin/cs && \
    cs install --channel https://raw.githubusercontent.com/oyvindberg/bleep/master/coursier-channel.json bleep && \
    mv /root/.local/share/coursier/bin/bleep /