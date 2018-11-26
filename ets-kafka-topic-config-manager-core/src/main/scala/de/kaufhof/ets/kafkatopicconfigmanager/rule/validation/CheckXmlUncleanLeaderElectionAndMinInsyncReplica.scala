package de.kaufhof.ets.kafkatopicconfigmanager.rule.validation

import de.kaufhof.ets.kafkatopicconfigmanager.rule.validation.CheckXmlUncleanLeaderElectionAndMinInsyncReplica.XmlMinInsyncReplicaAreLessThanTwoButUncleanLeaderElectionIsNotSet
import de.kaufhof.ets.kafkatopicconfigmanager.rule._

object CheckXmlUncleanLeaderElectionAndMinInsyncReplica {
  case class XmlMinInsyncReplicaAreLessThanTwoButUncleanLeaderElectionIsNotSet(minInsyncReplica: Short) extends ValidationError
}
class CheckXmlUncleanLeaderElectionAndMinInsyncReplica(uncleanLeaderElection: Boolean, minInsyncReplica: Short) extends XmlConfigurationValidation {
  override def validate(): ValidationResult = {
    if (!uncleanLeaderElection && minInsyncReplica < 2) XmlMinInsyncReplicaAreLessThanTwoButUncleanLeaderElectionIsNotSet(minInsyncReplica)
    else ValidationSuccess
  }
}
