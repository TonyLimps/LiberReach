module org.tonylimps.liberreach.core {
    requires com.alibaba.fastjson2;
	requires jdk.compiler;
	requires org.apache.logging.log4j.core;

	exports org.tonylimps.liberreach.core;
	exports org.tonylimps.liberreach.core.enums;
	exports org.tonylimps.liberreach.core.threads;
	exports org.tonylimps.liberreach.core.managers;
}
