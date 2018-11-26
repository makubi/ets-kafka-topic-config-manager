package de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties.configs.cleanuppolicy

import de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties.configs.cleanuppolicy.CheckCompaction.{CleanupPolicyCompactMissingForKafka, CleanupPolicyCompactMissingForXml}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties.configs.cleanuppolicy.compact.{CheckDeleteRetentionMs, CheckMinCleanableDirtyRatio, CheckMinCompactionLagMs}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{Environment, Topic}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.properties.configs.cleanuppolicy.{KafkaCompact, XmlCompact}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{ConfigurationError, _}

object CheckCompaction {
  case class CleanupPolicyCompactMissingForKafka(override val environment: Environment, override val topic: Topic) extends ConfigurationError
  case class CleanupPolicyCompactMissingForXml(override val environment: Environment, override val topic: Topic) extends ConfigurationError
}

class CheckCompaction(optionalXmlCompact: Option[XmlCompact], optionalKafkaCompact: Option[KafkaCompact])(implicit environment: Environment, topic: Topic) extends ConfigurationComparison {

  override def validate(): ValidationResult = {

    (optionalXmlCompact, optionalKafkaCompact) match {

      case (Some(xmlCompact), Some(kafkaCompact)) =>
        new CheckDeleteRetentionMs(xmlCompact.deleteRetentionMs, kafkaCompact.deleteRetentionMs).validate() +
        new CheckMinCleanableDirtyRatio(xmlCompact.minCleanableDirtyRatio, kafkaCompact.minCleanableDirtyRatio).validate() +
        new CheckMinCompactionLagMs(xmlCompact.minCompactionLagMs, kafkaCompact.minCompactionLagMs).validate()

      case (Some(xmlCompact), None) =>
        CleanupPolicyCompactMissingForKafka(environment, topic)

      case (None, Some(kafkaCompact)) =>
        CleanupPolicyCompactMissingForXml(environment, topic)

      case (None, None) =>
        ValidationSuccess
    }
  }

}
