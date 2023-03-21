FROM maven:3.6.3-jdk-8
RUN apt-get update && apt-get upgrade -y
COPY services/qs-us /usr/src/app/qs-us
WORKDIR /usr/src/app/qs-us
RUN mvn clean install
EXPOSE 8081
ENTRYPOINT java -jar target/QSUserServiceApi.jar
