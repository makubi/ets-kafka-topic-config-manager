package de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment

import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.properties.{KafkaConfigs, XmlConfigs}

case class XmlProperties(xmlConfigs: XmlConfigs, xmlReplicationFactor: Short, xmlPartitions: Int)
case class KafkaProperties(kafkaConfigs: KafkaConfigs, kafkaReplicationFactor: Short, kafkaPartitions: Int)
