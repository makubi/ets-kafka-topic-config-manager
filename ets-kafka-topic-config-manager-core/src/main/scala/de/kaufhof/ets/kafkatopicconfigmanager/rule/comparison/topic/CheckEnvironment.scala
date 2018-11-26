package de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic

import de.kaufhof.ets.kafkatopicconfigmanager.modelparser.KafkaToModelParser
import de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.CheckProperties
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{Environment, KafkaEnvironment, Topic, XmlEnvironment}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.validation.{CheckXmlReplicationFactorGreaterThanMinInsyncReplica, CheckXmlUncleanLeaderElectionAndMinInsyncReplica}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{ConfigurationComparison, ValidationResult}

class CheckEnvironment(xmlEnvironment: XmlEnvironment, kafkaEnvironment: KafkaEnvironment)(implicit topic: Topic) extends ConfigurationComparison {

  private val defaultUncleanLeaderElection = false

  override def validate(): ValidationResult = {
    if (xmlEnvironment.name != kafkaEnvironment.name) {
      throw new IllegalArgumentException(s"XML and Kafka environment names differ: ${xmlEnvironment.name}, ${kafkaEnvironment.name}")
    } else {
      implicit val environment: Environment = Environment(xmlEnvironment.name)

      new CheckXmlReplicationFactorGreaterThanMinInsyncReplica(xmlEnvironment.xmlProperties.xmlReplicationFactor, xmlEnvironment.xmlProperties.xmlConfigs.xmlMinInsyncReplicas.getOrElse(throw new RuntimeException(s"${KafkaToModelParser.ConfigKey.`min.insync.replicas`} is missing in XML"))).validate() +
      new CheckXmlUncleanLeaderElectionAndMinInsyncReplica(
        xmlEnvironment.xmlProperties.xmlConfigs.xmlUncleanLeaderElection.getOrElse(defaultUncleanLeaderElection),
        xmlEnvironment.xmlProperties.xmlConfigs.xmlMinInsyncReplicas.getOrElse(throw new RuntimeException(s"${KafkaToModelParser.ConfigKey.`min.insync.replicas`} is missing in XML"))
      ).validate() +
      new CheckProperties(xmlEnvironment.xmlProperties, kafkaEnvironment.kafkaProperties)(Environment(xmlEnvironment.name), topic).validate()
    }
  }
}
