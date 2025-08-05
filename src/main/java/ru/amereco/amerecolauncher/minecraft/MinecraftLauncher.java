package ru.amereco.amerecolauncher.minecraft;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class MinecraftLauncher {
    public Path mainDir;
    
    public String executable = Path.of(
        System.getProperty("java.home"), 
        "bin",
        (System.getProperty("os.name").toLowerCase().contains("windows")) ? "java.exe" : "java"
    ).toString();
    public List<Path> classPaths = new ArrayList<>();
    public String mainClass;
    public String userName = "pithrilla";
    public String userType = "mojang";
    public String version;
    public String versionType;
    public Path assetsDir;
    public String assetIndex;
    public Path gameDir;
    public Path nativesDir;
    public String uuid;
    public String accessToken;
    public List<String> additionalArguments = new ArrayList<>();

    public MinecraftLauncher(String executable, List<Path> classPaths, String mainClass,
            Path assetsDir, String assetIndex, Path gameDir, Path nativesDir) {
        this.executable = executable;
        this.classPaths = classPaths;
        this.mainClass = mainClass;
        this.assetsDir = assetsDir;
        this.assetIndex = assetIndex;
        this.gameDir = gameDir;
        this.nativesDir = nativesDir;
    }

    public MinecraftLauncher(Path mainDir, Path gameDir) throws IOException {
        this.mainDir = mainDir.toAbsolutePath();
        this.gameDir = gameDir.toAbsolutePath();
    }

    public void launch() throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(executable);
        command.add("-Djava.library.path=" + nativesDir);
        command.add("-Dminecraft.launcher.brand=amereco-launcher");
        command.add("-Dminecraft.launcher.version=1.0");
        command.add("-cp");
        command.add(classPaths.stream()
                .map(Path::toString)
//                .map((p) -> "\""+p+"\"")
                .collect(Collectors.joining(File.pathSeparator)));
        command.add(mainClass);
        command.add("--version"); command.add(version);
        command.add("--versionType"); command.add(versionType);
        command.add("--gameDir"); command.add(gameDir.toString());
        command.add("--assetsDir"); command.add(assetsDir.toString());
        command.add("--assetIndex"); command.add(assetIndex);
        command.add("--username"); command.add(userName);
        command.add("--userType"); command.add(userType);
        // command.add("--uuid"); command.add(uuid);
        // command.add("--accessToken"); command.add(accessToken);

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(new File(gameDir.toString()));

        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT).redirectError(ProcessBuilder.Redirect.INHERIT);
        
        Process process = builder.start();
        try {
            while (process.isAlive() && !Thread.currentThread().isInterrupted()) {
                Thread.sleep(100);
            }
        } catch (InterruptedException exc) {
        
        } finally {
            process.destroy();
            System.out.println("Minecraft terminated!");
        }
    }
}
