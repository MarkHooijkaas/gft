package org.kisst.http4j;

import java.util.Set;

public interface HttpHostMap {
	public HttpHost getHttpHost(String name);
	public Set<String> getHttpHostNames();
}
