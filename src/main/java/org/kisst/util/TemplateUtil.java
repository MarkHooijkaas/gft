package org.kisst.util;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;

public class TemplateUtil {
	@SuppressWarnings("deprecation")
	private static final Configuration freemarkerConfig= new Configuration();


	@SuppressWarnings("deprecation")
	public static void init(File templ_dir) {
		freemarkerConfig.setTemplateLoader(new TemplateUtilLoader(templ_dir));
		DefaultObjectWrapper wrapper = new DefaultObjectWrapper();
		wrapper.setExposeFields(true);
		freemarkerConfig.setObjectWrapper(wrapper);
	}

	public static String processTemplate(String templateText, Object context) {
		try {
			Template templ = new Template("InternalString", new StringReader(templateText),freemarkerConfig);
			return processTemplate(templ, context);
		}
		catch (IOException e) { throw new RuntimeException(e);} 
	}

	
	public static String processTemplate(File template, Object context) {
		try {
			Template templ = new Template((template).getName(), new FileReader(template),freemarkerConfig);
			return processTemplate(templ, context);
		}
		catch (IOException e) { throw new RuntimeException(e);} 
	}
	
	public static String processTemplate(Template templ, Object context) {
		try {
			StringWriter out=new StringWriter();
			templ.process(context, out);
			return out.toString();
		}
		catch (IOException e) { throw new RuntimeException(e);} 
		catch (TemplateException e) {  throw new RuntimeException(e);}
	}
}
