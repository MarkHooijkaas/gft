package org.kisst.cfg4j;

import java.util.ArrayList;

public class LayeredProps extends PropsBase {
	private final ArrayList<Props> layers= new ArrayList<Props>();

	@Override
	public Object get(String key, Object defaultValue) {
		for (Props layer: layers) {
			Object o=layer.get(key,null);
			if (o!=null)
				return o;
		}
		return defaultValue;
	}
	
	public void addLayer(Props props) { layers.add(props); }

	public Iterable<String> keys() {
		throw new RuntimeException("not implemented"); // TODO
	}

}
