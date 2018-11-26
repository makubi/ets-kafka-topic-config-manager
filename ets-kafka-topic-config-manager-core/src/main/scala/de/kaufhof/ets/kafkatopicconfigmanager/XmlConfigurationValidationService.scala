package de.kaufhof.ets.kafkatopicconfigmanager

import java.net.URL
import java.util.ServiceLoader

import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.XmlTopic
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{ValidationResult, ValidationSuccess}

import scala.collection.JavaConverters._

class XmlConfigurationValidationService extends TopicXmlConfigurationValidationServiceProvider {

  private val xmlConfigurationValidationServiceProviderList = ServiceLoader.load(classOf[TopicXmlConfigurationValidationServiceProvider]).asScala.filterNot(_.getClass == getClass)

  override def validate(namedXmlTopics: java.util.Map[URL, XmlTopic]): ValidationResult =
    ValidationSuccess ++ xmlConfigurationValidationServiceProviderList.map(_.validate(namedXmlTopics)).toArray
}
