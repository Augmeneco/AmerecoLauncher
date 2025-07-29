open module ru.amereco.amerecolauncher {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires org.json;
    requires dev.dirs;
    requires com.google.gson;
    requires java.base;

    exports ru.amereco.amerecolauncher;
    exports ru.amereco.amerecolauncher.httpsync;
    exports ru.amereco.amerecolauncher.minecraft;
    exports ru.amereco.amerecolauncher.minecraft.fabric;
    exports ru.amereco.amerecolauncher.minecraft.mixins;
    exports ru.amereco.amerecolauncher.minecraft.models;
}
