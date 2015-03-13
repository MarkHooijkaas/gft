package org.kisst.gft.admin;

import java.util.ArrayList;

import org.kisst.gft.GftContainer;
import org.kisst.gft.admin.status.InProgressPollerFiles;
import org.kisst.gft.admin.status.NotListeningListenerThreads;
import org.kisst.gft.admin.status.ProblematicPollerFiles;
import org.kisst.gft.admin.status.StatusItem;
import org.kisst.servlet4j.ServletContainer;

public class AdminServer extends ServletContainer {

	private final GftContainer gft;
	private final ArrayList<StatusItem> statusItems =new ArrayList<StatusItem>();

	public AdminServer(GftContainer gft) {
		super(gft.props);
		this.gft=gft;
		addStatusItem(new ProblematicPollerFiles(gft));
		addStatusItem(new InProgressPollerFiles(gft));
		addStatusItem(new NotListeningListenerThreads(gft));
	}

	public void addStatusItem(StatusItem item) { statusItems.add(item); }
	
	@Override public void startListening() { 
		addServlet("default", new HomeServlet(gft, statusItems));
		addServlet("/channel", new ChannelServlet(gft));
		addServlet("/poller", new PollerServlet(gft));
		addServlet("/dir", new DirectoryServlet(gft));
		addServlet("/listener", new ListenerServlet(gft));
		addServlet("/message", new JmsMessageServlet(gft));
		addServlet("/config", new ConfigServlet(gft));
        //handlerMap.put("/restart", new RestartServlet(gft));
		addServlet("/reset", new ResetServlet(gft));
        //handlerMap.put("/shutdown", new ShutdownServlet(gft));
		addServlet("/encrypt", new EncryptServlet(gft));
        for (StatusItem item: statusItems)
        	addServlet("/"+item.getUrl(), item);
        
        super.startListening();
	}


}
