package org.kisst.util;

import org.kisst.props4j.Props;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtil {

	public static String quotedName(String name) {
		if (name.indexOf(' ')>=0 || name.indexOf('.')>=0)
			return '"'+name+'"';
		else
			return name;
	}

	public static String substituteDate(String str) {
		StringBuilder result = new StringBuilder();
		Date now=new Date();
		int prevpos=0;
		int pos=str.indexOf("${DATE:");
		while (pos>=0) {
			int pos2=str.indexOf("}", pos);
			if (pos2<0)
				throw new RuntimeException("Unbounded ${ starting with "+str.substring(pos,pos+10));
			String format=str.substring(pos+7,pos2);
			result.append(str.substring(prevpos,pos));
			SimpleDateFormat sdf= new SimpleDateFormat(format);
			result.append(sdf.format(now));
			prevpos=pos2+1;
			pos=str.indexOf("${DATE:",prevpos);
		}
		result.append(str.substring(prevpos));
		return result.toString();
	}


	public static String substitute(String str, Props vars) {
		StringBuilder result = new StringBuilder();
		int prevpos=0;
		int pos=str.indexOf("${");
		while (pos>=0) {
			int pos2=str.indexOf("}", pos);
			if (pos2<0)
				throw new RuntimeException("Unbounded ${ starting with "+str.substring(pos,pos+10));
			String key=str.substring(pos+2,pos2);
			result.append(str.substring(prevpos,pos));
			Object value=vars.get(key,null);
			if (value==null && key.equals("dollar"))
				value="$";
			if (value==null)
				throw new RuntimeException("Unknown variable ${"+key+"}");
			result.append(value.toString());
			prevpos=pos2+1;
			pos=str.indexOf("${",prevpos);
		}
		result.append(str.substring(prevpos));
		return result.toString();
	}
	
	public static String doubleQuotedString(String str) {
		StringBuilder result= new StringBuilder("\""); 
		for (int i=0; i<str.length(); i++) {
			char ch=str.charAt(i);
			if (ch=='\n') { result.append("\\n"); continue; }
			if (ch=='\r') { result.append("\\r"); continue; }
			if (ch=='\t') { result.append("\\t"); continue; }
			if (ch=='"' || ch=='\\' || ch=='$') result.append('\\');
			result.append(ch);
		}
		result.append('"');
		return result.toString();
	}

	public static String singleQuotedString(String str) {
		StringBuilder result= new StringBuilder("'"); 
		for (int i=0; i<str.length(); i++) {
			char ch=str.charAt(i);
			if (ch=='\n') { result.append("\\n"); continue; }
			if (ch=='\r') { result.append("\\r"); continue; }
			if (ch=='\t') { result.append("\\t"); continue; }
			if (ch=='\'' || ch=='\\' ) result.append('\\');
			result.append(ch);
		}
		result.append("'");
		return result.toString();
	}
}
