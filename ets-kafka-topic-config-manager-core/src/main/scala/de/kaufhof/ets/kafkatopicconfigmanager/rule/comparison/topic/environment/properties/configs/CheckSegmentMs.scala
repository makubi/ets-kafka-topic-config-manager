package de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties.configs

import de.kaufhof.ets.kafkatopicconfigmanager.modelparser.KafkaToModelParser
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{Environment, Topic}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{DifferentConfigurationError, EnvironmentAndTopicAware, _}


object CheckSegmentMs {
  case class DifferentSegmentMs(override val xmlValue: Long, override val kafkaValue: Long, override val environment: Environment, override val topic: Topic) extends DifferentConfigurationError[Long] {
    override val key: String = KafkaToModelParser.ConfigKey.`segment.ms`
  }
  case class SegmentMsMissingForKafka(xmlSegmentMs: Long, override val environment: Environment, override val topic: Topic) extends ValidationError with EnvironmentAndTopicAware
  case class SegmentMsMissingForXml(kafkaSegmentMs: Long, override val environment: Environment, override val topic: Topic) extends ValidationError with EnvironmentAndTopicAware
}

class CheckSegmentMs(optionalXmlSegmentMs: Option[Long], optionalKafkaSegmentMs: Option[Long])(implicit environment: Environment, topic: Topic) extends ConfigurationComparison {
  import CheckSegmentMs._

  override def validate(): ValidationResult = {
    compareOptionalValues(optionalXmlSegmentMs, optionalKafkaSegmentMs)(DifferentSegmentMs, SegmentMsMissingForKafka, SegmentMsMissingForXml)
  }

}
