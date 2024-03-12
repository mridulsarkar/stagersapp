FROM openjdk:17-alpine
ENV APP_HOME=/usr/app/  
WORKDIR $APP_HOME
COPY ./target/stagersapp-0.0.1-SNAPSHOT.jar stagersapp-0.0.1-SNAPSHOT.jar
EXPOSE 8081
CMD ["java","-jar","stagersapp-0.0.1-SNAPSHOT.jar"]