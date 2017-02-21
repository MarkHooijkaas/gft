package org.kisst.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import freemarker.cache.TemplateLoader;

public class TemplateUtilLoader implements TemplateLoader {
	public static class Source {
		private final String name;
		private final File file;
		private InputStream stream=null;
		private Reader reader=null;
		private Source(String name) { this.name=name; this.file=null; } 
		private Source(String name, File f) { this.name=name; this.file=f; } 
		public boolean equals(Object other) {
			if (other instanceof Source)
				return name.equals(((Source)other).name);
			else
				return false;
		}
	}

	private final File baseDir;
	public TemplateUtilLoader(File baseDir) { 
		this.baseDir=baseDir;
		if (! baseDir.exists())
			throw new RuntimeException("Template base directory "+baseDir.getName()+" does not exist");
		if (! baseDir.isDirectory())
			throw new RuntimeException("Template base directory "+baseDir.getName()+" is not a directory");
	}

	public Object findTemplateSource(String name) {
		if (name.startsWith("file:"))
			return new Source(name, new File(baseDir, name.substring(5)));
		else
			return new Source(name);
	}

	public long getLastModified(Object source) {
		Source src=(Source) source;
		if (src.file!=null)
			return src.file.lastModified();
		else
			return 1;// string resource, so 1 second after 1970 should be nice
	}

	public Reader getReader(Object source, String encoding) throws IOException {
		Source src=(Source) source;
		if (src.reader!=null)
			throw new RuntimeException("template reader already opened "+this);
		if (src.file!=null) {
			try {
				src.stream=new FileInputStream(src.file);
				src.reader=new InputStreamReader(src.stream, encoding);
			}
			finally {
				if (src.reader==null && src.stream!=null)
					// something went wrong in creating the InputStreamReader
					// this could potentially leak resources
					src.stream.close();
			}
			return src.reader;
		}
		else {
			src.reader=new StringReader(src.name);
			return src.reader;
		}
	}
	public void closeTemplateSource(Object source) throws IOException {
		Source src=(Source) source;
		try {
			if (src.reader!=null) {
				src.reader.close();
				src.reader=null;
			}
		}
		finally {
			if (src.stream!=null) {
				src.stream.close();
				src.stream=null;
			}
		}
	}

}
