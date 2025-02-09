# Utiliser une image de base légère
FROM openjdk:17-jdk

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
RUN bleep dist back

# Définir le point d'entrée (optionnel)
ENTRYPOINT [""]
