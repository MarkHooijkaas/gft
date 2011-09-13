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

import java.io.File;
import java.nio.channels.Channels;
import java.util.Enumeration;

import nl.duo.gft.odwek.ArchiveerChannel;
import nl.duo.gft.odwek.OnDemandHost;

import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.Channel;
import org.kisst.gft.filetransfer.FileServer;
import org.kisst.gft.filetransfer.FileServerConnection;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.filetransfer.RemoteFileServer;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.edms.od.ODCriteria;
import com.ibm.edms.od.ODException;
import com.ibm.edms.od.ODFolder;
import com.ibm.edms.od.ODHit;
import com.ibm.edms.od.ODServer;

public class ArchiveAction implements Action {
	private final static Logger logger=LoggerFactory.getLogger(ArchiveAction.class);
	private final boolean safeToRetry;
	private final GftContainer gft;
    private final OnDemandHost host;
	
	public ArchiveAction(GftContainer gft, Props props) {
		// TODO: we willen channel weten
		//Channel chan=null;
		//if (chan instanceof ArchiveerChannel)
		//	throw new RuntimeException("ArchiveAction moet in een ArchiveerChannel zitten, en dat is "+chan.name+" niet");
		safeToRetry = props.getBoolean("safeToRetry", false);
		this.host=gft.ondemandhosts.get("main");
		this.gft = gft; 
	}

	public boolean safeToRetry() { return safeToRetry; }
        
	public Object execute(Task task) {
		String filename = null;		
		
		FileTransferTask ft= (FileTransferTask) task;
		logger.info("archiveAction is aangeroepen!");

		logger.info("archiveAction Stap haal op!");
		
		FileServer fileserver= new RemoteFileServer(gft.sshhosts.get(ft.channel.src.name));
		FileServerConnection fsconn=fileserver.openConnection();
		File nieuwTempDir = gft.createUniqueDir(ft.channel.name);
		filename = ft.message.getChildText("Body/transferFile/bestand");
		String remotefile = ft.channel.srcdir + "/" + filename;
		fsconn.getToLocalFile(remotefile, nieuwTempDir.getPath());
		//TODO stappen aan elkaar koppelen:


		
		//ophalen lijstje uit ondemand 
		//String kenmerkNaam = "docSoort";
		//filename = ft.message.getChildText("Body/transferFile/"+kenmerkNaam);

		
		logger.info("archiveAction Stap Archiveer!");
		
		ODServer odServer = null;
		try {
			// ONT= 1455, FAT=1460
			odServer = host.openConnection();
			// String folder = "DUO Documenten"; // TODO uit channel
			
			storeDocument(odServer, ft);
			odServer.logoff();
		}catch (ODException e) {
			System.out.println("ODException: " + e);
			System.out.println("   id = " + e.getErrorId());
			System.out.println("  msg = " + e.getErrorMsg());
			e.printStackTrace();
		}catch (Exception e2) {
			System.out.println("exception: " + e2);
			e2.printStackTrace();
		}finally {
			if (odServer != null) {
				odServer.terminate();
			}
		}

		logger.info("archiveAction is ruim localfile op!");
		
//TODO nette opruiming
		File f = new File(nieuwTempDir+"/"+filename);
		logger.info("delete localfile {}", f.getPath());
		f.delete();
		logger.info("delete localfile {}", nieuwTempDir.getPath());
		nieuwTempDir.delete();
		return null;
	}
	
	private void storeDocument(ODServer odServer, FileTransferTask ft) throws Exception {
		ArchiveerChannel channel = (ArchiveerChannel) ft.channel;
		ODFolder odFolder = odServer.openFolder(channel.odfolder);
		
		String applGroup = channel.odapplgroup;		
		String application = channel.odapplication;
		
//		String ApplGroup = "DUOARC_LOS"; // TODO uit channel;		
		//String Application = "DUOPDF_LOS"; // TODO uit channel
		
		Object[][] dubbelArray = odFolder.getStoreDocFields(applGroup, application);
		for (int i = 0; i < dubbelArray.length; i++) {
			Object[] enkelArray = dubbelArray[i];
			for (int j = 0; j < enkelArray.length; j++) {
				Object obj = enkelArray[j];
				System.out.println("[" + i + " " + j + "]:" + obj);
			}
		}
		String[] newValues = new String[17];
		for (int i = 0; i < newValues.length; i++) {
			newValues[i] = "";
		}

		// DOCTYPE
		newValues[0] = "DR_TEST";

		// DATUM afhandeling mm/dd/yy (AMERIKAANS)
		newValues[1] = "09/09/11";

		// BSN
		newValues[2] = "123456789";

		// ARCHIEFKENMERK
		newValues[3] = "DR test GAS";

		// RPTID
		newValues[4] = "DUOPDF_LOS";

		// ONTVANGEN VERZONDEN
		newValues[5] = "O";

		// DATUM rapport mm/dd/yy (AMERIKAANS)
		// newValues[6]="05/23/11";
		newValues[6] = "t";

		// tijd hh:mm:ss (AMERIKAANS)
		// newValues[7]="12:34:09";
		newValues[7] = "t";

		// CORRNR is nummer 9, max 12 lang
		newValues[8] = "131313131313";

		// DOCVIID is nummer 9, max 12 lang
		newValues[9] = "1";

		System.out.println("start store-call" + System.currentTimeMillis());

		odFolder.storeDocument("C:/temp/43432.pdf",	applGroup, application, newValues);

		System.out.println("end store-call" + System.currentTimeMillis());

		odFolder.close();

	}
}
