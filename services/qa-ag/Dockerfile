FROM maven:3.6.3-jdk-8
RUN apt-get update && apt-get upgrade -y
COPY qa-ag /usr/src/app/qa-ag
WORKDIR /usr/src/app/qa-ag
RUN mvn clean package
## COPY /usr/src/app/qa-ag/target/QSGatewayService.jar QSGatewayService.jar
EXPOSE 8080
ENTRYPOINT  java -jar target/QSGatewayService.jar