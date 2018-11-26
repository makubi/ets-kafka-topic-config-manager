# ETS Kafka Topic Config Manager

Kafka Topic Config Manager helps you manage the topic configurations of your Kafka clusters.
This is done by providing your configurations in a XML format, the XSD can be found [here](https://github.com/Galeria-Kaufhof/xsds/tree/gh-pages/ets-kafka-topic-config-manager).

## XSD version compatibility

| Kafka Topic Config Manager version | XSD version |
|:----------------------------------:|:-----------:|
| 0.0.1                              | 0.0.3       |

## Supplying Kafka configurations

You have to supply the XML configurations to the manager. This enabled the manager to check differences between the configuration itself and the Kafka cluster and optionally updates the Kafka cluster configurations.

This is best done by creating a Java / Scala project that allows you to provide the configurations. We will go into this in more detail in this README.

There are four things you have to supply (3 and 4 are optional):
1. The manager needs to know how to retrieve the XML configuration files (e.g. from a folder, the classpath, an url,...). This is done by providing a `TopicConfigurationServiceProvider`.
2. The manager needs to know the Kafka server addresses. This is done by providing a HOCON style config. The keys are `environements[].name and environments[].bootstrapServers`. The configuration will be explained later on.
3. The manager calls zero to _n_ `TopicXmlConfigurationValidationServiceProvider` to validate XML configurations.
4. The manager calls zero to _n_ `TopicUpdateListenerServiceProvider` if topic configurations of the cluster are updated.

### Create a project that depends on ets-kafka-topic-config-manager-api

The project must provide a `de.kaufhof.ets.kafkatopicconfigmanager.TopicConfigurationServiceProvider` via the Java [ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html) interface.

The `TopicConfigurationServiceProvider` is located in the `de.kaufhof.ets:ets-kafka-topic-config-manager-api` JAR.
Use the `provided` scope, because the api JAR will be provided by the manager JAR later.

### Provide topic configurations

You can use the just created project or another one or just package the topics however you like.
They must be provided to the manager later as well.

The example uses the same project for the `TopicConfigurationServiceProvider` and the configurations.

### Provide the manager config

You can provide the manager config via an `application.conf` file in HOCON format by specifying, e.g.
```
environments = [
  {
    name: env1
    bootstrapServers: kafka1-env1:9092,kafka2-env1:9092
  }
  {
    name: env2
    bootstrapServers: kafka1-env2:9092,kafka2-env2:9092
  }
]
```

Alternatively you can also specify the configuration via Java system properties like
`-Denvironments.0.name=env1 -Denvironments.0.bootstrapServers=kafka1-env1:9092,kafka2-env1:9092`.

### Provide the TopicConfigurationsServiceProvider and the configs to the manager

The manager JARs, configuration provider and topic configuration files must be visible to the classpath.

You can then execute the `de.kaufhof.ets.kafkatopicconfigmanager.Main` class.

`java -cp "manager/*:providers/*:configs/*" de.kaufhof.ets.kafkatopicconfigmanager.Main`

### Example
For an example see the `ets-kafka-topic-config-manager-example` module.
It packages all JAR files to a single `tar.gz`.
The configurations are packaged in the same JAR as the `TopicConfigurationsServiceProvider`.

You have to provide the JARs packaged in the `tar.gz` file of the manager and the ones from your configuration JAR(s) and execute the `de.kaufhof.ets.kafkatopicconfigmanager.Main` Main class.

Example

```
$ tar -xf "manager.tar.gz" -C "manager"
$ tree manager
manager
├── config-1.3.2.jar
├── kafka-clients-1.1.1.jar
├── ets-kafka-topic-config-manager-api-1.0-SNAPSHOT.jar
├── ets-kafka-topic-config-manager-core-1.0-SNAPSHOT.jar
├── logback-classic-1.2.3.jar
├── logback-core-1.2.3.jar
├── lz4-java-1.4.1.jar
├── scala-library-2.12.4.jar
├── slf4j-api-1.7.25.jar
└── snappy-java-1.1.7.1.jar
$ tar -xf "configs.tar.gz" -C "configs"
$ tree configs
  configs
  ├── ets-kafka-topic-config-manager-example-1.0-SNAPSHOT.jar
  ├── spring-core-5.0.1.RELEASE.jar
  └── spring-jcl-5.0.1.RELEASE.jar

$ java -cp "manager/*:configs/*" de.kaufhof.ets.kafkatopicconfigmanager.Main
``` 

Attention: The `VERSION` must be the same across the dependency you use in your configurations JAR (`de.kaufhof.ets:ets-kafka-topic-config-manager-api`) and the `ets-kafka-topic-config-manager-core` JAR / Docker image you use to manage your topic configurations.


## Auto apply

By default, config differences are only printed without changing any configurations and topics of the Kafka cluster.

You can enable auto apply by supplying `-DautoApply=true` as Java system property.

## Run docker image

**The image is not published yet**

The Docker container utilizes all JARs in `/opt/topic-configs` and `/opt/config-providers` by adding them to the classpath.
This allows you to supply topic configs by mounting them to `/opt/topic-configs` and config providers to `/opt/config-providers`.

Example:

`docker run --rm -v /path/to/your/topics/jar:/opt/topic-configs/topics.jar $DOCKER_IMAGE_NAME`

You can also provide Java Opts via the `JAVA_OPTS` env var via `-e JAVA_OPTS=...`.

This allows you to enable auto apply via

`docker run --rm -v /path/to/your/topics/jar:/opt/topic-configs/topics.jar -e JAVA_OPTS="-DautoApply=true" $DOCKER_IMAGE_NAME`

## License

The Kafka Topic Config Manager is released under the MIT License.