package de.kaufhof.ets.kafkatopicconfigmanager.file.folder;

import de.kaufhof.ets.kafkatopicconfigmanager.TopicConfigurationServiceProvider;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

public class FolderTopicConfigurationServiceProvider implements TopicConfigurationServiceProvider {

    private static final String PATH_PROPERTY = "file.folder.path";
    private static final String RECURSIVE_PROPERTY = "file.folder.recursive";

    private static final String DEFAULT_RECURSIVE = Boolean.toString(false);
    private static final int DEFAULT_MAX_FOLDER_DEPTH = 1;

    @Override
    public URL[] getTopicConfigurations() {
        final String folderPath = System.getProperty(PATH_PROPERTY);

        if (folderPath == null)
            throw new IllegalArgumentException("Java property " + PATH_PROPERTY + " is not defined");

        final boolean recursive = Boolean.parseBoolean(System.getProperty(RECURSIVE_PROPERTY, DEFAULT_RECURSIVE));
        final int maxDepth = recursive ? Integer.MAX_VALUE : DEFAULT_MAX_FOLDER_DEPTH;

        final Path folder = Paths.get(folderPath);

        try {
            return Files
                    .walk(folder, maxDepth)
                    .filter(Files::isRegularFile)
                    .map(pathToURL)
                    .toArray(URL[]::new);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Function<Path, URL> pathToURL = new Function<Path, URL>() {
        @Override
        public URL apply(Path path) {
            try {
                return path.toUri().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    };
}
