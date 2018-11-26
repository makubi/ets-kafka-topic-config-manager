package de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties.configs.cleanuppolicy

import de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties.configs.cleanuppolicy.CheckDelete.{CleanupPolicyDeleteMissingForKafka, CleanupPolicyDeleteMissingForXml}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties.configs.cleanuppolicy.delete.CheckRetentionMs
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{Environment, Topic}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.properties.configs.cleanuppolicy.{KafkaDelete, XmlDelete}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{ConfigurationError, _}

object CheckDelete {
  case class CleanupPolicyDeleteMissingForKafka(override val environment: Environment, override val topic: Topic) extends ConfigurationError
  case class CleanupPolicyDeleteMissingForXml(override val environment: Environment, override val topic: Topic) extends ConfigurationError
}

class CheckDelete(optionalXmlDelete: Option[XmlDelete], optionalKafkaDelete: Option[KafkaDelete])(implicit environment: Environment, topic: Topic) extends ConfigurationComparison {

  override def validate(): ValidationResult = {

    (optionalXmlDelete, optionalKafkaDelete) match {

      case (Some(xmlDelete), Some(kafkaDelete)) =>
        new CheckRetentionMs(xmlDelete.retentionMs, kafkaDelete.retentionMs).validate()

      case (Some(xmlDelete), None) =>
        CleanupPolicyDeleteMissingForKafka(environment, topic)

      case (None, Some(kafkaDelete)) =>
        CleanupPolicyDeleteMissingForXml(environment, topic)

      case (None, None) =>
        ValidationSuccess
    }
  }

}
