from openjdk:26-ea-21-oraclelinux8

copy target/e-commerce-techshop-0.0.1-SNAPSHOT.jar e-commerce-techshop-0.0.1-SNAPSHOT.jar

cmd ["java", "-jar", "e-commerce-techshop-0.0.1-SNAPSHOT.jar"]