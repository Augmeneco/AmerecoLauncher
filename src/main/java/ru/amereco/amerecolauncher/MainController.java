package ru.amereco.amerecolauncher;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.application.Platform;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ru.amereco.amerecolauncher.httpsync.HTTPSync;
import ru.amereco.amerecolauncher.httpsync.HTTPSync.ProgressData;
import ru.amereco.amerecolauncher.minecraft.MinecraftLauncher;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import ru.amereco.amerecolauncher.minecraft.mixins.Fabric;

public class MainController {
    private final Config config = Config.load();
    private HTTPSync httpSync;
    private Thread minecraftThread;
    
    @FXML private Button mainButton;
    @FXML private Button settingsBtn;
    @FXML private Button logoutBtn;
    @FXML private Button quitBtn;
    @FXML private ProgressBar progressBar;
    @FXML private Label stageLabel;
    @FXML private Label stepLabel;
    @FXML private Label progressLabel;
    @FXML private ImageView backgroundImage;

    @FXML
    public void initialize() {
        // Initialize HTTPSync
        String configUrl = "https://lanode.augmeneco.ru/rpcraft_dist/instances/rpcraft/rpcraft.json";
        String baseUrl = "https://lanode.augmeneco.ru/rpcraft_dist/";
        Path configPath = Paths.get(config.mainDir, "instances/rpcraft/rpcraft.json");
        Path basePath = Paths.get(config.mainDir);

        try {
            Files.createDirectories(basePath);
        } catch (IOException exception) {
            System.out.println("Can't create path "+basePath);
            exitProgram();
        }

        httpSync = new HTTPSync(configUrl, baseUrl, configPath, basePath, 5000, 3000);
        httpSync.setOnProgress(this::handleProgressUpdate);

        // Start initial update check
        checkUpdates();
    }

    private void checkUpdates() {
        new Thread(() -> {
            try {
                boolean hasUpdates = httpSync.checkUpdates();
                javafx.application.Platform.runLater(() -> {
                    if (hasUpdates) {
                        mainButton.setText("Обновить");
                        mainButton.setOnAction(e -> startUpdate());
                    } else {
                        mainButton.setText("Играть");
                        mainButton.setOnAction(e -> launchMinecraft());
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    stageLabel.setText("Ошибка проверки обновлений");
                    stepLabel.setText(e.getMessage());
                });
            }
        }).start();
    }

    private void startUpdate() {
        new Thread(() -> {
            try {
                httpSync.synchronize();
                javafx.application.Platform.runLater(() -> {
                    mainButton.setText("Играть");
                    mainButton.setOnAction(e -> launchMinecraft());
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    stageLabel.setText("Ошибка обновления");
                    stepLabel.setText(e.getMessage());
                });
            }
        }).start();
    }

    private void handleProgressUpdate(ProgressData progress) {
        javafx.application.Platform.runLater(() -> {
            stageLabel.setText(progress.stage);
            stepLabel.setText(progress.step);
            progressBar.setProgress(progress.maxProgress > 0 ?
                (double)progress.progress / progress.maxProgress : 0);
            progressLabel.setText(String.format("%d / %d",
                progress.progress, progress.maxProgress));
        });
    }

    private void launchMinecraft() {
        try {
            String mainDir = config.mainDir;
            String gameDir = Paths.get(mainDir, "instances/rpcraft").toString();

            mainButton.setText("Остановить");
            mainButton.setOnAction(e -> stopMinecraft());

            minecraftThread = new Thread(() -> {
                try {
                    MinecraftLauncher minecraftLauncher = new MinecraftLauncher("1.20.1", mainDir, gameDir);
                    Fabric.apply(minecraftLauncher, "fabric-1.20.1-0.16.10", mainDir);
                    minecraftLauncher.launch();
                } catch (Exception exc) {
                    javafx.application.Platform.runLater(() -> {
                        mainButton.setText("Играть");
                        mainButton.setOnAction(e -> launchMinecraft());
                        stageLabel.setText("Ошибка запуска");
                        stepLabel.setText(exc.getMessage());
                    });
                } finally {
                    javafx.application.Platform.runLater(() -> {
                        mainButton.setText("Играть");
                        mainButton.setOnAction(e -> launchMinecraft());
                    });
                }
            });
            minecraftThread.start();
        } catch (Exception exc) {
            stageLabel.setText("Ошибка инициализации");
            stepLabel.setText(exc.getMessage());
        }
    }

    public void stopMinecraft() {
        minecraftThread.interrupt();
    }
    
    @FXML
    private void playOrUpdate() {
        // Will be implemented based on current state
    }
    
    @FXML
    private void switchToLogin() throws IOException {
        App.setRoot("auth");
    }

    @FXML
    private void switchToSettings() throws IOException {
        App.setRoot("settings");
    }
    
    @FXML
    private void exitProgram() {
        Platform.exit();
    }
}
