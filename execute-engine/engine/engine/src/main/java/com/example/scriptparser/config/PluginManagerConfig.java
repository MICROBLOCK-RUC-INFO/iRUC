package com.example.scriptparser.config;

import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class PluginManagerConfig {

    @Value("${plugin.directory:plugins}")
    private String pluginsDir;

    @Bean
    public PluginManager pluginManager() {
        Path pluginsPath = Paths.get(pluginsDir);
        DefaultPluginManager pluginManager = new DefaultPluginManager(pluginsPath);

        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        pluginManager.getPlugins().forEach(plugin -> {
            System.out.println("Loaded plugin: " + plugin.getDescriptor().getPluginId());
        });

        return pluginManager;
    }
}
