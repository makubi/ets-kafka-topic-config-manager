package de.kaufhof.ets.kafkatopicconfigmanager.rule.validation

import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{Environment, Topic}
import de.kaufhof.ets.kafkatopicconfigmanager.rule.validation.CheckXmlReplicationFactorGreaterThanMinInsyncReplica.ReplicationFactorNotGreaterThanMinInSyncReplica
import de.kaufhof.ets.kafkatopicconfigmanager.rule.{EnvironmentAndTopicAware, _}

object CheckXmlReplicationFactorGreaterThanMinInsyncReplica {
  case class ReplicationFactorNotGreaterThanMinInSyncReplica(replicationFactor: Short, minInsyncReplica: Short, override val environment: Environment, override val topic: Topic) extends ValidationError with EnvironmentAndTopicAware
}
class CheckXmlReplicationFactorGreaterThanMinInsyncReplica(replicationFactor: Short, minInsyncReplica: Short)(implicit environment: Environment, topic: Topic) extends XmlConfigurationValidation {
  override def validate(): ValidationResult = {
    if (replicationFactor > minInsyncReplica) ValidationSuccess
    else ReplicationFactorNotGreaterThanMinInSyncReplica(replicationFactor, minInsyncReplica, environment, topic)
  }
}
