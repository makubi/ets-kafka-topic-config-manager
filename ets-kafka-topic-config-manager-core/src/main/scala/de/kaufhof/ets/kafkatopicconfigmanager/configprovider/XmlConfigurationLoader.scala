package de.kaufhof.ets.kafkatopicconfigmanager.configprovider

import java.io.InputStream
import java.net.URL
import java.nio.file.Path
import java.util.ServiceLoader

import de.kaufhof.ets.kafkatopicconfigmanager.TopicConfigurationServiceProvider

import scala.collection.JavaConverters._

case class ClasspathFile(filePath: Path, inputStream: InputStream)

object XmlResourceFinder {

  def allFilesInTopicFolder: Array[URL] = {
    val serviceLoaderList = ServiceLoader.load(classOf[TopicConfigurationServiceProvider]).asScala.toSeq

    if (serviceLoaderList.size != 1)
      throw new IllegalArgumentException(s"Expected one ${classOf[TopicConfigurationServiceProvider].getName} but found ${serviceLoaderList.size}: ${serviceLoaderList.map(_.getClass.getName).mkString(", ")}")

    val serviceLoader = serviceLoaderList.head
    serviceLoader.getTopicConfigurations
  }

}
