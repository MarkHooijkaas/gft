package org.kisst.cfg4j;

public class DefaultSpecification {
	final StringBasedSetting setting;
	final String defaultValue;
	public DefaultSpecification(StringBasedSetting setting, String defaultValue) {
		this.setting=setting;
		this.defaultValue=defaultValue;
	}
}
