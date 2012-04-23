package org.kisst.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;

public class JarLoader {
	
	private final File dir;
	private final File[] files;
	private final URLClassLoader loader; 

	public JarLoader(String filename) {
		this.dir=new File(filename);
		if (! dir.isDirectory())
			throw new IllegalArgumentException(filename+" should be a directory");
		files = dir.listFiles();
		URL[] urls = new URL[files.length];
		int i=0;
		for (File f:files) {
			try {
				urls[i++]=f.toURL();
			} 
			catch (MalformedURLException e) { throw new RuntimeException(e); }
		}
		loader = new URLClassLoader(urls);
	}
	
	public ClassLoader getClassLoader() { return loader; }
	
	public Class<?> getClass(String name) {
		try {
			return Class.forName(name, true, loader);
		} 
		catch (ClassNotFoundException e) { throw new RuntimeException(e); }
	}
	
	public List<Class<?>> getMainClasses() {
		ArrayList<Class<?>> result=new ArrayList<Class<?>>();
		for (File f: files) {
			try {
				URL url=new URL("jar:"+f.toURL()+"!/META-INF/MANIFEST.MF");
				Manifest manifest =new Manifest(url.openStream());
				String classname =  manifest.getMainAttributes().getValue("Main-Class");
				System.out.println(url+"\t"+classname);
				if (classname!=null)
					result.add(getClass(classname));
				
			} catch (IOException e) { throw new RuntimeException(e);}
		}
		return result;
	}
}
