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

import nl.duo.gft.odwek.ArchiveerChannel;
import nl.duo.gft.odwek.OnDemandHost;

import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileServer;
import org.kisst.gft.filetransfer.FileServerConnection;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.filetransfer.RemoteFileServer;
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
		File file = new File(nieuwTempDir+"/"+filename);
		
		logger.info("archiveAction Stap Archiveer!");
		
		ODServer odServer = null;
		try {
			// ONT= 1455, FAT=1460
			logger.info("open connection met {}",host);
			odServer = host.openConnection();
			logger.info("connection {}",host);

			// String folder = "DUO Documenten"; // TODO uit channel
			
			storeDocument(odServer, ft, file);
			odServer.logoff();
		}
		catch (ODException e) { throw new RuntimeException(e.getMessage()+", id="+e.getErrorId()+", msg="+e.getErrorMsg(), e); } 
		catch (Exception e) { throw new RuntimeException(e);}
		finally {
			if (odServer != null) {
				odServer.terminate();
			}
		}

		logger.info("archiveAction is ruim localfile op!");
		
//TODO nette opruiming
		//File f = new File(nieuwTempDir+"/"+filename);
	//	logger.info("delete localfile {}", file.getPath());
	//	file.delete();
	//	logger.info("delete localfile {}", nieuwTempDir.getPath());
	//	nieuwTempDir.delete();
	//	return null;
		
		// Deletes all files and subdirectories under dir.
		// Returns true if all deletions were successful.
		// If a deletion fails, the method stops attempting to delete and returns false.

		boolean gelukt = deleteDir(nieuwTempDir);
		if (gelukt){
			logger.info("verwijderen van directorie {}, inclusief bestanden, is gelukt", nieuwTempDir.getPath());
			}
		else {
			logger.error("verwijderen van directorie {} is niet gelukt", nieuwTempDir.getPath());	
			}
	
		return null;
	}
	
	public static boolean deleteDir(File dir) {
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            boolean success = deleteDir(new File(dir, children[i]));
	            if (!success) {
	                return false;
	            }
	        }
	    }

	    // The directory is now empty so delete it
	    return dir.delete();
	}

	private void storeDocument(ODServer odServer, FileTransferTask ft, File file) throws Exception {
		ArchiveerChannel channel = (ArchiveerChannel) ft.channel;
		ODFolder odFolder = odServer.openFolder(channel.odfolder);
		
		String applGroup = channel.odapplgroup;		
		String application = channel.odapplication;
//		String ApplGroup = "DUOARC_LOS"; // TODO uit channel;		
		//String Application = "DUOPDF_LOS"; // TODO uit channel
		Object[][] dubbelArray = odFolder.getStoreDocFields(applGroup, application);
		String[] docFields = new String[dubbelArray.length];
		for (int i = 0; i < dubbelArray.length; i++) {
			String waarde = null;
			String docField = (String) dubbelArray[i][0];
			ArchiveerChannel.Field fielddef=channel.fields.get(docField);
			waarde = ft.message.getChildText("Body/transferFile/extra/kenmerken/?"+docField );
			if (fielddef!=null) {
				if (waarde==null && fielddef.optional==false)
					throw new RuntimeException("veld "+docField+" is niet optioneel en niet megegeven");
				if (waarde==null && fielddef.defaultValue!=null)
					waarde=fielddef.defaultValue;
				if (fielddef.fixedValue != null)
					waarde=fielddef.fixedValue;
			}
			
			logger.info("waarde is {}", waarde);
			docFields[i]=waarde;
		}
		logger.info("docFields is: {}", docFields);
		odFolder.storeDocument(file.getPath(), applGroup, application, docFields);
		odFolder.close();

	}
}
