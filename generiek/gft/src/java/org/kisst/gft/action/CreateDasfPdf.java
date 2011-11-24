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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import org.kisst.gft.GftContainer;
import org.kisst.gft.action.domain.Aanvraag;
import org.kisst.gft.action.domain.Opleiding;
import org.kisst.gft.action.domain.Persoon;
import org.kisst.gft.action.domain.Woonadres;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.html.HtmlParser;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.PdfWriter;

import freemarker.template.utility.StringUtil;

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
		String pipe = ft.getContent().getChildText("bestandsinhoud");
		pipe = StringUtil.chomp(pipe);
		pipe = pipe.trim();
		logger.info("pipe {}", pipe);

		HashMap<String, String> velden = parseStrings(pipe);
		logger.info("velden {}", velden.toString());
		HashMap<Object, Object> context = new HashMap<Object, Object>();
		
		Persoon persoon= new Persoon();
		persoon.naam=velden.get("11345");
		persoon.voornamen=velden.get("11348");
		persoon.geslacht=velden.get("11341");
		
		Woonadres woonadres= new Woonadres();
		woonadres.regel1=velden.get("11347");
		
		Opleiding opleiding = new Opleiding();
		opleiding.soortStudie =velden.get("11349");
		
		Aanvraag aanvraag = new Aanvraag();
		aanvraag.soortAanvraag =velden.get("11351");
		
		//context.put("naam", velden.get("11345"));
		context.put("persoon", persoon);
		context.put("woonadres", woonadres);
		context.put("aanvraag", aanvraag);
		context.put("opleiding", opleiding);
		context.put("documentnummer", velden.get("11346"));
		logger.info("context {}", context.toString());
		String text = gft.processTemplate(templateName, context);
		try {
			MaakPdf(text, ft);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	private void MaakPdf(String text, FileTransferTask ft) throws DocumentException, IOException {
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

	private static HashMap<String, String> parseStrings(String bericht) {
		HashMap<String, String> result = new HashMap<String, String>();
		String[] parts = bericht.split("[|]");
		for (int i = 0; i < parts.length; i++) {
			String key = parts[i].substring(0, 5);
			String value = parts[i].substring(5);
			result.put(key, value);
		}
		return result;
	}
}
