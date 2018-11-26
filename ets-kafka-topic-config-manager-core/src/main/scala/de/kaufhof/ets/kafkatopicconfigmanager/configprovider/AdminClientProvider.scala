package de.kaufhof.ets.kafkatopicconfigmanager.configprovider

import com.typesafe.config.ConfigFactory
import de.kaufhof.ets.kafkatopicconfigmanager.Logging
import org.apache.kafka.clients.admin.AdminClient

import scala.collection.JavaConverters._

object AdminClientProvider extends Logging {

  private val configuration = ConfigFactory.load()

  private val environmentsConfigs = configuration.getConfigList("environments").asScala

  private val environmentServerMap = environmentsConfigs.map { bootstrapServerConfig =>
    bootstrapServerConfig.getString("name") -> bootstrapServerConfig.getString("bootstrapServers")
  }.toMap
  val allEnvironments: Iterable[String] = environmentServerMap.keys

  logger.info(s"Load environments: $allEnvironments")

  private val adminClients = allEnvironments.map { environment =>
    environment -> newAdminClientForEnvironment(environment)
  }.toMap

  sys.addShutdownHook {
    adminClients.foreach(_._2.close())
  }

  def adminClientForEnvironment(environment: String): AdminClient =
    adminClients(environment)

  private def newAdminClientForEnvironment(environment: String): AdminClient = {
    val properties = Map[String, AnyRef](
      "bootstrap.servers" -> environmentServerMap.getOrElse(environment, throw new IllegalArgumentException(s"Server config for environment $environment not found"))
    )

    AdminClient.create(properties.asJava)
  }
}
