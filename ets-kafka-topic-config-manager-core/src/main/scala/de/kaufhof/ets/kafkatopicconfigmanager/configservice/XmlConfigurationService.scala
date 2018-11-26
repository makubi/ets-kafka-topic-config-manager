package de.kaufhof.ets.kafkatopicconfigmanager.configservice

import java.io.InputStream
import java.net.URL

import de.kaufhof.ets.kafkatopicconfigmanager.XmlUnMarshallerProvider
import de.kaufhof.ets.kafkatopicconfigmanager.configprovider.XmlResourceFinder
import de.kaufhof.ets.kafkatopicconfigmanager.generated.Topic
import de.kaufhof.ets.kafkatopicconfigmanager.modelparser.XmlToModelParser
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.XmlTopic
import javax.xml.bind.Unmarshaller

import scala.collection.JavaConverters._

class XmlConfigurationService(xmlToModelParser: XmlToModelParser) {

  def allXmlConfigs: java.util.Map[URL, XmlTopic] = {
    val unmarshaller = XmlUnMarshallerProvider.unmarshaller

    XmlResourceFinder.allFilesInTopicFolder.map(url => url -> xmlConfig(unmarshaller, url)).toMap.asJava
  }

  private def xmlConfig(unmarshaller: Unmarshaller, url: URL): XmlTopic = {
    val topic = withInputStream(() => url.openStream()) { inputStream =>
      unmarshaller.unmarshal(inputStream).asInstanceOf[Topic]
    }

    xmlToModelParser.parseTopic(topic)
  }

  private def withInputStream[T](inputStreamF: () => InputStream)(f: InputStream => T): T = {
    val inputStream = inputStreamF()
    try {
      f(inputStream)
    } finally {
      inputStream.close()
    }
  }
}
