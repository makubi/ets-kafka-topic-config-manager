package de.kaufhof.ets.kafkatopicconfigmanager

import de.kaufhof.ets.kafkatopicconfigmanager.modelparser.KafkaToModelParser
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.properties.XmlConfigs
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.environment.properties.configs.XmlCleanupPolicy
import org.apache.kafka.common.KafkaFuture

import scala.collection.JavaConverters._
import scala.concurrent.{Future, Promise}
import scala.language.implicitConversions

object Implicits {

  implicit class MapOps[K, V](map: scala.collection.mutable.Map[K, V]) {
    def putIfDefined(key: K, optionalValue: Option[V]): Option[V] = optionalValue.flatMap { value =>
      map.put(key, value)
    }
  }

  implicit class XmlConfigsOps(xmlConfigs: XmlConfigs) {
    def toKafkaConfigs: java.util.Map[String, String] = {
      val map = cleanupPolicyConfigs(xmlConfigs.xmlCleanupPolicy)

      map.putIfDefined(KafkaToModelParser.ConfigKey.`message.format.version`, xmlConfigs.xmlMessageFormatVersion)
      map.putIfDefined(KafkaToModelParser.ConfigKey.`min.insync.replicas`, xmlConfigs.xmlMinInsyncReplicas.map(_.toString))
      map.putIfDefined(KafkaToModelParser.ConfigKey.`segment.ms`, xmlConfigs.segmentMs.map(_.toString))
      map.putIfDefined(KafkaToModelParser.ConfigKey.`unclean.leader.election.enable`, xmlConfigs.xmlUncleanLeaderElection.map(_.toString))

      map.asJava
    }

    private def cleanupPolicyConfigs(xmlCleanupPolicy: XmlCleanupPolicy): scala.collection.mutable.Map[String, String] = {
      val configs = scala.collection.mutable.Map.empty[String, String]

      def addCleanupPolicy(cleanupPolicy: String): String =
        Iterable(
          configs.get(KafkaToModelParser.ConfigKey.`cleanup.policy`),
          Some(cleanupPolicy)
        ).flatten.mkString(",")

      xmlCleanupPolicy.compact.map { compact =>
        configs.put(KafkaToModelParser.ConfigKey.`cleanup.policy`, addCleanupPolicy("compact"))

        configs.putIfDefined(KafkaToModelParser.ConfigKey.`delete.retention.ms`, compact.deleteRetentionMs.map(_.toString))
        configs.putIfDefined(KafkaToModelParser.ConfigKey.`min.cleanable.dirty.ratio`, compact.minCleanableDirtyRatio.map(_.toString))
        configs.putIfDefined(KafkaToModelParser.ConfigKey.`min.compaction.lag.ms`, compact.minCompactionLagMs.map(_.toString))
      }

      xmlCleanupPolicy.delete.map { delete =>
        configs.put(KafkaToModelParser.ConfigKey.`cleanup.policy`, addCleanupPolicy("delete"))

        configs.putIfDefined(KafkaToModelParser.ConfigKey.`retention.ms`, delete.retentionMs.map(_.toString))
      }

      configs
    }
  }

  implicit def kafkaFuture2Future[T](kafkaFuture: KafkaFuture[T]): Future[T] = {
    val p = Promise[T]()

    p success kafkaFuture.get()

    p.future
  }

  implicit def voidKafkaFuture2UnitFuture(kafkaFuture: KafkaFuture[Void]): Future[Unit] = {
    val p = Promise[Unit]()

    p success kafkaFuture.get()

    p.future
  }
}
