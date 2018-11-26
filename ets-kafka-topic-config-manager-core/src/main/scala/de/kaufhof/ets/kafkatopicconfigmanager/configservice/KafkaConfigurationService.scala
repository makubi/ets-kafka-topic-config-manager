package de.kaufhof.ets.kafkatopicconfigmanager.configservice

import de.kaufhof.ets.kafkatopicconfigmanager.configprovider.KafkaConfigurationLoader
import de.kaufhof.ets.kafkatopicconfigmanager.modelparser.{KafkaToModelParser, EnvironmentConfigs}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.KafkaTopic

class KafkaConfigurationService(kafkaConfigurationLoader: KafkaConfigurationLoader, kafkaToModelParser: KafkaToModelParser) {

  def allKafkaConfigs: Iterable[KafkaTopic] = {
    val allKafkaTopics = kafkaConfigurationLoader.topicNamesPerEnvironment.flatMap(_._2).toSet
    val environmentConfigs = kafkaConfigurationLoader.forAllEnvironments

    allKafkaTopics.map { topicName =>
      kafkaConfig(environmentConfigs, topicName)
    }
  }

  private def kafkaConfig(environmentConfigs: EnvironmentConfigs, topic: String): KafkaTopic = {
    kafkaToModelParser.parseTopic(topic, environmentConfigs)
  }
}
