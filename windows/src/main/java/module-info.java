module com.tonylimps.filerelay.client.windows {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.alibaba.fastjson2;
    requires com.tonylimps.filerelay.core;
    requires jdk.management;

    opens com.tonylimps.filerelay.client.windows to javafx.fxml;
    exports com.tonylimps.filerelay.client.windows;
}