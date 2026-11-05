package it.technoade.technoblocks.config;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.StringReader;

final class Yaml {

    private Yaml() {
    }

    static YamlConfiguration of(String... lines) {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) sb.append(line).append('\n');
        return YamlConfiguration.loadConfiguration(new StringReader(sb.toString()));
    }
}
