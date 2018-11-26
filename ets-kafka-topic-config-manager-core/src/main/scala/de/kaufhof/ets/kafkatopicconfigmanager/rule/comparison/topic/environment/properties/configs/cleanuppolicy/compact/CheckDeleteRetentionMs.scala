package de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties.configs.cleanuppolicy.compact

import de.kaufhof.ets.kafkatopicconfigmanager.modelparser.KafkaToModelParser
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{Environment, Topic}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{ConfigurationMissingInXml, ConfigurationMissingOnKafka, ConfigurationWithKeyError, DifferentConfigurationError, _}

object CheckDeleteRetentionMs {
  trait DeleteRetentionMsConfigurationError extends ConfigurationWithKeyError {
    override val key: String = KafkaToModelParser.ConfigKey.`delete.retention.ms`
  }

  case class DifferentDeleteRetentionMs(override val xmlValue: Long, override val kafkaValue: Long, override val environment: Environment, override val topic: Topic) extends DeleteRetentionMsConfigurationError with DifferentConfigurationError[Long]
  case class DeleteRetentionMsMissingForKafka(override val xmlValue: Long, override val environment: Environment, override val topic: Topic) extends DeleteRetentionMsConfigurationError with ConfigurationMissingOnKafka[Long]
  case class DeleteRetentionMsMissingForXml(override val kafkaValue: Long, override val environment: Environment, override val topic: Topic) extends DeleteRetentionMsConfigurationError with ConfigurationMissingInXml[Long]
}

class CheckDeleteRetentionMs(optionalXmlDeleteRetentionMs: Option[Long], optionalKafkaDeleteRetentionMs: Option[Long])(implicit environment: Environment, topic: Topic) extends ConfigurationComparison {
  import CheckDeleteRetentionMs._

  override def validate(): ValidationResult = {
    (optionalXmlDeleteRetentionMs, optionalKafkaDeleteRetentionMs) match {

      case (Some(xmlDeleteRetentionMs), Some(kafkaDeleteRetentionMs)) =>
        compareWithErrorResult(xmlDeleteRetentionMs, kafkaDeleteRetentionMs, DifferentDeleteRetentionMs)

      case (Some(xmlDeleteRetentionMs), None) =>
        DeleteRetentionMsMissingForKafka(xmlDeleteRetentionMs, environment, topic)

      case (None, Some(kafkaDeleteRetentionMs)) =>
        DeleteRetentionMsMissingForXml(kafkaDeleteRetentionMs, environment, topic)

      case (None, None) =>
        ValidationSuccess
    }
  }

}
