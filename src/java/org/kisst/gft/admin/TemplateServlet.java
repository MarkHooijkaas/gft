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
import org.kisst.gft.GftWrapper;

public class TemplateServlet extends BaseServlet {
	private static final Configuration cfg= new Configuration();;
	static {
		//cfg.setDirectoryForTemplateLoading(new File("src/java"));
		cfg.setClassForTemplateLoading(TemplateServlet.class, "/");
		cfg.setObjectWrapper(new DefaultObjectWrapper());
	}


	private final String templateName;

	public TemplateServlet(GftWrapper wrapper) {
		super(wrapper);
		this.templateName=this.getClass().getName().replace('.', '/')+".template";
	}

	public TemplateServlet(GftWrapper wrapper, String templateName) {
		super(wrapper);
		this.templateName=templateName;
	}

	
	public void handle(HttpServletRequest request, HttpServletResponse response) {
		if (getUser(request, response)==null)
			return;
		try {
			HashMap<String, Object> root=new HashMap<String, Object>();
			root.put("gft", wrapper.getCurrentGft());
			addContext(root);

			Template temp = cfg.getTemplate(templateName);
			Writer out = response.getWriter();
			temp.process(root, out);
			out.flush();
		}
		catch (TemplateException e) { throw new RuntimeException(e); } 
		catch (IOException e)  { throw new RuntimeException(e); }
	}


	protected void addContext(HashMap<String, Object> root) {}
}
