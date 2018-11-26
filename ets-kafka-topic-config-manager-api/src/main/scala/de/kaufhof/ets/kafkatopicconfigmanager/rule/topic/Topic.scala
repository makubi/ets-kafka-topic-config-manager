package de.kaufhof.ets.kafkatopicconfigmanager.rule.topic

case class Topic(name: String)

case class XmlTopic(topic: Topic, optionalXmlScheme: Option[XmlScheme], xmlEnvironments: XmlEnvironments)
case class KafkaTopic(topic: Topic, kafkaEnvironments: KafkaEnvironments)
