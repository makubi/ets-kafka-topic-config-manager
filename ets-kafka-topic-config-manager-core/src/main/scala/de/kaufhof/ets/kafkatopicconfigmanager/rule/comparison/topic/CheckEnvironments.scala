package de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic

import java.util.Collections

import de.kaufhof.ets.kafkatopicconfigmanager.Implicits._
import de.kaufhof.ets.kafkatopicconfigmanager.modelparser.KafkaToModelParser
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{AutoApplicableErrorOnKafkaWithEnvironment, _}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.CheckEnvironments.{EnvironmentIsMissingInXml, EnvironmentIsMissingOnKafka}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic._
import de.kaufhof.ets.kafkatopicconfigmanager.{Logging, TopicUpdateListenerService}
import org.apache.kafka.clients.admin.{AdminClient, NewTopic}

import scala.concurrent.{ExecutionContext, Future}

object CheckEnvironments extends Logging {

  case class EnvironmentIsMissingInXml(override val topic: Topic, kafkaEnvironment: KafkaEnvironment) extends ValidationError with AutoApplicableErrorOnKafkaWithEnvironment {
    override def autoApply(adminClient: AdminClient, topicUpdateListenerService: TopicUpdateListenerService, executionContext: ExecutionContext): Future[Unit] = {
      logger.info(s"Deleting topic ${topic.name}")

      adminClient.deleteTopics(Collections.singleton(topic.name)).all()
        .map(_ => topicUpdateListenerService.topicDeleted(kafkaEnvironment.name, topic.name))(executionContext)
    }

    override val environment: Environment = Environment(kafkaEnvironment.name)
  }
  case class EnvironmentIsMissingOnKafka(xmlEnvironment: XmlEnvironment, override val topic: Topic) extends ValidationError with AutoApplicableErrorOnKafkaWithEnvironment {
    override def autoApply(adminClient: AdminClient, topicUpdateListenerService: TopicUpdateListenerService, executionContext: ExecutionContext): Future[Unit] = {
      val newTopic =
        new NewTopic(topic.name, xmlEnvironment.xmlProperties.xmlPartitions, xmlEnvironment.xmlProperties.xmlReplicationFactor)
        .configs(xmlEnvironment.xmlProperties.xmlConfigs.toKafkaConfigs)

      logger.info(s"Creating topic ${topic.name}")

      adminClient.createTopics(Collections.singleton(newTopic)).all()
        .map(_ => topicUpdateListenerService.topicCreated(xmlEnvironment.name, topic.name))(executionContext)
    }

    override val environment: Environment = Environment(xmlEnvironment.name)
  }
}

class CheckEnvironments(xmlEnvironments: XmlEnvironments, kafkaEnvironments: KafkaEnvironments)(implicit topic: Topic) extends ConfigurationComparison {

  override def validate(): ValidationResult = {
    KafkaToModelParser.allEnvironments.map { environment =>
      val r: ValidationResult = (xmlEnvironments.xmlEnvironmentList.find(_.name == environment), kafkaEnvironments.kafkaEnvironmentList.find(_.name == environment)) match {
        case (Some(xmlEnvironment), Some(kafkaEnvironment)) =>
          new CheckEnvironment(xmlEnvironment, kafkaEnvironment).validate()
        case (Some(xmlEnvironment), None) =>
          EnvironmentIsMissingOnKafka(xmlEnvironment, topic)
        case (None, Some(kafkaEnvironment)) =>
          EnvironmentIsMissingInXml(topic, kafkaEnvironment)
        case (None, None) =>
          ValidationSuccess
      }

      r
    }
  }
}
