package de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.properties

import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.properties.configs.{KafkaCleanupPolicy, XmlCleanupPolicy}

case class XmlConfigs(xmlCleanupPolicy: XmlCleanupPolicy, xmlMessageFormatVersion: Option[String], xmlMinInsyncReplicas: Option[Short], segmentMs: Option[Long], xmlUncleanLeaderElection: Option[Boolean])
case class KafkaConfigs(kafkaCleanupPolicy: KafkaCleanupPolicy, kafkaMessageFormatVersion: Option[String], kafkaMinInsyncReplicas: Option[Short], segmentMs: Option[Long], kafkaUncleanLeaderElection: Option[Boolean], topicConfigs: Map[String, String])

