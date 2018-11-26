package de.kaufhof.ets.kafkatopicconfigmanager.rule.topic

import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.{KafkaProperties, XmlProperties}

case class Environment(value: String)

case class XmlEnvironment(name: String, xmlProperties: XmlProperties)
case class KafkaEnvironment(name: String, kafkaProperties: KafkaProperties)