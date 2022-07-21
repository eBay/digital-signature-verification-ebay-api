FROM openjdk:11
WORKDIR /home/nouser/
COPY src/main/resources/keys src/main/resources/keys
COPY target/signaturevalidation*.jar .
EXPOSE 8080
USER nobody
ENTRYPOINT java -jar "$(ls signaturevalidation*.jar)"