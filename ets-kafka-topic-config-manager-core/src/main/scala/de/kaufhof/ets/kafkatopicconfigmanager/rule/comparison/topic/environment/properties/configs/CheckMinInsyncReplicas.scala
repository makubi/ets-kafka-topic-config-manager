package de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties.configs

import de.kaufhof.ets.kafkatopicconfigmanager.modelparser.KafkaToModelParser
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{Environment, Topic}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{DifferentConfigurationError, EnvironmentAndTopicAware, _}

object CheckMinInsyncReplicas {
  case class DifferentMinInsyncReplicas(override val xmlValue: Short, override val kafkaValue: Short, override val environment: Environment, override val topic: Topic) extends DifferentConfigurationError[Short
    ] {
    override val key: String = KafkaToModelParser.ConfigKey.`min.insync.replicas`
  }
  case class MinInsyncReplicasMissingForKafka(xmlMinInsyncReplicas: Int, override val environment: Environment, override val topic: Topic) extends ValidationError with EnvironmentAndTopicAware
  case class MinInsyncReplicasMissingForXml(kafkaMinInsyncReplicas: Int, override val environment: Environment, override val topic: Topic) extends ValidationError with EnvironmentAndTopicAware
}

class CheckMinInsyncReplicas(optionalXmlMinInsyncReplicas: Option[Short], optionalKafkaMinInsyncReplicas: Option[Short])(implicit environment: Environment, topic: Topic) extends ConfigurationComparison {
  import CheckMinInsyncReplicas._

  override def validate(): ValidationResult = {
    (optionalXmlMinInsyncReplicas, optionalKafkaMinInsyncReplicas) match {

      case (Some(xmlMinInsyncReplicas), Some(kafkaMinInsyncReplicas)) =>
        compareWithErrorResult(xmlMinInsyncReplicas, kafkaMinInsyncReplicas, DifferentMinInsyncReplicas.apply)

      case (Some(xmlMinInsyncReplicas), None) =>
        MinInsyncReplicasMissingForKafka(xmlMinInsyncReplicas, environment, topic)

      case (None, Some(kafkaMinInsyncReplicas)) =>
        MinInsyncReplicasMissingForXml(kafkaMinInsyncReplicas, environment, topic)

      case (None, None) =>
        ValidationSuccess
    }
  }

}
