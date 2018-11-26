package de.kaufhof.ets.kafkatopicconfigmanager

import java.util.ServiceLoader

import scala.collection.JavaConverters._

class TopicUpdateListenerService extends TopicUpdateListenerServiceProvider {

  private val topicUpdateListenerServiceProviderList = ServiceLoader.load(classOf[TopicUpdateListenerServiceProvider]).asScala.filterNot(_.getClass == getClass)

  override def topicCreated(environment: String, topic: String): Unit =
    topicUpdateListenerServiceProviderList.foreach(_.topicCreated(environment, topic))

  override def topicDeleted(environment: String, topic: String): Unit =
    topicUpdateListenerServiceProviderList.foreach(_.topicDeleted(environment, topic))

  override def topicConfigurationUpdated(environment: String, topic: String): Unit =
    topicUpdateListenerServiceProviderList.foreach(_.topicConfigurationUpdated(environment, topic))

  override def topicPartitionCreated(environment: String, topic: String): Unit =
    topicUpdateListenerServiceProviderList.foreach(_.topicPartitionCreated(environment, topic))
}
