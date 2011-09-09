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
import java.util.Enumeration;

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
		safeToRetry = props.getBoolean("safeToRetry", false);
		this.host=gft.ondemandhosts.get("main");
		this.gft = gft; 
	}

	public boolean safeToRetry() { return safeToRetry; }
        
	public Object execute(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		logger.info("archiveAction is aangeroepen!");

		logger.info("archiveAction Stap haal op!");
		
		FileServer fileserver= new RemoteFileServer(gft.sshhosts.get(ft.channel.src.name));
		FileServerConnection fsconn=fileserver.openConnection();
		File nieuwTempDir = gft.createUniqueDir(ft.channel.name);
		String filename = ft.message.getChildText("Body/transferFile/bestand");
		String remotefile = ft.channel.srcdir + "/" + ft.message.getChildText("Body/transferFile/bestand");
		fsconn.getToLocalFile(remotefile, nieuwTempDir.getPath());
		//TODO stappen aan elkaar koppelen:
		
		logger.info("archiveAction Stap Archiveer!");
		ODServer odServer = null;
		try {
			// ONT= 1455, FAT=1460
			odServer = host.openConnection();

			System.out.println("FOLDERS: ");

			Enumeration e = odServer.getFolders();
			while (e.hasMoreElements()) {
				ODFolder f = (ODFolder) e.nextElement();
				System.out.println(f.getName() + "--" + f.getDescription());

				// f.close();
			}

			// System.out.println("\n\nCRITERIA for DUO Documenten DocID");
			System.out.println("\n\nCRITERIA for DUO Documenten");
			ODFolder f = odServer.openFolder("DUO Documenten");
			// ODFolder f = odServer.openFolder("DUO Documenten DocID");
			// System.out.println("\n\nCRITERIA for DUO Documenten KS en OS");
			// ODFolder f = odServer.openFolder("DUO Documenten KS en OS");
			Enumeration crits = f.getCriteria();
			while (crits.hasMoreElements()) {
				ODCriteria c = (ODCriteria) crits.nextElement();
				System.out.println(c.getName() + ": " + c);
			}
			f.close();

			System.out.println("\n\nCRITERIA for SCI Documenten DocID");
			ODFolder f2 = odServer.openFolder("SCI Documenten DocID");
			// System.out.println("\n\nCRITERIA for DUO Documenten KS en OS");
			// ODFolder f = odServer.openFolder("DUO Documenten KS en OS");
			Enumeration crits2 = f2.getCriteria();
			while (crits2.hasMoreElements()) {
				ODCriteria c = (ODCriteria) crits2.nextElement();
				System.out.println(c.getName() + ": " + c);
			}
			f2.close();

			// System.out.println("\n\nCRITERIA for SCI Documenten");
			// ODFolder f = odServer.openFolder("SCI Documenten");
			// Enumeration crits = f.getCriteria();
			// while (crits.hasMoreElements()) {
			// ODCriteria c = (ODCriteria) crits.nextElement();
			// System.out.println(c.getName() + ": " + c);
			// }
			// f.close();

			// System.out.println("\n\nCalling ODHit.getDocument");

			// getDocumentByDocId(odServer, "DUO Documenten DocID",
			// "B10027AA.AAD");
			// getDocumentAndAnnotationsByDocId(odServer,
			// "DUO Documenten DocID", "B10027AA.AAD");
			// getDocumentAndAnnotationsByDocId(odServer,
			// "SCI Documenten DocID", "1test");
			// getDocumentAndAnnotationsByDocId(odServer,
			// "DUO Documenten DocID", "1test");
			// storeDocument(odServer, "DUO Documenten DocID", "1test");
			storeDocument(odServer, "DUO Documenten", "C11200CC.CCC");
			// getDocument(odServer, "DUO Documenten DocId", "000002364934");

			// getDocument(odServer, "DUO Documenten KS en OS", "000002364934");
			// byte[] doc = odHit.getDocument();
			// System.out.println("Size document" + doc.length);

			// ----------
			// Cleanup
			// ----------
			odServer.logoff();
		}

		catch (ODException e) {
			System.out.println("ODException: " + e);
			System.out.println("   id = " + e.getErrorId());
			System.out.println("  msg = " + e.getErrorMsg());
			e.printStackTrace();
		}

		catch (Exception e2) {
			System.out.println("exception: " + e2);
			e2.printStackTrace();
		} finally {
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
	private static ODHit storeDocument(ODServer odServer, String folder,
			String docID) throws Exception {
		ODFolder odFolder = odServer.openFolder(folder);
		// Object [][] Fields =
		// folder.getStoreDocFields("DUOARC_LOS","DUOTIF_LOS");
		// ODCriteria odCrit = odFolder.getCriteria("DocIdVi");
		// odCrit.setSearchValue(docID);
		// String a =odCrit.getDefaultFmt();
		// String b =odCrit.getDisplayFmt();
		// String c = odCrit.getName();
		// boolean d = odCrit.isQueryable();
		// odCrit.setOperator(ODConstant.OPEqual);
		//		
		//		
		// ODCriteria odCrit2 = odFolder.getCriteria("Datum afh.");
		// ODCriteria odCrit2 = odFolder.getCriteria("OntvangenVerzonden");
		// hmm veranderd qua formaat
		// odCrit2.setSearchValues("01-12-31", "11-01-01");
		// mm/dd/jj
		// odCrit2.setSearchValues("12/31/01","01/01/11");
		// odCrit2.setOperator(ODConstant.OPBetween);

		String[] appnames = odFolder.getApplGroupNames();
		for (int k = 0; k < appnames.length; k++) {
			System.out.println("Appname: " + appnames[k]);

			Object[] obj = odFolder.getApplNames(appnames[k]);
			for (int i = 0; i < obj.length; i++) {
				System.out.println(obj[i].toString());
			}
		}

		// odFolder.setMaxHits(10);
		// Vector hits = odFolder.search();
		Object[][] dubbelArray = odFolder.getStoreDocFields("DUOARC_LOS",
				"DUOTIF_LOS");
		for (int i = 0; i < dubbelArray.length; i++) {
			Object[] enkelArray = dubbelArray[i];
			for (int j = 0; j < enkelArray.length; j++) {
				Object obj = enkelArray[j];
				System.out.println("[" + i + " " + j + "]:" + obj);
			}
		}
		// odFolder.getStoreDocFields("DUO", "DUO Documenten");
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

		// TODO werkt nog niet.
		System.out.println(System.currentTimeMillis());
		odFolder.storeDocument("C:/temp/43432.pdf",
				"DUOARC_LOS", "DUOPDF_LOS", newValues);
		System.out.println(System.currentTimeMillis());

		System.out.println("gelukt?");
		// if (hits.size()==0){
		// System.out.println("Niet gevonden....");
		// }
		//		
		// for (int i = 0; i < hits.size(); i++) {
		// ODHit odhit=(ODHit) hits.elementAt(i);
		//			
		// byte[] doc = odhit.getDocument();
		// String html=new String(doc, "UTF-16");
		// System.out.println(i+ "-size document ID"+odhit.getDocId() +
		// " lengte:  "+doc.length);
		//			
		// }
		//	
		//		
		//		
		// if (hits.size() > 0){
		// ODHit odh= (ODHit) hits.elementAt(0);
		odFolder.close();
		// return (ODHit) hits.elementAt(0);
		// }
		odFolder.close();
		return null;
		//	

	}
}
