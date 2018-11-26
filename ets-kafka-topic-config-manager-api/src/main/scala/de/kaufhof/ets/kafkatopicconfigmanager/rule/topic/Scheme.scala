package de.kaufhof.ets.kafkatopicconfigmanager.rule.topic

import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.scheme.XmlSource

case class XmlScheme(`type`: String, optionalKeySchema: Option[XmlSchemaTypeDescriptor], valueSchema: XmlSchemaTypeDescriptor)

case class XmlSchemaTypeDescriptor(name: String, source: XmlSource)