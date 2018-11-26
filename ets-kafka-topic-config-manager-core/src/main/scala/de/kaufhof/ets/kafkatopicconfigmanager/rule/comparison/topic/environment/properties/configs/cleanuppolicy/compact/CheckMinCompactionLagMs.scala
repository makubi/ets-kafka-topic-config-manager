package de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties.configs.cleanuppolicy.compact

import de.kaufhof.ets.kafkatopicconfigmanager.modelparser.KafkaToModelParser
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{Environment, Topic}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{ConfigurationMissingInXml, ConfigurationMissingOnKafka, ConfigurationWithKeyError, DifferentConfigurationError, _}

object CheckMinCompactionLagMs {
  trait MinCompactionLagMsConfigurationError extends ConfigurationWithKeyError {
    override val key: String = KafkaToModelParser.ConfigKey.`min.compaction.lag.ms`
  }

  case class DifferentMinCompactionLagMs(override val xmlValue: Long, override val kafkaValue: Long, override val environment: Environment, override val topic: Topic) extends MinCompactionLagMsConfigurationError with DifferentConfigurationError[Long]
  case class MinCompactionLagMsMissingForKafka(override val xmlValue: Long, override val environment: Environment, override val topic: Topic) extends MinCompactionLagMsConfigurationError with ConfigurationMissingOnKafka[Long]
  case class MinCompactionLagMsMissingForXml(override val kafkaValue: Long, override val environment: Environment, override val topic: Topic) extends MinCompactionLagMsConfigurationError with ConfigurationMissingInXml[Long]
}

class CheckMinCompactionLagMs(optionalXmlMinCompactionLagMs: Option[Long], optionalKafkaMinCompactionLagMs: Option[Long])(implicit environment: Environment, topic: Topic) extends ConfigurationComparison {
  import CheckMinCompactionLagMs._

  override def validate(): ValidationResult = {
    (optionalXmlMinCompactionLagMs, optionalKafkaMinCompactionLagMs) match {

      case (Some(xmlMinCompactionLagMs), Some(kafkaMinCompactionLagMs)) =>
        compareWithErrorResult(xmlMinCompactionLagMs, kafkaMinCompactionLagMs, DifferentMinCompactionLagMs)

      case (Some(xmlMinCompactionLagMs), None) =>
        MinCompactionLagMsMissingForKafka(xmlMinCompactionLagMs, environment, topic)

      case (None, Some(kafkaMinCompactionLagMs)) =>
        MinCompactionLagMsMissingForXml(kafkaMinCompactionLagMs, environment, topic)

      case (None, None) =>
        ValidationSuccess
    }
  }

}
