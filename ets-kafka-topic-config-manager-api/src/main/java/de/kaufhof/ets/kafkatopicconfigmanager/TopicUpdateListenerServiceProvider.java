package de.kaufhof.ets.kafkatopicconfigmanager;

public interface TopicUpdateListenerServiceProvider {

    void topicCreated(String environment, String topic);
    void topicDeleted(String environment, String topic);

    void topicConfigurationUpdated(String environment, String topic);
    void topicPartitionCreated(String environment, String topic);
}
