package org.kisst.gft.admin;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kisst.gft.GftContainer;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class TemplateServlet extends BaseServlet {
	private static final Configuration cfg= new Configuration();;
	static {
		//cfg.setDirectoryForTemplateLoading(new File("src/java"));
		cfg.setClassForTemplateLoading(TemplateServlet.class, "/");
		cfg.setObjectWrapper(new DefaultObjectWrapper());
	}
	
	
	public TemplateServlet(GftContainer gft) {
		super(gft);
	}

	
	public void handle(HttpServletRequest request, HttpServletResponse response) {
		try {
			HashMap<String, Object> root=new HashMap<String, Object>();
			root.put("channels", gft.channels);
			Template temp = cfg.getTemplate("org/kisst/gft/admin/Gft.template");
			Writer out = response.getWriter();
			temp.process(root, out);
			out.flush();
		}
		catch (TemplateException e) { throw new RuntimeException(e); } 
		catch (IOException e)  { throw new RuntimeException(e); }
	}
}