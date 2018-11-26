FROM maven as build

COPY . /build
WORKDIR /build
RUN mvn -B clean package
RUN mkdir /tmp/ets-kafka-topic-config-manager
RUN tar -xzf ets-kafka-topic-config-manager-core/target/ets-kafka-topic-config-manager-core-*-ets-assembly-descriptors-application.tar.gz -C /tmp/ets-kafka-topic-config-manager

FROM java:jre

COPY --from=build /tmp/ets-kafka-topic-config-manager/ /opt/ets-kafka-topic-config-manager/
CMD java $JAVA_OPTS -cp "/opt/ets-kafka-topic-config-manager/*:/opt/topic-configs/*:/opt/topic-configs:/opt/config-provider/*:/opt/config-provider" de.kaufhof.ets.kafkatopicconfigmanager.Main