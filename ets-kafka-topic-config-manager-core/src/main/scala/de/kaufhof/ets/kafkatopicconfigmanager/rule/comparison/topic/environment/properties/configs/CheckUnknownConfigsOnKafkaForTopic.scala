package de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties.configs

import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{Environment, Topic}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{DifferentConfigurationError, _}

object CheckUnknownConfigsOnKafkaForTopic {
  case class UnknownConfigOnKafka(override val key: String, value: String, override val environment: Environment, override val topic: Topic) extends DifferentConfigurationError[Option[String]] {
    override val xmlValue: Option[String] = None
    override val kafkaValue: Option[String] = Some(value)
  }
}

class CheckUnknownConfigsOnKafkaForTopic(knownConfigKeys: Iterable[String], kafkaTopicConfigs: Map[String, String])(implicit environment: Environment, topic: Topic) extends ConfigurationComparison {
  import CheckUnknownConfigsOnKafkaForTopic._

  override def validate(): ValidationResult = {
    val configsOnKafkaOnly = kafkaTopicConfigs.filterNot { case (key, _) =>
      knownConfigKeys.toSeq.contains(key)
    }

    if (configsOnKafkaOnly.nonEmpty)
      ValidationErrors(configsOnKafkaOnly.map(c => UnknownConfigOnKafka(c._1, c._2, environment, topic)))
    else
      ValidationSuccess
  }
}
