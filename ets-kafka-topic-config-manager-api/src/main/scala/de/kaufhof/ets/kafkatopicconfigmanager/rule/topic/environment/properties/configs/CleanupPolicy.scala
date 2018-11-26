package de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.properties.configs

import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.properties.configs.cleanuppolicy.{KafkaCompact, KafkaDelete, XmlCompact, XmlDelete}

case class XmlCleanupPolicy(compact: Option[XmlCompact], delete: Option[XmlDelete])
case class KafkaCleanupPolicy(compact: Option[KafkaCompact], delete: Option[KafkaDelete])
