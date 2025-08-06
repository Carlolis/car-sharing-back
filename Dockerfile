
# Stage 1: EdgeDB
FROM edgedb/edgedb AS edgedb
WORKDIR /myapp
ARG EDGEDB_DSN
COPY gel.toml dbschema ./
RUN edgedb instance link --dsn=${EDGEDB_DSN} --non-interactive --trust-tls-cert db && \
    edgedb migrate -I db

# Stage 2: Build
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /myapp

# Installation des dépendances en une seule couche
RUN apk add --no-cache curl gcompat

# Installation de Coursier et Bleep en une seule couche
RUN curl -fL "https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz" | gzip -d > /usr/local/bin/cs && \
    chmod +x /usr/local/bin/cs && \
    cs install --channel https://raw.githubusercontent.com/oyvindberg/bleep/master/coursier-channel.json bleep && \
    mv /root/.local/share/coursier/bin/bleep /usr/local/bin/ && \
    rm -rf /root/.cache /root/.local

# Copier uniquement les fichiers nécessaires pour le build
COPY build.bleep.yaml bleep.yaml ./
COPY src ./src/
COPY project ./project/

# Build
RUN bleep dist web && \
    rm -rf /root/.cache /root/.local /tmp/*

# Stage 3: Final
FROM eclipse-temurin:17-jre-alpine AS final
WORKDIR /myapp

# Installation de gcompat en une seule couche
RUN apk add --no-cache gcompat && \
    adduser -S -u 1000 appuser && \
    mkdir -p /myapp/dist && \
    chown -R appuser:appuser /myapp

# Copier uniquement les fichiers nécessaires
COPY --from=builder /myapp/.bleep/builds/normal/.bloop/web/dist ./dist/
COPY --from=edgedb /myapp/gel.toml ./dist/

# Utiliser un utilisateur non-root
USER appuser

# Exposer les ports nécessaires
EXPOSE 8081 8095

# Définir le point d'entrée
CMD ["/myapp/dist/bin/web"]