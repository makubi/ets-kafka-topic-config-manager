package de.kaufhof.ets.kafkatopicconfigmanagerexample;

import de.kaufhof.ets.kafkatopicconfigmanager.TopicConfigurationServiceProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TopicFolderConfigurationServiceProvider implements TopicConfigurationServiceProvider {

  @Override
  public URL[] getTopicConfigurations() {
      try {
          Resource[] resources = new PathMatchingResourcePatternResolver().getResources("/topics/*");

          List<URL> urls = new ArrayList<>(resources.length);
          for (Resource resource : resources) {
              urls.add(resource.getURL());
          }

          return urls.toArray(new URL[0]);
      } catch (IOException e) {
          throw new RuntimeException(e);
      }
  }
}
