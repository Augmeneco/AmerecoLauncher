package ru.amereco.amerecolauncher.httpsync;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.json.JSONObject;
import org.json.JSONArray;

public class HTTPSync {
    private final String configURL;
    private final String baseURL;
    private final Path configPath;
    private final Path basePath;
    private final int timeout;
    private final int retryDelay;
    
    private int maxProgress;
    private int progress;
    private String stage;
    private String step;
    private Consumer<ProgressData> onProgress;
    
    private final HttpClient httpClient;

    public HTTPSync(String configURL, String baseURL, Path configPath, Path basePath, 
                   int timeout, int retryDelay) {
        this.configURL = configURL;
        this.baseURL = baseURL;
        this.configPath = configPath;
        this.basePath = basePath;
        this.timeout = timeout;
        this.retryDelay = retryDelay;

        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofMillis(timeout))
            .build();
    }

    public boolean checkUpdates() throws Exception {
        updateStage("Проверка обновлений...");
        
        JSONObject remoteConfig = getConfig(configURL);
        JSONObject localConfig = getLocalConfig(configPath);
        
        boolean hasUpdates = !remoteConfig.getString("totalHash")
            .equals(localConfig.getString("totalHash"));
        
        updateStage("Обновления проверены: " + 
            (hasUpdates ? "Есть обновления" : "Нет обновлений"));
        
        return hasUpdates;
    }

    private JSONObject getLocalConfig(Path path) throws Exception {
        if (Files.exists(path)) {
            String content = Files.readString(path);
            return new JSONObject(content);
        } else {
            JSONObject emptyConfig = new JSONObject();
            emptyConfig.put("files", new JSONArray());
            emptyConfig.put("totalHash", "");
            return emptyConfig;
        }
    }

    public void setOnProgress(Consumer<ProgressData> callback) {
        this.onProgress = callback;
    }
    private JSONObject getConfig(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();
            
        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());
            
        return new JSONObject(response.body());
    }

    private void updateStage(String stage) {
        this.stage = stage;
        if (onProgress != null) {
            onProgress.accept(new ProgressData(maxProgress, progress, stage, step));
        }
    }

    public void synchronize() throws Exception {
        updateStage("Загрузка обновлений...");
        Map<String, String> fileActions = getFileActions();
        try {
            performFileActions(fileActions);
            updateConfig();
        } finally {
            fileActions.clear();
        }
        updateStage("Обновления загружены...");
    }

    private Map<String, String> getFileActions() throws Exception {
        updateStage("Загрузка конфигов в память");
        
        JSONObject remoteConfig = getConfig(configURL);
        JSONObject localConfig = getLocalConfig(configPath);
        
        Map<String, String> actions = new HashMap<>();
        
        updateStage("Вычисление разницы файлов: Пометка файлов");
        maxProgress = remoteConfig.getJSONArray("files").length() + 
                      localConfig.getJSONArray("files").length();
        progress = 0;
        
        // Mark local files for deletion
        for (int i = 0; i < localConfig.getJSONArray("files").length(); i++) {
            JSONObject file = localConfig.getJSONArray("files").getJSONObject(i);
            String path = file.getString("path");
            updateStep(path);
            actions.put(path, "D");
        }
        
        updateStage("Вычисление разницы файлов: Сравнение");
        for (int i = 0; i < remoteConfig.getJSONArray("files").length(); i++) {
            JSONObject remoteFile = remoteConfig.getJSONArray("files").getJSONObject(i);
            String path = remoteFile.getString("path");
            updateStep(path);
            
            boolean found = false;
            for (int j = 0; j < localConfig.getJSONArray("files").length(); j++) {
                JSONObject localFile = localConfig.getJSONArray("files").getJSONObject(j);
                if (path.equals(localFile.getString("path"))) {
                    found = true;
                    if (!remoteFile.getString("hash").equals(localFile.getString("hash"))) {
                        actions.put(path, "U");
                    } else {
                        actions.remove(path);
                    }
                    break;
                }
            }
            
            if (!found) {
                actions.put(path, "C");
            }
        }
        
        updateStage("Сравнение окончено");
        return actions;
    }

    private void updateStep(String step) {
        this.step = step;
        progress++;
        if (onProgress != null) {
            onProgress.accept(new ProgressData(maxProgress, progress, stage, step));
        }
    }

    private void performFileActions(Map<String, String> actions) throws Exception {
        updateStage("Синхронизация");
        maxProgress = actions.size();
        progress = 0;
        
        for (Map.Entry<String, String> entry : actions.entrySet()) {
            updateStep(entry.getKey());
            String action = entry.getValue();
            String localPath = basePath.resolve(entry.getKey()).toString();
            
            if (action.equals("C") || action.equals("U")) {
                String remoteUrl = baseURL + URLEncoderChromium.encode(entry.getKey());
                Path dirPath = Paths.get(localPath).getParent();
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                }
                downloadFile(remoteUrl, localPath);
            } else if (action.equals("D")) {
                Files.deleteIfExists(Paths.get(localPath));
            }
        }
    }

    private void downloadFile(String url, String outputPath) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();
            
        HttpResponse<InputStream> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofInputStream());
            
        Files.copy(response.body(), Paths.get(outputPath), 
            StandardCopyOption.REPLACE_EXISTING);
    }

    private void updateConfig() throws Exception {
        Path configDir = configPath.getParent();
        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir);
        }
        downloadFile(configURL, configPath.toString());
    }

    public int getRetryDelay() {
        return retryDelay;
    }

    public static class ProgressData {
        public final int maxProgress;
        public final int progress;
        public final String stage;
        public final String step;
        
        public ProgressData(int max, int prog, String stg, String stp) {
            maxProgress = max;
            progress = prog;
            stage = stg;
            step = stp;
        }
    }

    public static class URLEncoderChromium {
        private static final String ALLOWED_CHARS = 
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz" +
            "0123456789-_.~;/?:@&=+$,!()*#";

        public static String encode(String s) {
            StringBuilder result = new StringBuilder();
            try {
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (ALLOWED_CHARS.indexOf(c) != -1) {
                        result.append(c);
                    } else {
                        byte[] utf8Bytes = String.valueOf(c).getBytes("UTF-8");
                        for (byte b : utf8Bytes) {
                            result.append(String.format("%%%02X", b & 0xFF));
                        }
                    }
                }
                return result.toString();
            } catch (UnsupportedEncodingException e) {
                return s;
            }
        }
    }
}
