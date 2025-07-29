/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.amereco.amerecolauncher.minecraft;

import ru.amereco.amerecolauncher.utils.Downloader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import ru.amereco.amerecolauncher.Config;
import ru.amereco.amerecolauncher.minecraft.models.Version;
import ru.amereco.amerecolauncher.minecraft.models.VersionManifest;
import ru.amereco.amerecolauncher.minecraft.models.AssetIndex;

/**
 *
 * @author lanode
 */
public class MinecraftDownloader extends Downloader {
    private static final String VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    
    public VersionManifest getVersionManifest() throws IOException, InterruptedException {
        String response = httpGet(URI.create(VERSION_MANIFEST_URL)); 
        return gson.fromJson(response, VersionManifest.class);
    }
    
    public Version getVersion(URI versionUrl) throws IOException, InterruptedException {       
        String response = httpGet(versionUrl); 
        return gson.fromJson(response, Version.class);
    }
    
    public AssetIndex getAssetIndex(URI assetIndexUrl) throws IOException, InterruptedException {       
        String response = httpGet(assetIndexUrl); 
        return gson.fromJson(response, AssetIndex.class);
    }

    @Override
    public boolean checkUpdates() throws IOException, InterruptedException {
        var versionManifest = getVersionManifest();
        return checkUpdates(versionManifest.latest().release());
    }
    
    @Override
    public boolean checkUpdates(String versionId) throws IOException, InterruptedException {
        Path mainDir = Path.of(Config.get().mainDir);
        return !Files.exists(mainDir.resolve("versions").resolve(versionId).resolve(versionId+".json"));
    }
    
    @Override
    public void download() throws IOException, InterruptedException {
        var versionManifest = getVersionManifest();
        download(versionManifest.latest().release());
    }
    
    @Override
    public void download(String versionId) throws IOException, InterruptedException {
        updateStage("Загрузка Minecraft: библиотеки");
        maxProgress = 1;
        progress = 0;
        updateStepAndIncProgress(versionId+".json");
        
        String mainDir = Config.get().mainDir;
        
        var versionManifest = getVersionManifest();
        var versionInfo = versionManifest.versions().stream().filter(e -> e.id().equals(versionId)).findFirst().get();
        
        var version = getVersion(versionInfo.url());
        
        maxProgress = version.libraries().size()+3;
        progress = 0;
        
//        updateStepAndIncProgress(versionInfo.id()+".json");
        downloadToPathInThread(versionInfo.url(), Path.of(mainDir, "versions", versionInfo.id(), versionInfo.id()+".json"));
        
//        updateStepAndIncProgress(versionId+".jar");
        downloadToPathInThread(version.downloads().client().url(), Path.of(mainDir, "versions", versionId, versionId+".jar"));
        
        for (var library : version.libraries()) {
            if (library.shouldUse()) {
                updateStepAndIncProgress(library.name());
                downloadToPathInThread(library.downloads().artifact().url(), Path.of(mainDir, "libraries", library.downloads().artifact().path()));
            }
        }
        
        updateStage("Загрузка Minecraft: ассеты");
//        updateStepAndIncProgress(version.assetIndex().id()+".json");
        downloadToPathInThread(version.assetIndex().url(), Path.of(mainDir, "assets", "indexes", version.assetIndex().id()+".json"));
        
        var assetIndex = getAssetIndex(version.assetIndex().url());
        
        maxProgress = assetIndex.objects().size();
        progress = 0;
        
        for (Map.Entry<String, AssetIndex.Asset> entry : assetIndex.objects().entrySet()) {
//            updateStepAndIncProgress(entry.getKey());
            var hash = entry.getValue().hash();
            var folderName = hash.substring(0, 2);
            downloadToPathInThread(URI.create("https://resources.download.minecraft.net/"+folderName+"/"+hash), 
                           Path.of(mainDir, "assets", "objects", folderName, hash));
        }
        
        waitUntilDownload();
    }
}
