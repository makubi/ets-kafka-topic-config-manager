package de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties.configs

import de.kaufhof.ets.kafkatopicconfigmanager.modelparser.KafkaToModelParser
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{Environment, Topic}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{DifferentConfigurationError, _}

object CheckUncleanLeaderElectionEnable {
  case class DifferentUncleanLeaderElectionEnable(override val xmlValue: Boolean, override val kafkaValue: Boolean, override val environment: Environment, override val topic: Topic) extends DifferentConfigurationError[Boolean] {
    override val key: String = KafkaToModelParser.ConfigKey.`unclean.leader.election.enable`
  }
  case class UncleanLeaderElectionEnableMissingForKafka(xmlUncleanLeaderElectionEnable: Boolean) extends ValidationError
  case class UncleanLeaderElectionEnableMissingForXml(kafkaUncleanLeaderElectionEnable: Boolean) extends ValidationError
}

class CheckUncleanLeaderElectionEnable(optionalXmlUncleanLeaderElectionEnable: Option[Boolean], optionalKafkaUncleanLeaderElectionEnable: Option[Boolean])(implicit environment: Environment, topic: Topic) extends ConfigurationComparison {
  import CheckUncleanLeaderElectionEnable._

  override def validate(): ValidationResult = {
    (optionalXmlUncleanLeaderElectionEnable, optionalKafkaUncleanLeaderElectionEnable) match {

      case (Some(xmlUncleanLeaderElectionEnable), Some(kafkaUncleanLeaderElectionEnable)) =>
        compareWithErrorResult(xmlUncleanLeaderElectionEnable, kafkaUncleanLeaderElectionEnable, DifferentUncleanLeaderElectionEnable)

      case (Some(xmlUncleanLeaderElectionEnable), None) =>
        UncleanLeaderElectionEnableMissingForKafka(xmlUncleanLeaderElectionEnable)

      case (None, Some(kafkaUncleanLeaderElectionEnable)) =>
        UncleanLeaderElectionEnableMissingForXml(kafkaUncleanLeaderElectionEnable)

      case (None, None) =>
        ValidationSuccess
    }
  }

}
