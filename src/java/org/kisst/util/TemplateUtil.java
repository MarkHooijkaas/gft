package org.kisst.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;


import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class TemplateUtil {
	private static final Configuration freemarkerConfig= new Configuration();


	public static void init(File templ_dir) {
		freemarkerConfig.setTemplateLoader(new TemplateUtilLoader(templ_dir));
		DefaultObjectWrapper wrapper = new DefaultObjectWrapper();
		wrapper.setExposeFields(true);
		freemarkerConfig.setObjectWrapper(wrapper);
	}

	public static String processTemplate(Object template, Object context) {
		try {
			StringWriter out=new StringWriter();
			Template templ;
			if (template instanceof File)
				templ=new Template(((File) template).getName(), new FileReader((File) template),freemarkerConfig);
			else if (template instanceof String)
				templ=new Template("InternalString", new StringReader((String) template),freemarkerConfig);
			else
				throw new RuntimeException("Unsupported template type "+template.getClass());
			templ.process(context, out);
			return out.toString();
		}
		catch (IOException e) { throw new RuntimeException(e);} 
		catch (TemplateException e) {  throw new RuntimeException(e);}
	}
}
