package org.kisst.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import org.kisst.gft.GftSettings;
import org.kisst.props4j.Props;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorComposite;
import com.jamonapi.MonitorFactory;

public class JamonUtil {
	
	public static String getFirstDnPart(String dn) {
		int pos=dn.indexOf('=');
		int pos2=dn.indexOf(',',pos);
		if (pos<0 ||pos2<0)
			return dn;
		else
			return dn.substring(pos+1, pos2);
	}
	
	private static Comparator<Monitor> comparator = new Comparator<Monitor>() {
		public int compare(Monitor o1, Monitor o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getLabel(), o2.getLabel());
		}
	};
	
	private static void logAndResetAllTimers(String filename, String message) {
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		StringBuilder msg= new StringBuilder();
		msg.append(format.format(new Date()));
        msg.append('\t');
		msg.append(message);
        msg.append('\n');
        MonitorComposite rootMon = MonitorFactory.getRootMonitor();
        Monitor[] monitors = null;
        if (rootMon!=null) 
        	monitors=rootMon.getMonitors();
        if (monitors!=null) {
        	Arrays.sort(monitors,comparator);
        	for (Monitor mon : monitors){
        		if (mon.getHits()==0)
        			continue;
        		msg.append(format.format(new Date()));
        		msg.append('\t');
        		msg.append(mon);
        		msg.append('\n');
        		mon.reset();
        	}
        }

        FileOutputStream out = null;
		try {
			out = new FileOutputStream(filename, true);
			out.write(msg.toString().getBytes());
		} catch (IOException e) { throw new RuntimeException(e);}
		finally {
			if (out!=null) {
				try { out.close(); }
				catch (IOException e) { throw new RuntimeException(e);}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void jamonLog(Props props, String message) {
		if (props==null) {
			//logger.warn("Could not perform jamon logging because properties are not available for jamonLog: "+message);
			return;
		}
		if (! GftSettings.jamonEnabled.get(props))
			return;
		String filename=GftSettings.jamonfilename.get(props);
		Date now = new Date();
		filename = filename.replace("${yyyy}", ""+(now.getYear()+1900));
		filename = filename.replace("${mm}", ""+(now.getMonth()+1));
		filename = filename.replace("${dd}", ""+(now.getDate()));
		filename = filename.replace("${dollar}", "$");
		JamonUtil.logAndResetAllTimers(filename, message);
	}

	
	public static class JamonThread implements Runnable {
		private boolean running=true;
		private Thread myThread=null;
		private final Props props;
		public JamonThread(Props props) {
			this.props=props;
		} 
		public void run() {
			myThread = Thread.currentThread();
			while (running) {
				int interval = GftSettings.jamonIntervalInSeconds.get(props);
				try {
					if (interval<=0)
						Thread.sleep(600*1000); // sleep 10 minutes, could be any time
					else
						Thread.sleep(interval*1000);
					synchronized(this) {
						if (interval>0)
							jamonLog(props, "TIMER expired after "+interval+" seconds, dumping all statistics");
					}
				} catch (InterruptedException e) { /* ignore, probably a reset or stop */  }
			}
			myThread=null;
		}
		public synchronized void stop() {
			running=false;
			reset();
		}
		public synchronized void  reset() {
			if (myThread!=null)
				myThread.interrupt();
		}
	}
}

