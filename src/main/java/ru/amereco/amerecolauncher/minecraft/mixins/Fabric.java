package ru.amereco.amerecolauncher.minecraft.mixins;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import ru.amereco.amerecolauncher.minecraft.MinecraftLauncher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;

public class Fabric {
    public static void apply(MinecraftLauncher launcher, String version, String mainDir) throws IOException {
        Path basePath = Paths.get(mainDir).toAbsolutePath();
        Path versionJsonPath = basePath.resolve("versions").resolve(version).resolve(version + ".json");

        JSONObject clientJson;
        try (InputStream is = Files.newInputStream(versionJsonPath)) {
            clientJson = new JSONObject(new JSONTokener(is));
        }

        String librariesDir = basePath.resolve("libraries").toString();
        String additionalClassPaths = getClassPaths(clientJson, librariesDir);

        launcher.classPaths += File.pathSeparator + additionalClassPaths;
        launcher.classPaths += File.pathSeparator + basePath.resolve("versions").resolve(version).resolve(version + ".jar");

        launcher.mainClass = clientJson.getString("mainClass");
    }

    private static String getClassPaths(JSONObject clientJson, String mcDir) {
        JSONArray libraries = clientJson.getJSONArray("libraries");
        List<String> paths = new ArrayList<>();

        for (int i = 0; i < libraries.length(); i++) {
            JSONObject lib = libraries.getJSONObject(i);
            String name = lib.getString("name");

            String path = getLibraryPath(name, mcDir);
            paths.add(path);
        }

        return String.join(File.pathSeparator, paths);
    }

    private static String getLibraryPath(String name, String mcDir) {
        // Example: "net.fabricmc:sponge-mixin:0.11.4+mixin.0.8.5"
        String[] parts = name.split(":");
        if (parts.length != 3) throw new IllegalArgumentException("Invalid library name format: " + name);

        String group = parts[0];
        String artifact = parts[1];
        String version = parts[2];

        String groupPath = group.replace('.', File.separatorChar);
        Path path = Paths.get(mcDir, groupPath, artifact, version, artifact + "-" + version + ".jar");

        return path.toString();
    }
}
