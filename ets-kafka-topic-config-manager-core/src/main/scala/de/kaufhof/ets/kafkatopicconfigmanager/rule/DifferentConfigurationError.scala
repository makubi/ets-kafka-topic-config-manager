package de.kaufhof.ets.kafkatopicconfigmanager.rule

import de.kaufhof.ets.kafkatopicconfigmanager.TopicUpdateListenerService
import de.kaufhof.ets.kafkatopicconfigmanager.configprovider.KafkaConfigurationLoader
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{Environment, Topic}
import org.apache.kafka.clients.admin.AdminClient

import scala.concurrent.{ExecutionContext, Future}

trait EnvironmentAware {
  val environment: Environment
}

trait TopicAware {
  val topic: Topic
}

trait ConfigurationError extends ValidationError with EnvironmentAndTopicAware

trait ConfigurationKey {
  val key: String
}

trait ConfigurationWithKeyError extends ConfigurationError with ConfigurationKey

trait ErrorOnKafka extends ValidationError

trait ManualApplicableErrorOnKafka extends ErrorOnKafka with EnvironmentAndTopicAware {
  def manualApplicationHelp(kafkaConfigurationLoader: KafkaConfigurationLoader): String
}

trait AutoApplicableErrorOnKafka extends ErrorOnKafka with TopicAware {
  def autoApply(topicUpdateListenerService: TopicUpdateListenerService, executionContext: ExecutionContext): Future[Unit]
}

trait AutoApplicableErrorOnKafkaWithEnvironment extends ErrorOnKafka with EnvironmentAndTopicAware {
  def autoApply(adminClient: AdminClient, topicUpdateListenerService: TopicUpdateListenerService, executionContext: ExecutionContext): Future[Unit]
}

/** Describes a configuration that is exists in the XML */
trait XmlValue[T] extends ConfigurationError {
  val xmlValue: T
}

/** Describes a configuration that is exists on Kafka */
trait KafkaValue[T] extends ConfigurationError {
  val kafkaValue: T
}

trait EnvironmentAndTopicAware extends EnvironmentAware with TopicAware

trait DifferentValues[T] extends XmlValue[T] with KafkaValue[T]

/** Describes a configuration that exists in the XML and on Kafka but differ */
trait DifferentConfigurationError[T] extends ConfigurationError with ConfigurationKey with DifferentValues[T]

/** Describes an optional configuration that exists in the XML and on Kafka but differ */
//trait DifferentOptionalConfigurationError[T] extends DifferentConfigurationError[Option[T]]

/** Describes a configuration that is missing in the XML but exists on Kafka */
trait ConfigurationMissingInXml[T] extends ConfigurationWithKeyError with KafkaValue[T]

/** Describes a configuration that is missing on Kafka but exists in the XML */
trait ConfigurationMissingOnKafka[T] extends ConfigurationWithKeyError with XmlValue[T]