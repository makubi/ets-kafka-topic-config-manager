package de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties.configs

import de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties.configs.cleanuppolicy.{CheckCompaction, CheckDelete}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.properties.configs.{KafkaCleanupPolicy, XmlCleanupPolicy}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{Environment, Topic}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{ConfigurationComparison, ValidationResult}


class CheckCleanupPolicy(xmlCleanupPolicy: XmlCleanupPolicy, kafkaCleanupPolicy: KafkaCleanupPolicy)(implicit environment: Environment, topic: Topic) extends ConfigurationComparison {

  override def validate(): ValidationResult = {

    new CheckCompaction(xmlCleanupPolicy.compact, kafkaCleanupPolicy.compact).validate() +
    new CheckDelete(xmlCleanupPolicy.delete, kafkaCleanupPolicy.delete).validate()
  }
}
