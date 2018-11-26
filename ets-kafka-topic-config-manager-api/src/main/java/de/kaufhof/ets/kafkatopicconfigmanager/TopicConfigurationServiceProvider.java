package de.kaufhof.ets.kafkatopicconfigmanager;

import java.net.URL;

public interface TopicConfigurationServiceProvider {
    URL[] getTopicConfigurations();
}
