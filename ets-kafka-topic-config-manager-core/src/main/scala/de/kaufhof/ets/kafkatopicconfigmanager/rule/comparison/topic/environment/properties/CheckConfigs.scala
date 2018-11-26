package de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties

import de.kaufhof.ets.kafkatopicconfigmanager.Implicits._
import de.kaufhof.ets.kafkatopicconfigmanager.modelparser.KafkaToModelParser
import de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties.CheckConfigs.ConfigsDiffer
import de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties.configs._
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.properties.{KafkaConfigs, XmlConfigs}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{Environment, Topic}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{AutoApplicableErrorOnKafkaWithEnvironment, _}
import de.kaufhof.ets.kafkatopicconfigmanager.{Logging, TopicUpdateListenerService}
import org.apache.kafka.clients.admin.{AdminClient, Config, ConfigEntry}
import org.apache.kafka.common.config.ConfigResource

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

object CheckConfigs extends Logging {

  case class ConfigsDiffer(xmlConfigs: XmlConfigs, kafkaConfigs: KafkaConfigs, override val environment: Environment, override val topic: Topic, errors: ValidationErrors) extends ValidationError with AutoApplicableErrorOnKafkaWithEnvironment {
    override def autoApply(adminClient: AdminClient, topicUpdateListenerService: TopicUpdateListenerService, executionContext: ExecutionContext): Future[Unit] = {
      val configResource = new ConfigResource(ConfigResource.Type.TOPIC, topic.name)
      val configEntries = xmlConfigs.toKafkaConfigs.asScala.map { case (key, value) =>
        new ConfigEntry(key, value)
      }

      logger.info(s"Updating configs for topic ${topic.name} in environment $environment")

      adminClient.alterConfigs(Map(configResource -> new Config(configEntries.asJavaCollection)).asJava).all()
        .map(_ => topicUpdateListenerService.topicConfigurationUpdated(environment.value, topic.name))(executionContext)
    }
  }

}

class CheckConfigs(xmlConfigs: XmlConfigs, kafkaConfigs: KafkaConfigs)(implicit environment: Environment, topic: Topic) extends ConfigurationComparison {

  override def validate(): ValidationResult = {
    val results = new CheckCleanupPolicy(xmlConfigs.xmlCleanupPolicy, kafkaConfigs.kafkaCleanupPolicy).validate() +
    new CheckMessageFormatVersion(xmlConfigs.xmlMessageFormatVersion, kafkaConfigs.kafkaMessageFormatVersion).validate() +
    new CheckMinInsyncReplicas(xmlConfigs.xmlMinInsyncReplicas, kafkaConfigs.kafkaMinInsyncReplicas).validate() +
    new CheckSegmentMs(xmlConfigs.segmentMs, kafkaConfigs.segmentMs).validate() +
    new CheckUncleanLeaderElectionEnable(xmlConfigs.xmlUncleanLeaderElection, kafkaConfigs.kafkaUncleanLeaderElection).validate() +
    new CheckUnknownConfigsOnKafkaForTopic(KafkaToModelParser.ConfigKey.all, kafkaConfigs.topicConfigs).validate()

    results match {
      case ValidationSuccess => ValidationSuccess
      case errors: ValidationErrors => ConfigsDiffer(xmlConfigs, kafkaConfigs, environment, topic, errors)
    }
  }
}

