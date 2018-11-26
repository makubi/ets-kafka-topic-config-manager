package de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties

import de.kaufhof.ets.kafkatopicconfigmanager.configprovider.KafkaConfigurationLoader
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{Environment, Topic}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{ManualApplicableErrorOnKafka, _}

object CheckReplicationFactor {
  case class DifferentReplicationFactor(xmlReplicationFactor: Short, kafkaReplicationFactor: Short, override val environment: Environment, override val topic: Topic) extends ManualApplicableErrorOnKafka {

    def replicas(nodes: Seq[Int], partition: Int, replicationFactor: Short): List[Int] =
      Stream.continually(nodes).flatten.slice(partition, partition + replicationFactor).toList

    override def manualApplicationHelp(kafkaConfigurationLoader: KafkaConfigurationLoader): String = {
      val nodes = kafkaConfigurationLoader.clusterNodesPerEnvironment(environment.value)
      val numberOfPartitions = kafkaConfigurationLoader.forEnvironment(environment.value).configs(topic.name).topicDescription.partitions().size()

      if (xmlReplicationFactor > kafkaReplicationFactor) {
        val partitionsJson = 0 until numberOfPartitions map { partition =>
            s"""{"topic":"${topic.name}","partition":$partition,"replicas":[${replicas(nodes, partition, xmlReplicationFactor).mkString(",")}]}""".stripMargin
        }
        val createReassignFile = s"""
            |echo '{"version":1,"partitions":[
            |${partitionsJson.mkString("," + System.lineSeparator())}
            |]}' > /tmp/increase-replication-factor.json
            |""".stripMargin

        val reassignCommand = s"bin/kafka-reassign-partitions.sh --zookeeper $$ZOOKEEPER --reassignment-json-file /tmp/increase-replication-factor.json --execute"

        createReassignFile + System.lineSeparator() + reassignCommand
      } else {
        "Impossible to decrease replication factor"
      }
    }
  }
}

class CheckReplicationFactor(xmlReplicationFactor: Short, kafkaReplicationFactor: Short)(implicit val environment: Environment, topic: Topic) extends ConfigurationComparison {
  import CheckReplicationFactor._

  override def validate(): ValidationResult = {
    if (xmlReplicationFactor == kafkaReplicationFactor)
      ValidationSuccess
    else
      DifferentReplicationFactor(xmlReplicationFactor, kafkaReplicationFactor, environment, topic)
  }
}
