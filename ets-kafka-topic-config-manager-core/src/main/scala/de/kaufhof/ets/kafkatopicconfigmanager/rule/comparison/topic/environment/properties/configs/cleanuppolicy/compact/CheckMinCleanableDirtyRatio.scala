package de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties.configs.cleanuppolicy.compact

import de.kaufhof.ets.kafkatopicconfigmanager.modelparser.KafkaToModelParser
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{Environment, Topic}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{ConfigurationMissingInXml, ConfigurationMissingOnKafka, ConfigurationWithKeyError, DifferentConfigurationError, _}

object CheckMinCleanableDirtyRatio {
  trait MinCleanableDirtyRatioConfigurationError extends ConfigurationWithKeyError {
    override val key: String = KafkaToModelParser.ConfigKey.`min.cleanable.dirty.ratio`
  }

  case class DifferentMinCleanableDirtyRatio(override val xmlValue: Float, override val kafkaValue: Float, override val environment: Environment, override val topic: Topic) extends MinCleanableDirtyRatioConfigurationError with DifferentConfigurationError[Float]
  case class MinCleanableDirtyRatioMissingForKafka(override val xmlValue: Float, override val environment: Environment, override val topic: Topic) extends MinCleanableDirtyRatioConfigurationError with ConfigurationMissingOnKafka[Float]
  case class MinCleanableDirtyRatioMissingForXml(override val kafkaValue: Float, override val environment: Environment, override val topic: Topic) extends MinCleanableDirtyRatioConfigurationError with ConfigurationMissingInXml[Float]

}

class CheckMinCleanableDirtyRatio(optionalXmlMinCleanableDirtyRatio: Option[Float], optionalKafkaMinCleanableDirtyRatio: Option[Float])(implicit environment: Environment, topic: Topic) extends ConfigurationComparison {
  import CheckMinCleanableDirtyRatio._

  override def validate(): ValidationResult = {
    (optionalXmlMinCleanableDirtyRatio, optionalKafkaMinCleanableDirtyRatio) match {

      case (Some(xmlMinCleanableDirtyRatio), Some(kafkaMinCleanableDirtyRatio)) =>
        compareWithErrorResult(xmlMinCleanableDirtyRatio, kafkaMinCleanableDirtyRatio, DifferentMinCleanableDirtyRatio)

      case (Some(xmlMinCleanableDirtyRatio), None) =>
        MinCleanableDirtyRatioMissingForKafka(xmlMinCleanableDirtyRatio, environment, topic)

      case (None, Some(kafkaMinCleanableDirtyRatio)) =>
        MinCleanableDirtyRatioMissingForXml(kafkaMinCleanableDirtyRatio, environment, topic)

      case (None, None) =>
        ValidationSuccess
    }
  }
}