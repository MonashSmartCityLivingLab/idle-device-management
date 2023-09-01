FROM gradle:8-jdk17-jammy AS gradle-builder
WORKDIR /opt/idle-device-management
ENV SITES_CONFIG=config.example.json
COPY . .
RUN gradle build

FROM eclipse-temurin:17-jammy AS idle-device-management
RUN mkdir -p /var/log/smart-city/
RUN ln -sf /dev/stdout /var/log/smart-city/idle-device-management.log
ARG VERSION="0.0.1-SNAPSHOT"
WORKDIR /opt/idle-device-management
# copy jar
COPY --from=gradle-builder /opt/idle-device-management/build/libs/idle-device-management-$VERSION.jar idle-device-management.jar
EXPOSE 4100
CMD ["java", "-jar", "-Dspring.profiles.active=prod", "/opt/idle-device-management/idle-device-management.jar"]