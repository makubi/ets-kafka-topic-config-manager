package de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties.configs

import de.kaufhof.ets.kafkatopicconfigmanager.modelparser.KafkaToModelParser
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{Environment, Topic}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{DifferentConfigurationError, _}

object CheckMessageFormatVersion {
  case class DifferentMessageFormatVersion(override val xmlValue: String, override val kafkaValue: String, override val environment: Environment, override val topic: Topic) extends DifferentConfigurationError[String] {
    override val key: String = KafkaToModelParser.ConfigKey.`message.format.version`
  }
  case class MessageFormatVersionMissingForKafka(xmlMessageFormatVersion: String) extends ValidationError
  case class MessageFormatVersionMissingForXml(kafkaMessageFormatVersion: String) extends ValidationError
}

class CheckMessageFormatVersion(optionalXmlMessageFormatVersion: Option[String], optionalKafkaMessageFormatVersion: Option[String])(implicit environment: Environment, topic: Topic) extends ConfigurationComparison {
  import CheckMessageFormatVersion._

  override def validate(): ValidationResult = {
    (optionalXmlMessageFormatVersion, optionalKafkaMessageFormatVersion) match {

      case (Some(xmlMessageFormatVersion), Some(kafkaMessageFormatVersion)) =>
        compareComparableWithErrorResult(xmlMessageFormatVersion, kafkaMessageFormatVersion, DifferentMessageFormatVersion)

      case (Some(xmlMessageFormatVersion), None) =>
        MessageFormatVersionMissingForKafka(xmlMessageFormatVersion)

      case (None, Some(kafkaMessageFormatVersion)) =>
        MessageFormatVersionMissingForXml(kafkaMessageFormatVersion)

      case (None, None) =>
        ValidationSuccess
    }
  }

}
