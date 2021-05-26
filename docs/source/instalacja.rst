==========
Instalacja
==========
Instrukcja uruchamiania aplikacji za pomocą *docker-compose*
------------------------------------------------------------
1. Upewnij się, że masz zainstalowany *Docker* oraz *Docker compose*. Będzie również używany *postgres*.

2. W głównym katalogu projektu stwórz plik *Dockerfile* z zawartością:

::

    FROM gradle:6.3.0-jdk8 AS build
    USER root
    RUN mkdir app
    COPY src/ /app/src/
    COPY build.gradle /app/
    COPY settings.gradle /app/
    WORKDIR /app
    RUN gradle bootJar

    FROM openjdk:8-jdk-alpine
    COPY --from=build /app/build/libs/name.jar name.jar
    ENTRYPOINT ["java","-jar","/name.jar"]

W miejscach *name* należy wpisać nazwę pliku *.jar*, który otworzył się w *build/libs*.
Dodatkowo należy pamiętać, ze wersja *gradle* nie może być starsza niż *6.3.0*.

3. Możesz teraz zbudować obraz wpisując w terminal: ``docker build -t full-spring-boot .``


4. W głównym folderze projektu stwórz katalog: *docker-entrypoint-initdb.d*.

5. W katalogu *docker-entrypoint-initdb.d* utwórz plik inicjalizacyjny *create-databases.sh* z zawartością:

::

    #!/bin/bash
    set -e

    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
        CREATE USER docker;
        CREATE DATABASE docker;
        GRANT ALL PRIVILEGES ON DATABASE docker TO docker;
        EOSQL

6. Do głównego folderu porjektu dodaj plik *docker-compose.yaml* (lub z rozszerzeniem *.yml*) z zawartością:

::

    version: "3"
    services:
        app:
            image: "app/customer"
            build:
                context: .
                dockerfile: "Dockerfile"
            environment:
                POSTGRES_USER: ${POSTGRES_USER}
                POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
            ports:
                - 8080:8080
        db:
            image: postgres:latest
            volumes:
                - "./docker-entrypoint-initdb.d:/docker-entrypoint-initdb.d"
            environment:
                POSTGRES_USER: ${POSTGRES_USER}
                POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
            ports:
                - ${DB_PORT}:5432

7. Do głównego folderu dodaj plik *.env* z zawartością:

::

    POSTGRES_USER=postgres
    POSTGRES_PASSWORD=mysecretpass
    DB_PORT=5432

8. Skonfiguruj *application.properties*:

::

    server.port=8080

    ## Spring DATASOURCE (DataSourceAutoConfiguration & DataSourceProperties)
    spring.datasource.url=jdbc:postgresql://db:5432/docker
    spring.datasource.username=${POSTGRES_USER}
    spring.datasource.password=${POSTGRES_PASSWORD}

    # The SQL dialect makes Hibernate generate better SQL for the chosen database
    spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.PostgreSQLDialect

    # Hibernate ddl auto (create, create-drop, validate, update)
    spring.jpa.hibernate.ddl-auto = update''

9. Zbuduj obraz za pomocą polecenia: ``docker compose build``.

10. Uruchom: ``docker compose up``.