package de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties.configs.cleanuppolicy.delete

import de.kaufhof.ets.kafkatopicconfigmanager.modelparser.KafkaToModelParser
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{Environment, Topic}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{DifferentConfigurationError, _}

object CheckRetentionMs {
  case class DifferentRetentionMs(override val xmlValue: Long, override val kafkaValue: Long, override val environment: Environment, override val topic: Topic) extends DifferentConfigurationError[Long] {
    override val key: String = KafkaToModelParser.ConfigKey.`retention.ms`
  }
  case class RetentionMsMissingForKafka(xmlRetentionMs: Long) extends ValidationError
  case class RetentionMsMissingForXml(kafkaRetentionMs: Long) extends ValidationError
}

class CheckRetentionMs(optionalXmlRetentionMs: Option[Long], optionalKafkaRetentionMs: Option[Long])(implicit environment: Environment, topic: Topic) extends ConfigurationComparison {
  import CheckRetentionMs._

  override def validate(): ValidationResult = {
    (optionalXmlRetentionMs, optionalKafkaRetentionMs) match {

      case (Some(xmlRetentionMs), Some(kafkaRetentionMs)) =>
        compareWithErrorResult(xmlRetentionMs, kafkaRetentionMs, DifferentRetentionMs)

      case (Some(xmlRetentionMs), None) =>
        RetentionMsMissingForKafka(xmlRetentionMs)

      case (None, Some(kafkaRetentionMs)) =>
        RetentionMsMissingForXml(kafkaRetentionMs)

      case (None, None) =>
        ValidationSuccess
    }
  }

}
