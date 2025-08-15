module com.tonylimps.filerelay.windows {

    requires javafx.controls;
    requires javafx.fxml;

    requires com.alibaba.fastjson2;
    requires com.tonylimps.filerelay.core;
	requires org.apache.logging.log4j;
	requires org.apache.logging.log4j.core;

	opens com.tonylimps.filerelay.windows to javafx.fxml;
    exports com.tonylimps.filerelay.windows;
    exports com.tonylimps.filerelay.windows.controllers;
    opens com.tonylimps.filerelay.windows.controllers to javafx.fxml;
    exports com.tonylimps.filerelay.windows.managers;
    opens com.tonylimps.filerelay.windows.managers to javafx.fxml;
}
