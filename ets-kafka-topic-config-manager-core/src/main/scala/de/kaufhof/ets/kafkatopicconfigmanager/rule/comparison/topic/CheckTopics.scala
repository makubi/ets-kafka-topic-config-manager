package de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic

import de.kaufhof.ets.kafkatopicconfigmanager.TopicUpdateListenerService
import de.kaufhof.ets.kafkatopicconfigmanager.configprovider.AdminClientProvider
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{AutoApplicableErrorOnKafka, _}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.CheckEnvironments.{EnvironmentIsMissingInXml, EnvironmentIsMissingOnKafka}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{KafkaTopic, Topic, XmlTopic}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

object CheckTopics {
  case class TopicIsMissingForAllEnvironmentsOnKafka(xmlTopic: XmlTopic) extends AutoApplicableErrorOnKafka {

    override def autoApply(topicUpdateListenerService: TopicUpdateListenerService, executionContext: ExecutionContext): Future[Unit] = {
      Future.sequence(
        xmlTopic.xmlEnvironments.xmlEnvironmentList.map { xmlEnvironment =>
          EnvironmentIsMissingOnKafka(xmlEnvironment, xmlTopic.topic).autoApply(AdminClientProvider.adminClientForEnvironment(xmlEnvironment.name), topicUpdateListenerService, executionContext)
        }
      ).map(_ => ())
    }

    override val topic: Topic = xmlTopic.topic
  }
  case class TopicIsMissingForAllEnvironmentsInXml(kafkaTopic: KafkaTopic) extends AutoApplicableErrorOnKafka {
    override def autoApply(topicUpdateListenerService: TopicUpdateListenerService, executionContext: ExecutionContext): Future[Unit] = {
      Future.sequence(
        kafkaTopic.kafkaEnvironments.kafkaEnvironmentList.map { kafkaEnvironment =>
          EnvironmentIsMissingInXml(kafkaTopic.topic, kafkaEnvironment).autoApply(AdminClientProvider.adminClientForEnvironment(kafkaEnvironment.name), topicUpdateListenerService, executionContext)
        }
      ).map(_ => ())
    }

    override val topic: Topic = kafkaTopic.topic
  }
}

class CheckTopics(xmlTopics: Iterable[XmlTopic], kafkaTopics: Iterable[KafkaTopic]) extends ConfigurationComparison {
  import CheckTopics._

  override def validate(): ValidationResult = {
      val allTopics = (xmlTopics.map(_.topic.name) ++ kafkaTopics.map(_.topic.name)).toSet

      allTopics.map { topicName =>
        val xmlEnvironmentTopic = xmlTopics.find(_.topic.name == topicName)
        val kafkaEnvironmentTopic = kafkaTopics.find(_.topic.name == topicName)

        val r: ValidationResult = (xmlEnvironmentTopic, kafkaEnvironmentTopic) match {
          case (Some(xmlTopic), Some(kafkaTopic)) =>
            new CheckTopic(xmlTopic, kafkaTopic).validate()
          case (Some(xmlTopic), None) =>
            TopicIsMissingForAllEnvironmentsOnKafka(xmlTopic)
          case (None, Some(kafkaTopic)) =>
            TopicIsMissingForAllEnvironmentsInXml(kafkaTopic)
          case (None, None) =>
            ValidationSuccess
        }

      r
    }
  }
}
