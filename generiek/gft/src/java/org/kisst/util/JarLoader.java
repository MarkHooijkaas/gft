package org.kisst.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.Manifest;

import org.kisst.cfg4j.BooleanSetting;
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.MappedSetting;
import org.kisst.cfg4j.StringSetting;
import org.kisst.props4j.Props;

public class JarLoader {
	public static class ModuleSetting extends CompositeSetting {
		public final BooleanSetting skip = new BooleanSetting(this, "skip", false);
		
		public ModuleSetting(CompositeSetting parent, String name) { super(parent, name); }
	}
	
	public static class Settings extends CompositeSetting {
		public final StringSetting moduleDirectory = new StringSetting(this, "directory", "./modules");  
		public final MappedSetting<ModuleSetting> module = new MappedSetting<ModuleSetting>(this, "module", ModuleSetting.class);
		
		public Settings(CompositeSetting parent, String name) { super(parent, name); }
	}
	
	public static class ModuleInfo {
		public final File file;
		public final String mainClassname;
		public final String version;
		public File getFile() { return file;}
		public String getVersion() { return version; }
		public String getMainClassname() { return mainClassname; }
		public ModuleInfo(File f) {
			this.file=f;
			try {
				URL url=new URL("jar:"+f.toURL()+"!/META-INF/MANIFEST.MF");
				Manifest manifest = new Manifest(url.openStream());
				this.mainClassname =  manifest.getMainAttributes().getValue("Main-Class");
				this.version = manifest.getMainAttributes().getValue("Implementation-Version");
			} catch (IOException e) { throw new RuntimeException("Could not find module Main-Class in Manifest of "+f.getName(),e);}
		}
	}
	
	private final File dir;
	private final ArrayList<ModuleInfo> modules=new ArrayList<ModuleInfo>();
	private final URLClassLoader loader; 

	public JarLoader(Settings settings, Props props) {
		this.dir=new File(settings.moduleDirectory.get(props));
		if (! dir.isDirectory())
			throw new IllegalArgumentException(dir+" should be a directory");
		for (File f:dir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(".jar"))
				modules.add(new ModuleInfo(f));
		}
		int i=0;
		URL[] urls = new URL[modules.size()];
		for (ModuleInfo m: modules) {
			try {
				urls[i++]=m.file.toURL();
			} 
			catch (MalformedURLException e) { throw new RuntimeException(e); }
		}
		loader = new URLClassLoader(urls);
	}
	
	public List<ModuleInfo> getModuleInfo() { return Collections.unmodifiableList(modules); }
	public ClassLoader getClassLoader() { return loader; }
	
	public Class<?> getClass(String name) {
		try {
			return Class.forName(name, true, loader);
		} 
		catch (ClassNotFoundException e) { throw new RuntimeException(e); }
	}
	
	public List<Class<?>> getMainClasses() {
		ArrayList<Class<?>> result=new ArrayList<Class<?>>();
		for (ModuleInfo m: modules) {
			if (m.mainClassname!=null) {
				Class<?> c = getClass(m.mainClassname);
				result.add(c);
				System.out.println("Found "+c.getSimpleName()+"\tfrom file "+m.file+"\tversion "+m.version);
			}
		}
		return result;
	}
}
