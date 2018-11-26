package de.kaufhof.ets.kafkatopicconfigmanager.rule.validation

import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.XmlTopic
import de.kaufhof.ets.kafkatopicconfigmanager.rule.validation.CheckTopicNamesAreUnique.TopicExistsMultipleTimesInXml
import de.kaufhof.ets.kafkatopicconfigmanager.rule._

object CheckTopicNamesAreUnique {
  case class TopicExistsMultipleTimesInXml(topicName: String, numberOfOccurrences: Int) extends ValidationError
}
class CheckTopicNamesAreUnique(xmlTopics: Iterable[XmlTopic]) extends XmlConfigurationValidation {
  override def validate(): ValidationResult = {
    xmlTopics.groupBy(_.topic.name).map { case (topicName, topicDefinitions) =>
      val result: ValidationResult = topicDefinitions.size match {
        case 1 => ValidationSuccess
        case other => TopicExistsMultipleTimesInXml(topicName, other)
      }

      result
    }
  }
}
