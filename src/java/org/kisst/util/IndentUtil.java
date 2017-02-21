package org.kisst.util;

public class IndentUtil {
	public static interface Indentable { public String toIndentedString(String indent); }
	public static String toIndentedString(Object obj, String indent) {
		if (obj instanceof Indentable)
			return ((Indentable)obj).toIndentedString(indent);
		return ""+obj;
	}
}
