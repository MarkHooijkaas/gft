package org.kisst.util;

import org.kisst.cfg4j.BooleanSetting;
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.MappedSetting;
import org.kisst.cfg4j.StringSetting;
import org.kisst.props4j.Props;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.Manifest;

public class JarLoader {
	public static class ModuleSetting extends CompositeSetting {
		public ModuleSetting(CompositeSetting parent, String name) { super(parent, name); }
		public final BooleanSetting disabled = new BooleanSetting(this, "disabled", false);
	}
	
	public static class Settings extends CompositeSetting {
		public Settings(CompositeSetting parent, String name) { super(parent, name); }
		public final StringSetting moduleDirectory = new StringSetting(this, "directory", "./modules");  
		public final BooleanSetting checkDuplicates = new BooleanSetting(this, "checkDuplicates", true);
		public final MappedSetting<ModuleSetting> module = new MappedSetting<ModuleSetting>(this, "module", ModuleSetting.class);
	}
	
	public static class ModuleInfo {
		public final File file;
		public final String mainClassname;
		public final String version;
		public File getFile() { return file;}
		public Date getDate() { return new Date(file.lastModified()); } 
		public String getVersion() { return version; }
		public String getMainClassname() { return mainClassname; }
		public ModuleInfo(File f) {
			this.file=f;
			try {
				@SuppressWarnings("deprecation")
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

	public JarLoader(Props props, String topName) { this(new Settings(null,topName), props);}
	public JarLoader(Settings settings, Props props) {
		this.dir=new File(settings.moduleDirectory.get(props));
		if (! dir.isDirectory())
			throw new IllegalArgumentException(dir+" should be a directory");
		for (File f:dir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(".jar")) {
				ModuleInfo info = new ModuleInfo(f);
				String modulename=info.mainClassname;
				int pos=modulename.lastIndexOf('.');
				if (pos>0)
					modulename=modulename.substring(pos+1);
				if (settings.module.get(modulename).disabled.get(props))
					System.out.println("Skipping disabled module "+modulename);
				else
					modules.add(info);
			}
		}
		int i=0;
		URL[] urls = new URL[modules.size()];
		boolean checkDuplicates=settings.checkDuplicates.get(props);
		HashMap<String, ModuleInfo> alreadyLoadedModules = new HashMap<String, ModuleInfo>();
		for (ModuleInfo m: modules) {
			if (checkDuplicates) {
				ModuleInfo info=alreadyLoadedModules.get(m.mainClassname);
				if (info!=null)
					throw new DuplicateModuleException(m, info);
				alreadyLoadedModules.put(m.mainClassname, m);
			}
			try {
				urls[i++]=m.file.toURI().toURL();
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
	
	public class DuplicateModuleException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public final ModuleInfo mod1; 
		public final ModuleInfo mod2; 
		public DuplicateModuleException(ModuleInfo mod1, ModuleInfo mod2) {
			super("Duplicate module files "+mod1.file+" and "+mod2.file+" found, both with main class "+mod1.mainClassname);
			this.mod1=mod1;
			this.mod2=mod2;
		}
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
