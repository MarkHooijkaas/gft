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

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;

import nl.duo.parser.FieldGroup;
import nl.duo.parser.HierarchicalPipedMessageParser;
import nl.duo.parser.PipedMessageParser;
import nl.duo.wsf.domain.Bericht;
import nl.duo.wsf.mapper.FieldGroupToBerichtMapper;

import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.PdfWriter;

public class CreateDasfPdf implements Action {
	private final static Logger logger = LoggerFactory
			.getLogger(CreateDasfPdf.class);
	private final GftContainer gft;
	private final String templateName;

	public CreateDasfPdf(GftContainer gft, Props props) {
		this.gft = gft;
		this.templateName = props.getString("template");
	}

	@Override
	public Object execute(Task task) {
		FileTransferTask ft = (FileTransferTask) task;
		PipedMessageParser pipedMessageParser = new HierarchicalPipedMessageParser();
		FieldGroup fieldGroup = pipedMessageParser.parseMessage(
				ft.getContent().getChildText("bestandsinhoud"));

		Bericht bericht = FieldGroupToBerichtMapper.map(fieldGroup);
		String text = gft.processTemplate(templateName, bericht);
		try {
			MaakPdf(text, ft);
		} catch (DocumentException e) {
			logger.error("Fout tijdens creeeren PDF, " + e.getMessage());
		} catch (IOException e) {
			logger.error("Fout tijdens creeeren PDF, " + e.getMessage());
		}

		return null;
	}

	private void MaakPdf(String text, FileTransferTask ft)
			throws DocumentException, IOException {
		System.out.println("Create PDF eerste stap");
		// step 1
		Document document = new Document();
		// step 2
		// we'll create the file in memory
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfWriter.getInstance(document, baos);
		document.open();
		document.setMargins(20f, 20f, 20f, 20f);
		
		HTMLWorker htmlWorker = new HTMLWorker(document);
		htmlWorker.parse(new StringReader(text));
		document.close();

		// let's write the file in memory to a file anyway
		FileOutputStream fos = new FileOutputStream(ft.getTempFile());
		fos.write(baos.toByteArray());
		fos.close();
	}

	@Override
	public boolean safeToRetry() {
		return false;
	}
}
