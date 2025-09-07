module org.tonylimps.liberreach.windows {

    requires javafx.controls;
    requires javafx.fxml;

    requires com.alibaba.fastjson2;
    requires org.tonylimps.liberreach.core;
	requires org.apache.logging.log4j;
	requires org.apache.logging.log4j.core;
	requires java.desktop;
	requires java.naming;

	opens org.tonylimps.liberreach.windows;
    exports org.tonylimps.liberreach.windows;
    exports org.tonylimps.liberreach.windows.controllers;
    opens org.tonylimps.liberreach.windows.controllers to javafx.fxml;
    exports org.tonylimps.liberreach.windows.managers;

    opens org.tonylimps.liberreach.windows.managers to javafx.fxml;
}
