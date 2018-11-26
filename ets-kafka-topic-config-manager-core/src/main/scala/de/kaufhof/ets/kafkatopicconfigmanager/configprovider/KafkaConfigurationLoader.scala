package de.kaufhof.ets.kafkatopicconfigmanager.configprovider

import de.kaufhof.ets.kafkatopicconfigmanager.modelparser.{KafkaToModelParser, EnvironmentConfig, EnvironmentConfigs, TopicConfig}
import org.apache.kafka.common.config.ConfigResource

import scala.collection.JavaConverters._
import scala.collection.mutable

class KafkaConfigurationLoader {

  private val adminClients = KafkaToModelParser.allEnvironments.map { environment =>
    environment -> AdminClientProvider.adminClientForEnvironment(environment)
  }.toMap

  val clusterNodesPerEnvironment: Map[String, Seq[Int]] = KafkaToModelParser.allEnvironments.map { environment =>
    environment -> adminClients(environment).describeCluster().nodes().get().asScala.map(_.id()).toSeq
  }.toMap

  val topicNamesPerEnvironment: Map[String, mutable.Set[String]] = KafkaToModelParser.allEnvironments.map { environment =>
    environment -> adminClients(environment).listTopics().names().get().asScala
  }.toMap

  private val topicConfigsPerEnvironment = KafkaToModelParser.allEnvironments.map { environment =>
    environment -> adminClients(environment).describeConfigs(topicNamesPerEnvironment(environment).map(name => new ConfigResource(ConfigResource.Type.TOPIC, name)).asJavaCollection).all().get().asScala
  }.toMap

  private val topicDescriptionsPerEnvironment = KafkaToModelParser.allEnvironments.map { environment =>
    environment -> adminClients(environment).describeTopics(topicNamesPerEnvironment(environment).asJavaCollection).all().get().asScala
  }.toMap

  def forAllEnvironments: EnvironmentConfigs = {
    EnvironmentConfigs(
      KafkaToModelParser.allEnvironments.map(environment => environment -> forEnvironment(environment)).toMap
    )
  }

  def forEnvironment(environment: String): EnvironmentConfig = {
    val topicNames = topicNamesPerEnvironment(environment)
    val configs = topicConfigsPerEnvironment(environment)
    val topicDescriptions = topicDescriptionsPerEnvironment(environment)

    val topicConfigs = topicNames.map { topicName =>
      val config = configs.find(_._1.name() == topicName).getOrElse(throw new RuntimeException(s"Unable to find configs for topic $topicName in environment $environment"))._2
      val topicDescription = topicDescriptions.find(_._1 == topicName).getOrElse(throw new RuntimeException(s"Unable to find the topic description for topic $topicName in environment $environment"))._2

      topicName -> TopicConfig(topicDescription, config)
    }.toMap

    EnvironmentConfig(topicConfigs)
  }
}
