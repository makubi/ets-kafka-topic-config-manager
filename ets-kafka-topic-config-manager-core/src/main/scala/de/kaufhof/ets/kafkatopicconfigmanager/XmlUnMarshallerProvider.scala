package de.kaufhof.ets.kafkatopicconfigmanager

import javax.xml.XMLConstants
import javax.xml.bind.{JAXBContext, Unmarshaller}
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

import scala.io.Source

object XmlUnMarshallerProvider {
  private val jaxbContext = JAXBContext.newInstance("de.kaufhof.ets.kafkatopicconfigmanager.generated")

  private val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)

  private val topicXsdNameFile = "topic-xsd-name.txt"

  private val topicXsdName = withSource(
    Source.fromInputStream(
      Option(getClass.getResourceAsStream(s"/$topicXsdNameFile"))
        .getOrElse(throw new RuntimeException(s"Unable to find $topicXsdNameFile")))
    )(_.getLines().filterNot(_.startsWith("#")).mkString)

  private val schema = schemaFactory.newSchema(new StreamSource(getClass.getResourceAsStream(s"/$topicXsdName")))

  def unmarshaller: Unmarshaller = {
    val unmarshaller = jaxbContext.createUnmarshaller
    unmarshaller.setSchema(schema)

    unmarshaller
  }

  private def withSource[T](sourceF: => Source)(f: Source => T): T = {
    val source = sourceF

    try {
      f(source)
    } finally {
      source.close()
    }
  }

}
