FROM maven:3.8.4-openjdk-17 as builder

ARG VERSION

WORKDIR /project
COPY . /project/
RUN mvn -Drevision=$VERSION package -DskipTests -B

FROM eclipse-temurin:17.0.2_8-jre-alpine

ARG VERSION

ENV CONFIG_FILE application-local.yaml

WORKDIR /application
COPY --from=builder /project/client/target/*-fat.jar /application/payments-client-$VERSION.jar

ENTRYPOINT ["sh", "-c"]
CMD ["exec java  -Dconfig.file=$CONFIG_FILE -jar payments-client-$VERSION.jar --input=$INPUT --output=$OUTPUT"]
