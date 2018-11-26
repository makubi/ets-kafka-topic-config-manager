package de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.environment.properties

import de.kaufhof.ets.kafkatopicconfigmanager.Implicits._
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{Environment, Topic}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{AutoApplicableErrorOnKafkaWithEnvironment, DifferentValues, _}
import de.kaufhof.ets.kafkatopicconfigmanager.{Logging, TopicUpdateListenerService}
import org.apache.kafka.clients.admin.{AdminClient, NewPartitions}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

object CheckNumberOfPartitions extends Logging {
  case class DifferentNumberOfPartitions(override val xmlValue: Int, override val kafkaValue: Int, override val environment: Environment, override val topic: Topic) extends DifferentValues[Int] with AutoApplicableErrorOnKafkaWithEnvironment {
    override def autoApply(adminClient: AdminClient, topicUpdateListenerService: TopicUpdateListenerService, executionContext: ExecutionContext): Future[Unit] = {
      if (xmlValue > kafkaValue) {
        val newPartitions = Map (
          topic.name -> NewPartitions.increaseTo(xmlValue)
        )

        logger.info(s"Creating partitions for topic ${topic.name} in environment $environment")

        adminClient.createPartitions(newPartitions.asJava).all()
          .map(_ => topicUpdateListenerService.topicPartitionCreated(environment.value, topic.name))(executionContext)
      } else {
        throw new RuntimeException("Decreasing partition count is not implemented")
      }
    }
  }
}

class CheckNumberOfPartitions(xmlNumberOfPartitions: Int, kafkaNumberOfPartitions: Int)(implicit environment: Environment, topic: Topic) extends ConfigurationComparison {
  import CheckNumberOfPartitions._

  override def validate(): ValidationResult = {
    if (xmlNumberOfPartitions == kafkaNumberOfPartitions)
      ValidationSuccess
    else
      DifferentNumberOfPartitions(xmlNumberOfPartitions, kafkaNumberOfPartitions, environment, topic)
  }
}