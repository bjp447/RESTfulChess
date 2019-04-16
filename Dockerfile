FROM java:8
EXPOSE 8080
ADD /target/jChess-1.5.1.jar jChess-1.5.1.jar
ENTRYPOINT ["java", "-jar", "jChess-1.5.1.jar"]