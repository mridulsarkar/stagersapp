From openjdk:17
copy ./target/stagersapp-0.0.1-SNAPSHOT.jar stagersapp-0.0.1-SNAPSHOT.jar
CMD ["java","-jar","stagersapp-0.0.1-SNAPSHOT.jar"]