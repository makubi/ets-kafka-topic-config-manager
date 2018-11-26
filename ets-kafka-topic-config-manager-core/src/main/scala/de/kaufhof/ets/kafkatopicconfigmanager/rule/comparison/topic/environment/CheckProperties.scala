package de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment

import de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties.{CheckConfigs, CheckNumberOfPartitions, CheckReplicationFactor}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.{KafkaProperties, XmlProperties}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{Environment, Topic}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{ConfigurationComparison, ValidationResult}

class CheckProperties(xmlProperties: XmlProperties, kafkaProperties: KafkaProperties)(implicit environment: Environment, topic: Topic) extends ConfigurationComparison {
  override def validate(): ValidationResult = {
    new CheckConfigs(xmlProperties.xmlConfigs, kafkaProperties.kafkaConfigs).validate() +
    new CheckReplicationFactor(xmlProperties.xmlReplicationFactor, kafkaProperties.kafkaReplicationFactor).validate() +
    new CheckNumberOfPartitions(xmlProperties.xmlPartitions, kafkaProperties.kafkaPartitions).validate()
  }
}
