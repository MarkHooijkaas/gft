/**
Copyright 2008, 2009 Mark Hooijkaas

This file is part of the RelayConnector framework.

The RelayConnector framework is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The RelayConnector framework is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the RelayConnector framework.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.kisst.gft.action;

import nl.duo.gft.odwek.ArchiveerChannel;
import nl.duo.gft.odwek.OnDemandHost;

import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.edms.od.ODException;
import com.ibm.edms.od.ODFolder;
import com.ibm.edms.od.ODServer;

public class ArchiveAction implements Action {
	private final static Logger logger=LoggerFactory.getLogger(ArchiveAction.class);
	private final boolean safeToRetry;
    private final OnDemandHost host;
	
	public ArchiveAction(GftContainer gft, Props props) {
		// TODO: we willen channel weten
		//Channel chan=null;
		//if (chan instanceof ArchiveerChannel)
		//	throw new RuntimeException("ArchiveAction moet in een ArchiveerChannel zitten, en dat is "+chan.name+" niet");
		safeToRetry = props.getBoolean("safeToRetry", false);
		this.host=gft.ondemandhosts.get("main");
	}

	public boolean safeToRetry() { return safeToRetry; }
        
	public Object execute(Task task) {
		
		FileTransferTask ft= (FileTransferTask) task;
	
		logger.info("archiveAction is aangeroepen!");		
		logger.info("archiveAction Stap Archiveer!");
		
		logger.info("open connection met {}",host);
		
		ODServer odServer = host.openConnection();
		try {
			// ONT= 1455, FAT=1460
			logger.info("connection {}",host);
			storeDocument(odServer, ft);
			
		}
		catch (ODException e) { throw new RuntimeException(e.getMessage()+", id="+e.getErrorId()+", msg="+e.getErrorMsg(), e); } 
		catch (Exception e) { throw new RuntimeException(e);}
		finally {
			try {
					odServer.logoff();
				} 
			catch (Exception e) { throw new RuntimeException(e);}
		}


		return null;
	}
	
	private void storeDocument(ODServer odServer, FileTransferTask ft) throws Exception {
		ArchiveerChannel channel = (ArchiveerChannel) ft.channel;
		ODFolder odFolder = odServer.openFolder(channel.odfolder);
		
		String applGroup = channel.odapplgroup;		
		String application = channel.odapplication;

		Object[][] dubbelArray = odFolder.getStoreDocFields(applGroup, application);
		String[] docFields = new String[dubbelArray.length];
		for (int i = 0; i < dubbelArray.length; i++) {
			String waarde = null;
			String docField = (String) dubbelArray[i][0];
			ArchiveerChannel.Field fielddef=channel.fields.get(docField);
			waarde = ft.content.getChildText("kenmerken/?"+docField );
			if (fielddef!=null) {
				if (waarde==null && fielddef.optional==false)
					throw new RuntimeException("veld "+docField+" is niet optioneel en niet meegegeven");
				if (waarde==null && fielddef.defaultValue!=null)
					waarde=fielddef.defaultValue;
				if (fielddef.fixedValue != null)
					waarde=fielddef.fixedValue;
			}
			
			logger.info("{} is {}", docField, waarde);
			docFields[i]=waarde;
		}
		if (logger.isInfoEnabled()) {
			String str="";
			for (int i=0; i<docFields.length; i++)
				str+=", "+docFields[i];
			logger.info("docFields is [{}]", str);
		}
		
		// This check is important because otherwise JVM will crash
		if (! ft.getTempFile().exists())
			throw new RuntimeException("Te archiveren bestand "+ft.getTempFile()+" bestaat niet");
		
		odFolder.storeDocument(ft.getTempFile().getPath(), applGroup, application, docFields);
		odFolder.close();

	}
}
