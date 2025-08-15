module com.tonylimps.filerelay.core {
    requires com.alibaba.fastjson2;
	requires jdk.compiler;
	requires org.apache.logging.log4j;

	exports com.tonylimps.filerelay.core;
	exports com.tonylimps.filerelay.core.enums;
	exports com.tonylimps.filerelay.core.threads;
	exports com.tonylimps.filerelay.core.managers;
}
