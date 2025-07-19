module ru.amereco.amerecolauncher {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires org.json;
    requires dev.dirs;
    requires com.google.gson;
    requires java.base;

    opens ru.amereco.amerecolauncher to javafx.fxml;
    opens ru.amereco.amerecolauncher.httpsync to javafx.fxml, org.json;
    exports ru.amereco.amerecolauncher;
    exports ru.amereco.amerecolauncher.httpsync;
}
