package de.kaufhof.ets.kafkatopicconfigmanager.modelparser

import de.kaufhof.ets.kafkatopicconfigmanager.generated.EnvironmentType.Properties
import de.kaufhof.ets.kafkatopicconfigmanager.generated.EnvironmentType.Properties.Configs
import de.kaufhof.ets.kafkatopicconfigmanager.generated.EnvironmentType.Properties.Configs.CleanupPolicy
import de.kaufhof.ets.kafkatopicconfigmanager.generated.EnvironmentType.Properties.Configs.CleanupPolicy.{Compact, Delete}
import de.kaufhof.ets.kafkatopicconfigmanager.generated.Topic
import de.kaufhof.ets.kafkatopicconfigmanager.generated.Topic.Schema
import de.kaufhof.ets.kafkatopicconfigmanager.generated.{Topic => _, _}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic._
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.scheme.{XmlGitSource, XmlSource, XmlUrlSource}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.XmlProperties
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.properties.XmlConfigs
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.properties.configs.XmlCleanupPolicy
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.properties.configs.cleanuppolicy.{XmlCompact, XmlDelete}

import scala.collection.JavaConverters._
import scala.language.implicitConversions

class XmlToModelParser {

  def parseTopic(topic: Topic): XmlTopic = {
    XmlTopic(de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.Topic(topic.getName), Option(topic.getSchema).map(parseSchema), parseEnvironments(topic.getEnvironments.getEnvironment.asScala))
  }

  private def parseSchema(schema: Schema): XmlScheme = {
    XmlScheme(schema.getType, Option(schema.getKey).map(parseSchemaTypeDescriptor), parseSchemaTypeDescriptor(schema.getValue))
  }

  private def parseSchemaTypeDescriptor(schemaTypeDescriptor: SchemaTypeDescriptorType): XmlSchemaTypeDescriptor = {
    XmlSchemaTypeDescriptor(schemaTypeDescriptor.getName, parseSource(schemaTypeDescriptor.getSource))
  }

  private def parseSource(source: SourceType): XmlSource = {
    val sources = Seq(
      Option(source.getGit).map(parseGitSource),
      Option(source.getUrl).map(parseUrlSource)
    ).flatten

    if (sources.size != 1)
      throw new IllegalArgumentException(s"One scheme source must exist: ${sources.mkString(", ")}")

    sources.head
  }

  private def parseGitSource(gitSource: GitSourceType): XmlGitSource = {
    XmlGitSource(gitSource.getRepository, gitSource.getPath)
  }

  private def parseUrlSource(urlSource: UrlSourceType): XmlUrlSource = {
    XmlUrlSource(urlSource.getUrl)
  }

  private def parseEnvironments(environments: Iterable[EnvironmentType]): XmlEnvironments = {
    XmlEnvironments(environments.map(parseXmlEnvironment))
  }

  private def parseXmlEnvironment(environment: EnvironmentType): XmlEnvironment = {
    XmlEnvironment(environment.getName, parseProperties(environment.getProperties))
  }

  private def parseProperties(properties: Properties): XmlProperties = {
    XmlProperties(parseConfigs(properties.getConfigs), properties.getReplicationFactor, properties.getPartitions)
  }

  private def parseConfigs(configs: Configs): XmlConfigs = {
    val xmlCleanupPolicy = parseCleanupPolicy(configs.getCleanupPolicy)

    XmlConfigs(xmlCleanupPolicy, Option(configs.getMessageFormatVersion), Option(configs.getMinInsyncReplicas), Option(configs.getSegmentMs).map(_.getValue), Option(configs.getUncleanLeaderElectionEnable).map(_.getValue))
  }

  private def parseCleanupPolicy(cleanupPolicy: CleanupPolicy): XmlCleanupPolicy = {
    var optionalCompact: Option[XmlCompact] = None
    var optionalDelete: Option[XmlDelete] = None

    cleanupPolicy.getCompactOrDelete.asScala.foreach {
      case compact: CleanupPolicy.Compact => optionalCompact = Some(parseCompact(compact))
      case delete: CleanupPolicy.Delete => optionalDelete = Some(parseDelete(delete))
    }

    XmlCleanupPolicy(optionalCompact, optionalDelete)
  }

  private def parseCompact(compact: Compact): XmlCompact = {
    XmlCompact(compact.getDeleteRetentionMs.optional, compact.getMinCompactionLagMs.optional, compact.getMinCleanableDirtyRatio.optional)
  }

  private def parseDelete(delete: Delete): XmlDelete = {
    XmlDelete(Option(delete.getRetentionMs))
  }

  private implicit class AnyRefOptionalOps[T <: AnyRef](t: T) {
    def optional: Option[T] = {
      Option(t)
    }
  }

  private implicit def javaLong2Long(value: Option[java.lang.Long]): Option[Long] =
    value.map(_.longValue())

  private implicit def javaFloat2Float(value: Option[java.lang.Float]): Option[Float] =
    value.map(_.floatValue())
}