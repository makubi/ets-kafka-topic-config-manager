package de.kaufhof.ets.kafkatopicconfigmanager.modelparser

import de.kaufhof.ets.kafkatopicconfigmanager.configprovider.AdminClientProvider
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.KafkaProperties
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.properties.KafkaConfigs
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.properties.configs.KafkaCleanupPolicy
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.properties.configs.cleanuppolicy.{KafkaCompact, KafkaDelete}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{KafkaEnvironment, KafkaEnvironments, KafkaTopic, Topic}
import org.apache.kafka.clients.admin.ConfigEntry.ConfigSource
import org.apache.kafka.clients.admin.{Config, ConfigEntry, TopicDescription}
import org.apache.kafka.common.TopicPartitionInfo

import scala.collection.JavaConverters._

object KafkaToModelParser {
  object ConfigKey {
    val `message.format.version` = "message.format.version"
    val `min.insync.replicas` = "min.insync.replicas"
    val `unclean.leader.election.enable` = "unclean.leader.election.enable"
    val `cleanup.policy` = "cleanup.policy"
    val `delete.retention.ms` = "delete.retention.ms"
    val `min.compaction.lag.ms` = "min.compaction.lag.ms"
    val `retention.ms` = "retention.ms"
    val `min.cleanable.dirty.ratio` = "min.cleanable.dirty.ratio"
    val `segment.ms` = "segment.ms"

    val all = Iterable(
      `cleanup.policy`,
      `delete.retention.ms`,
      `message.format.version`,
      `min.cleanable.dirty.ratio`,
      `min.compaction.lag.ms`,
      `min.insync.replicas`,
      `retention.ms`,
      `segment.ms`,
      `unclean.leader.election.enable`
    )
  }

  val allEnvironments = AdminClientProvider.allEnvironments
}

class KafkaToModelParser {
  import KafkaToModelParser._

  def parseTopic(topicName: String, environmentConfigs: EnvironmentConfigs): KafkaTopic = {
    KafkaTopic(Topic(topicName), parseEnvironments(topicName, environmentConfigs))
  }

  def parseEnvironments(topicName: String, environmentConfigs: EnvironmentConfigs): KafkaEnvironments = {
    val optionalEnvironments = allEnvironments.map { environment =>
      val environmentConfig = environmentConfigs.configs.get(environment)

      environmentConfig.flatMap { environmentConfig =>
        environmentConfig.configs.get(topicName).map { topicConfig =>
          KafkaEnvironment(
            environment,
            parseProperties(
              topicConfig.topicDescription.partitions().asScala,
              topicConfig.config.entries().asScala
                .filter(_.source() == ConfigSource.DYNAMIC_TOPIC_CONFIG) // Remove non topic configs
            )
          )
        }
      }
    }

    KafkaEnvironments(optionalEnvironments.flatten)
  }

  def parseProperties(partitions: Iterable[TopicPartitionInfo], configEntries: Iterable[ConfigEntry]): KafkaProperties = {
    KafkaProperties(parseConfigs(configEntries), parseReplicationFactor(partitions), parsePartitions(partitions))
  }

  def parseConfigs(kafkaTopicDescription: Iterable[ConfigEntry]): KafkaConfigs = {
    val configMap = kafkaTopicDescription.map { configEntry =>

      configEntry.name() -> configEntry.value()
    }.toMap

    val cleanupPolicy = parseCleanupPolicy(configMap)

    KafkaConfigs(
      cleanupPolicy,
      configMap.get(ConfigKey.`message.format.version`),
      configMap.get(ConfigKey.`min.insync.replicas`).map(_.toShort),
      configMap.get(ConfigKey.`segment.ms`).map(_.toLong),
      configMap.get(ConfigKey.`unclean.leader.election.enable`).map(_.toBoolean),
      configMap
    )
  }

  def parseReplicationFactor(partitions: Iterable[TopicPartitionInfo]): Short = {
    val firstPartition = partitions.headOption.getOrElse(throw new IllegalArgumentException("Topic does not have any partition"))
    val firstPartitionReplicationCount = firstPartition.replicas().size()

    val partitionsWithDifferentReplicationCount = partitions.filterNot(_.replicas().size() == firstPartitionReplicationCount)

    if (partitionsWithDifferentReplicationCount.nonEmpty) {
      throw new IllegalArgumentException(s"Partitions ${partitionsWithDifferentReplicationCount.map(_.partition()).mkString(",")} have a different replication count than partition ${firstPartition.partition()}")
    } else {
      if (firstPartitionReplicationCount > Short.MaxValue)
        throw new IllegalArgumentException(s"Replication count is bigger than ${Short.MaxValue}")
      else
        firstPartitionReplicationCount.toShort
    }
  }

  def parsePartitions(partitions: Iterable[TopicPartitionInfo]): Int = {
    partitions.size
  }

  def parseCleanupPolicy(configMap: Map[String, String]): KafkaCleanupPolicy = {
    KafkaCleanupPolicy(parseCompact(configMap), parseDelete(configMap))
  }

  def parseCompact(configMap: Map[String, String]): Option[KafkaCompact] = {
    if (configMap.get(ConfigKey.`cleanup.policy`).exists(_.contains("compact"))) {
      val deleteRetentionMs = configMap.get(ConfigKey.`delete.retention.ms`).map(_.toLong)
      val minCleanableDirtyRatio = configMap.get(ConfigKey.`min.cleanable.dirty.ratio`).map(_.toFloat)
      val minCompactionLagMs = configMap.get(ConfigKey.`min.compaction.lag.ms`).map(_.toLong)

      Some(KafkaCompact(deleteRetentionMs, minCompactionLagMs, minCleanableDirtyRatio))
    } else {
      None
    }
  }

  def parseDelete(configMap: Map[String, String]): Option[KafkaDelete] = {
    if (configMap.get(ConfigKey.`cleanup.policy`).exists(_.contains("delete"))) {
      val retentionMs = configMap.get(ConfigKey.`retention.ms`).map(_.toLong)

      Some(KafkaDelete(retentionMs))
    } else {
      None
    }
  }

}

case class TopicConfig(topicDescription: TopicDescription, config: Config)
case class EnvironmentConfig(configs: Map[String, TopicConfig])
case class EnvironmentConfigs(configs: Map[String, EnvironmentConfig])
