# pull in GelDB CLI
FROM edgedb/edgedb AS edgedb
WORKDIR /myapp
ARG EDGEDB_DSN
RUN echo "DSN is =${EDGEDB_DSN}"
COPY gel.toml /myapp/gel.toml
COPY dbschema /myapp/dbschema
RUN edgedb instance link --dsn=${EDGEDB_DSN} --non-interactive --trust-tls-cert db
RUN edgedb migrate -I db


# Utiliser une image de base légère
FROM debian:bookworm-slim AS base
WORKDIR /myapp
RUN apt-get update && apt-get install -y curl gzip openjdk-17-jdk


# Installer Coursier
RUN curl -fL "https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz" | gzip -d > cs
RUN chmod +x cs
RUN mv cs /usr/local/bin/cs

# Installer Bleep
RUN cs install --channel https://raw.githubusercontent.com/oyvindberg/bleep/master/coursier-channel.json bleep
RUN mv /root/.local/share/coursier/bin/bleep /usr/local/bin/

# Copier l'app
COPY . .

# Créer executable
RUN bleep dist web


# Finally, build the production image with minimal footprint
FROM debian:bookworm-slim AS runtime
WORKDIR /myapp
RUN apt-get update && apt-get install -y openjdk-17-jre-headless

COPY --from=base /myapp/.bleep/builds/normal/.bloop/web/dist /myapp/dist
COPY --from=edgedb /myapp/gel.toml /myapp/dist/gel.toml



ENTRYPOINT [""]