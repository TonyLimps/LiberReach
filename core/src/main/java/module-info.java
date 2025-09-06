module org.tonylimps.filerelay.core {
    requires com.alibaba.fastjson2;
	requires jdk.compiler;
	requires org.apache.logging.log4j.core;

	exports org.tonylimps.filerelay.core;
	exports org.tonylimps.filerelay.core.enums;
	exports org.tonylimps.filerelay.core.threads;
	exports org.tonylimps.filerelay.core.managers;
}
