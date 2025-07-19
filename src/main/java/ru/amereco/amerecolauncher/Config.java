package ru.amereco.amerecolauncher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import dev.dirs.ProjectDirectories;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final String CONFIG_FILE = "config.json";
    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
    private static final ProjectDirectories projectDir = ProjectDirectories.from("ru", "amereco", "AmerecoLauncher");

    @Expose
    public String mainDir;
    @Expose
    public String username;

    public Config() {
        this.mainDir = Path.of(projectDir.dataLocalDir, ".minceraft").toString();
        this.username = "pithrilla";
    }

    public static Config load() {
        Path configPath = Path.of(projectDir.configDir, CONFIG_FILE);
        try {
            if (Files.exists(configPath)) {
                String json = Files.readString(configPath);
                return gson.fromJson(json, Config.class);
            } else {
                Config defaultConfig = new Config();
                defaultConfig.save();
                return defaultConfig;
            }
        } catch (IOException e) {
            System.err.println("Failed to load config: " + e.getMessage());
        }
        return new Config();
    }

    public void save() {
        Path configPath = Path.of(projectDir.configDir, CONFIG_FILE);
        try {
            Files.createDirectories(configPath.getParent());
            String json = toString();
            Files.writeString(configPath, json);
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
