package de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.properties.configs.cleanuppolicy

case class XmlDelete(retentionMs: Option[Long])
case class KafkaDelete(retentionMs: Option[Long])
