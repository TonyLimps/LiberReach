module org.tonylimps.filerelay.windows {

    requires javafx.controls;
    requires javafx.fxml;

    requires com.alibaba.fastjson2;
    requires org.tonylimps.filerelay.core;
	requires org.apache.logging.log4j;
	requires org.apache.logging.log4j.core;
	requires java.desktop;
	requires java.naming;

	opens org.tonylimps.filerelay.windows;
    exports org.tonylimps.filerelay.windows;
    exports org.tonylimps.filerelay.windows.controllers;
    opens org.tonylimps.filerelay.windows.controllers to javafx.fxml;
    exports org.tonylimps.filerelay.windows.managers;

    opens org.tonylimps.filerelay.windows.managers to javafx.fxml;
}
