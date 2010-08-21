/**
Copyright 2008, 2009 Mark Hooijkaas

This file is part of the Caas tool.

The Caas tool is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The Caas tool is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the Caas tool.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.kisst.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

public class FileUtil {
	public static void saveString(File filename, String content) {
		FileWriter out=null;
		try {
			out=new FileWriter(filename);
			out.write(content);
		}
		catch (IOException e) { throw new RuntimeException(e); }
		finally {
			if (out!=null) {
				try {
					out.close();
				}
				catch (IOException e) { throw new RuntimeException(e); }
			}
		}
	}

	public static void load(Properties props, String filename) {
		FileInputStream inp = null;
		try {
			inp =new FileInputStream(filename);
			props.load(inp);
		} 
		catch (java.io.IOException e) { throw new RuntimeException(e);  }
		finally {
			try {
				if (inp!=null) 
					inp.close();
			}
			catch (java.io.IOException e) { throw new RuntimeException(e);  }
		}
	}



	public static String loadString(File f) {
		try {
			return loadString(new FileReader(f));
		} catch (FileNotFoundException e) { throw new RuntimeException(e);}
	}
	public static String loadString(String filename) {
		try {
			return loadString(new FileReader(filename));
		} catch (FileNotFoundException e) { throw new RuntimeException(e);}
	}
	
	public static String loadString(Reader rdr) {
		BufferedReader inp = null;
		try {
			inp =new BufferedReader(rdr);
			StringBuilder result=new StringBuilder();
			String line;
			while ((line=inp.readLine()) != null) {
				result.append(line);
				result.append("\n");
			}
			return result.toString();
		} 
		catch (java.io.IOException e) { throw new RuntimeException(e);  }
		finally {
			try {
				if (inp!=null) 
					inp.close();
			}
			catch (java.io.IOException e) { throw new RuntimeException(e);  }
		}
	}
	
	public static FileInputStream open(File f)  {
		try {
				return new FileInputStream(f);
		}
		catch (IOException e) { throw new RuntimeException(e); }
	}

	public static String filename(String path) {
		int idx=path.lastIndexOf("/");
		if (idx>=0)
			return path.substring(idx+1);
		else
			return path;
	}

	public static String joinPaths(String basePath, String path) {
		while (basePath.endsWith("/")) 
			basePath=basePath.substring(0,basePath.length()-1);
		while (path.startsWith("/")) 
			path=path.substring(1);
		return basePath+"/"+path;
		
	}

}
