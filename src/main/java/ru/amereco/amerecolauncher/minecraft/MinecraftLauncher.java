package ru.amereco.amerecolauncher.minecraft;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class MinecraftLauncher {
    public Path mainDir;
    
    public String executable = "java";
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
//
//    private boolean ruleAllows(JSONObject rule) {
//        String action = rule.optString("action", "allow");
//        boolean useLib = action.equals("disallow");
//
//        if (rule.has("os")) {
//            JSONObject os = rule.getJSONObject("os");
//            String osName = os.optString("name");
//            String arch = os.optString("arch");
//            String currentOS = System.getProperty("os.name").toLowerCase();
//            String currentArch = System.getProperty("os.arch").contains("64") ? "x64" : "x86";
//
//            if (!osName.isEmpty()) {
//                if (!currentOS.contains(osName.toLowerCase())) {
//                    return useLib;
//                }
//            }
//            if (!arch.isEmpty()) {
//                if (!arch.equals(currentArch)) {
//                    return useLib;
//                }
//            }
//        }
//        return !useLib;
//    }
//
//    private boolean shouldUseLibrary(JSONObject lib) {
//        if (!lib.has("rules")) return true;
//
//        JSONArray rules = lib.getJSONArray("rules");
//        for (int i = 0; i < rules.length(); i++) {
//            if (!ruleAllows(rules.getJSONObject(i))) return false;
//        }
//        return true;
//    }
//
//    private String getClassPaths(JSONObject clientJson, String mcDir) {
//        JSONArray libraries = clientJson.getJSONArray("libraries");
//        List<String> paths = new ArrayList<>();
//
//        for (int i = 0; i < libraries.length(); i++) {
//            JSONObject lib = libraries.getJSONObject(i);
//            if (!shouldUseLibrary(lib)) continue;
//
//            JSONObject downloads = lib.optJSONObject("downloads");
//            if (downloads != null && downloads.has("artifact")) {
//                JSONObject artifact = downloads.getJSONObject("artifact");
//                String path = artifact.getString("path");
//                paths.add(Paths.get(mcDir, path).toString());
//            }
//        }
//        return String.join(File.pathSeparator, paths);
//    }

    public void launch() throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(executable);
        command.add("-Djava.library.path=" + nativesDir);
        command.add("-Dminecraft.launcher.brand=amereco-launcher");
        command.add("-Dminecraft.launcher.version=1.0");
        command.add("-cp");
        command.add(classPaths.stream().map(Path::toString).collect(Collectors.joining(":")));
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
            while (process.isAlive() || !Thread.currentThread().isInterrupted()) {
                Thread.sleep(100);
            }
        } catch (InterruptedException exc) {
        
        } finally {
            process.destroy();
            System.out.println("Minecraft terminated!");
        }
    }
}
