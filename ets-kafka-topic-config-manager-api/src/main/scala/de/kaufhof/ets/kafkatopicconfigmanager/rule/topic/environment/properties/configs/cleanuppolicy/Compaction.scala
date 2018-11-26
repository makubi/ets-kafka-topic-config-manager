package de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.properties.configs.cleanuppolicy

case class XmlCompact(deleteRetentionMs: Option[Long], minCompactionLagMs: Option[Long], minCleanableDirtyRatio: Option[Float])
case class KafkaCompact(deleteRetentionMs: Option[Long], minCompactionLagMs: Option[Long], minCleanableDirtyRatio: Option[Float])
