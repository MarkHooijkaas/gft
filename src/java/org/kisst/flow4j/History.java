package org.kisst.flow4j;

import java.util.ArrayList;

import org.kisst.cfg4j.Props;
import org.kisst.cfg4j.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class History {
	public static final Logger logger = LoggerFactory.getLogger(History.class);

	public static class Item {
		public final long timestamp;
		public final String msg;
		public final String subject;
		Item(String subject, String msg) { this.subject=subject; this.msg=msg; this.timestamp=System.currentTimeMillis();}
		Item(String subject, String msg, long timestamp) { this.subject=subject; this.msg=msg; this.timestamp=timestamp;}
	}
	private final ArrayList<Item> items=new ArrayList<Item>();

	public History() {}
	public History(Sequence items) {
		for (Object o: items) {
			Props p=(Props) o;
			this.items.add(new Item(p.getString("subject",null),p.getString("message"), p.getLong("timestamp")));
		}
	}


	public synchronized void trace(String subject, String msg) { trace(new Item(subject, msg)); }
	public synchronized void trace(String msg) { trace(new Item(null, msg)); }
	public synchronized void trace(Item item) { items.add(item); } 

	public String getTraceAsString(Props props) {
		StringBuffer buf=new StringBuffer();
		for (Item i:items) {
			buf.append(i.msg);
			buf.append('\n');
		}
		return buf.toString();
	}

}
