package de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.scheme

sealed trait XmlSource

case class XmlGitSource(repository: String, path: String) extends XmlSource
case class XmlUrlSource(url: String) extends XmlSource