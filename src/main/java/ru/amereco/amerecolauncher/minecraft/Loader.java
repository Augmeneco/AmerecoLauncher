/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.amereco.amerecolauncher.minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import ru.amereco.amerecolauncher.minecraft.models.Version;
import ru.amereco.amerecolauncher.minecraft.models.Library;

/**
 *
 * @author lanode
 */
public class Loader {
    private Gson gson;
    private MinecraftLauncher minecraftLauncher;
    
    Path versionsDir;
    Path librariesDir;
    Path assetsDir;
    Path nativesDir;
    
//    private VersionJSON parseVersionJSON(Path jsonPath, Path librariesPath) throws IOException {
//        JSONObject clientJson;
//        try (InputStream is = Files.newInputStream(jsonPath)) {
//            clientJson = new JSONObject(new JSONTokener(is));
//        }
//
//        VersionJSON result = new VersionJSON();
//        result.classPaths = getClassPaths(clientJson, librariesPath.toString());
//        result.mainClass = clientJson.getString("mainClass");
//        result.version = clientJson.getString("id");
//        result.versionType = clientJson.getString("type");
//        result.assetIndex = clientJson.getJSONObject("assetIndex").getString("id");
//
//        return result;
//    }
    
//    VersionJSON vj = parseVersionJSON(versionsPath.resolve(version).resolve(version + ".json"), librariesPath);
//    vj.classPaths += File.pathSeparator + versionsPath.resolve(version).resolve(version + ".jar").toString();
//
//    this.executable = "java";
//    this.classPaths = vj.classPaths;
//    this.mainClass = vj.mainClass;
//    this.assetsDir = assetsPath.toString();
//    this.assetIndex = vj.assetIndex;
//    this.gameDir = Paths.get(gameDir).toAbsolutePath().toString();
//    this.nativesDir = nativesPath.toString();
//    this.version = vj.version;
//    this.versionType = vj.versionType;
    
//    private static class VersionJSON {
//        String classPaths;
//        String mainClass;
//        String version;
//        String versionType;
//        String assetIndex;
//    }
    
    public Loader(MinecraftLauncher minecraftLauncher) {
        gson = new GsonBuilder().create();
        this.minecraftLauncher = minecraftLauncher;
        versionsDir = minecraftLauncher.mainDir.resolve("versions");
        librariesDir = minecraftLauncher.mainDir.resolve("libraries");
        assetsDir = minecraftLauncher.mainDir.resolve("assets");
        nativesDir = minecraftLauncher.mainDir.resolve("natives");
    }
    
    private List<Path> getClassPaths(Version version, Path librariesDir) {
        List<Path> paths = new ArrayList<>();
        
        for (Library library : version.libraries()) {
            if (!library.shouldUse()) continue;
            
            if (library.downloads() != null && library.downloads().artifact() != null) {
                String path = library.downloads().artifact().path();
                paths.add(librariesDir.resolve(path));
            }
        }
        return paths;
    }
    
    private Version loadVersion(Path versionsDir, String versionId) {
        try {
            JsonReader reader = new JsonReader(new FileReader(versionsDir.resolve(versionId).resolve(versionId+".json").toString()));
            return gson.fromJson(reader, Version.class);
        } catch (FileNotFoundException exc) {
            throw new RuntimeException("Failed to load version "+versionId, exc);
        }
    }
    
    private void loadClassPaths(Version version) {
        List<Path> classPaths = getClassPaths(version, librariesDir);
        minecraftLauncher.classPaths.addAll(classPaths);
    }
    
    public void loadPatch(String versionId) {
        Version version = loadVersion(versionsDir, versionId);

        loadClassPaths(version);
        if (version.mainClass() != null)
            minecraftLauncher.mainClass = version.mainClass();
    }
    
    public void loadFull(String versionId) {      
        Version version = loadVersion(versionsDir, versionId);

        loadClassPaths(version);
        minecraftLauncher.classPaths.add(versionsDir.resolve(version.id()).resolve(version.id() + ".jar")); // Add version jar to classpath
        minecraftLauncher.mainClass = version.mainClass();
        minecraftLauncher.assetsDir = assetsDir;
        minecraftLauncher.assetIndex = version.assetIndex().id();
        minecraftLauncher.nativesDir = nativesDir;
        minecraftLauncher.version = version.id();
        minecraftLauncher.versionType = version.type();
    }
}
