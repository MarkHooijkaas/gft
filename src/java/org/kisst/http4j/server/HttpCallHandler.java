package org.kisst.http4j.server;

public interface HttpCallHandler {
	public void handle(HttpCall call, String subPath);
}