package de.kaufhof.ets.kafkatopicconfigmanager

import org.slf4j.{Logger, LoggerFactory}

trait Logging {

  val logger: Logger = LoggerFactory.getLogger(getClass)
}
