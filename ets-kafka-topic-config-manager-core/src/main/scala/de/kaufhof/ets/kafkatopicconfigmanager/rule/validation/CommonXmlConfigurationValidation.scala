package de.kaufhof.ets.kafkatopicconfigmanager.rule.validation

import java.net.URL
import java.util

import de.kaufhof.ets.kafkatopicconfigmanager.TopicXmlConfigurationValidationServiceProvider
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{ValidationResult, ValidationSuccess}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{Environment, XmlTopic}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

class CommonXmlConfigurationValidation extends TopicXmlConfigurationValidationServiceProvider {

  private val logger = LoggerFactory.getLogger(getClass)

  override def validate(urlTopicMap: util.Map[URL, XmlTopic]): ValidationResult = {
    val xmlTopicConfigurations = urlTopicMap.values().asScala

    new CheckTopicNamesAreUnique(xmlTopicConfigurations).validate() ++
    xmlTopicConfigurations.flatMap { topic =>
      topic.xmlEnvironments.xmlEnvironmentList.map { environment =>
        val properties = environment.xmlProperties

        properties.xmlConfigs.xmlMinInsyncReplicas.map { minInsyncReplicas =>
          new CheckXmlReplicationFactorGreaterThanMinInsyncReplica(properties.xmlReplicationFactor, minInsyncReplicas)(Environment(environment.name), topic.topic).validate()

          properties.xmlConfigs.xmlUncleanLeaderElection.map { uncleanLeaderElection =>
            new CheckXmlUncleanLeaderElectionAndMinInsyncReplica(uncleanLeaderElection, minInsyncReplicas).validate()
          }.getOrElse(logIgnoredValidation(s"Not validating ${classOf[CheckXmlUncleanLeaderElectionAndMinInsyncReplica].getName}"))
        }.getOrElse(logIgnoredValidation(s"Not validating ${classOf[CheckXmlReplicationFactorGreaterThanMinInsyncReplica].getName} and ${classOf[CheckXmlUncleanLeaderElectionAndMinInsyncReplica].getName}"))

      }
    }
  }

  private def logIgnoredValidation(message: String): ValidationResult = {
    logger.info(message)
    ValidationSuccess
  }
}
