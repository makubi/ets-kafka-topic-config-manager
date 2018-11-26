package de.kaufhof.ets.kafkatopicconfigmanager;

import de.kaufhof.ets.kafkatopicconfigmanager.rule.ValidationResult;
import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.XmlTopic;

import java.net.URL;
import java.util.Map;

public interface TopicXmlConfigurationValidationServiceProvider {
    ValidationResult validate(Map<URL, XmlTopic> urlTopicMap);
}
