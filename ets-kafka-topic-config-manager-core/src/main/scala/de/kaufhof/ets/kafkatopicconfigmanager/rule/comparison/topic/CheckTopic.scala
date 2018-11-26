package de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic

import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{KafkaTopic, XmlTopic}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{ConfigurationComparison, ValidationResult}

class CheckTopic(xmlTopic: XmlTopic, kafkaTopic: KafkaTopic) extends ConfigurationComparison {

  override def validate(): ValidationResult = {
    if (xmlTopic.topic.name != kafkaTopic.topic.name) {
      throw new IllegalArgumentException(s"XML and Kafka topic name differ: ${xmlTopic.topic.name}, ${kafkaTopic.topic.name}")
    } else {
      new CheckEnvironments(xmlTopic.xmlEnvironments, kafkaTopic.kafkaEnvironments)(xmlTopic.topic).validate()
    }
  }
}