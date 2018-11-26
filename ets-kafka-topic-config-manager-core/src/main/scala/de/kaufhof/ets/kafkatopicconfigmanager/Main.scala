package de.kaufhof.ets.kafkatopicconfigmanager

import de.kaufhof.ets.kafkatopicconfigmanager.configprovider.{AdminClientProvider, KafkaConfigurationLoader}
import de.kaufhof.ets.kafkatopicconfigmanager.configservice.{KafkaConfigurationService, XmlConfigurationService}
import de.kaufhof.ets.kafkatopicconfigmanager.modelparser.{KafkaToModelParser, XmlToModelParser}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.comparison.topic.CheckTopics
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{AutoApplicableErrorOnKafka, AutoApplicableErrorOnKafkaWithEnvironment, ManualApplicableErrorOnKafka, _}

import scala.collection.JavaConverters._

object Main extends App with Logging {

  val autoApply = sys.props.get("autoApply").contains("true")
  logger.info(s"Auto apply: $autoApply")

  val topicUpdateListenerService = new TopicUpdateListenerService

  val xmlConfigService = new XmlConfigurationService(new XmlToModelParser)

  logger.info("Initializing Kafka config providers")
  val kafkaConfigurationLoader = new KafkaConfigurationLoader
  val kafkaConfigService = new KafkaConfigurationService(kafkaConfigurationLoader, new KafkaToModelParser)

  logger.info("Reading all XML configurations")
  val xmlConfigurations = xmlConfigService.allXmlConfigs

  logger.info("Requesting all Kafka configurations")
  val kafkaConfigurations = kafkaConfigService.allKafkaConfigs

  logger.info("Compare XML and Kafka topic configurations")
  // Compare kafka and xml configurations
  val topicsValidationResult = new CheckTopics(xmlConfigurations.values().asScala, kafkaConfigurations).validate()

  logger.info("Validation XML topic configurations")
  val xmlConfigurationValidationResult = new XmlConfigurationValidationService().validate(xmlConfigurations)

  topicsValidationResult ++ xmlConfigurationValidationResult match {
    case ValidationErrors(errors) =>
      errors.foreach {
        case c: AutoApplicableErrorOnKafkaWithEnvironment =>
          logger.info(s"Auto-applicable difference for ${c.topic.name} found: ${c.shortDescription}")
          logger.debug(c.longDescription)

          if (autoApply) {
            logger.info(s"Running auto application for ${c.topic.name} for environment ${c.environment.value}: ${c.shortDescription}")
            c.autoApply(AdminClientProvider.adminClientForEnvironment(c.environment.value), topicUpdateListenerService, scala.concurrent.ExecutionContext.Implicits.global)
          }

        case c: AutoApplicableErrorOnKafka =>
          logger.info(s"Auto-applicable difference for ${c.topic.name} found: ${c.shortDescription}")
          logger.debug(c.longDescription)

          if (autoApply) {
            logger.info(s"Running auto apply for ${c.topic.name}: ${c.shortDescription}")
            c.autoApply(topicUpdateListenerService, scala.concurrent.ExecutionContext.Implicits.global)
          }

        case c: ManualApplicableErrorOnKafka =>
          logger.info(s"Manual applicable configuration difference for ${c.topic.name} found: ${c.shortDescription}")
          logger.debug(c.manualApplicationHelp(kafkaConfigurationLoader))

        case otherError =>
          logger.warn(s"Other configuration difference: ${otherError.shortDescription}")
          logger.debug(otherError.longDescription)
      }
    case ValidationSuccess =>
      logger.info("All topic configurations are valid")
  }

}
