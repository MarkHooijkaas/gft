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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ReflectionUtil {

	public static Constructor<?> getConstructor(String classname, Class<?>[] signature) {
		try {
			Class<?> c = Class.forName(classname);
			return getConstructor(c, signature);
		} catch (ClassNotFoundException e) { throw new RuntimeException(e); }
	}

	public static Field getField(Class<?> cls, String name) {
		try {
			return cls.getDeclaredField(name);
		} 
		catch (SecurityException e) {throw new RuntimeException(e); }
		catch (NoSuchFieldException e) { return null; }
	}

	public static Method getMethod(Class<?> cls, String name, Class<?>[] signature) {
		Method[] metharr = cls.getDeclaredMethods();
		for (Method meth :metharr) {
			if (name.equals(meth.getName())) {
				Class<?>[] paramtypes = meth.getParameterTypes();
				if (java.util.Arrays.equals(signature, paramtypes))
					return meth;
			}
		}
		return null;
	}

	public static Constructor<?> getFirstCompatibleConstructor(Class<?> cls, Class<?>[] signature) {
		Constructor<?>[] consarr = cls.getDeclaredConstructors();
		for (int i=0; i<consarr.length; i++) {
			Class<?>[] paramtypes = consarr[i].getParameterTypes();
			if (paramtypes.length!=signature.length)
				continue;
			boolean compatible=true;
			for (int j=0; j<signature.length; j++) {
				if (!signature[j].isAssignableFrom(paramtypes[j]))
					compatible=false;
			}
			if (compatible)
				return consarr[i];
		}
		return null;
	}

	public static Constructor<?> getConstructor(Class<?> cls, Class<?>[] signature) {
		Constructor<?>[] consarr = cls.getDeclaredConstructors();
		for (int i=0; i<consarr.length; i++) {
			Class<?>[] paramtypes = consarr[i].getParameterTypes();
			if (java.util.Arrays.equals(signature, paramtypes))
				return consarr[i];
		}
		return null;
	}
	
	public static Object invoke(Object o, Method m, Object[] args) {
		try {
			m.setAccessible(true);
			return m.invoke(o, args);
		}
		catch (IllegalAccessException e) { throw new RuntimeException(e); }
		catch (InvocationTargetException e) {e.getCause().printStackTrace(); throw new RuntimeException(e.getCause()); }
	}
	public static Object invoke(Object o, String name, Object[] args) {
		return invoke(o.getClass(),o, name, args);
	}
	public static Object invoke(Class<?> c, Object o, String name, Object[] args) {
		try {
			return invoke(o, c.getDeclaredMethod(name, getSignature(args)), args);
		}
		catch (NoSuchMethodException e) { throw new RuntimeException(e); }
	}

	private static Class<?>[] getSignature(Object[] args) {
		Class<?>[] signature=new Class[args.length];
		for (int i=0; i<args.length; i++)
			signature[i]=args[i].getClass();
		return signature;
	}

	public static Class<?> findClass(String name) {
		try {	
			return Class.forName(name);
		}
		catch (IllegalArgumentException e) { throw new RuntimeException(e); }
		catch (ClassNotFoundException e) { throw new RuntimeException(e); }
	}

	public static Object createObject(String classname, Object[] args) {
		return createObject(findClass(classname), args);
	}
	public static Object createObject(Class<?> c, Object[] args) {
		Constructor<?> cons= getFirstCompatibleConstructor(c,getSignature(args));
		return createObject(cons,args);
	}
			
	public static Object createObject(Constructor<?> cons, Object[] args) {
		try {
			cons.setAccessible(true);
			return cons.newInstance(args);
		}
		catch (IllegalAccessException e) { throw new RuntimeException(e); } 
		catch (InstantiationException e) { throw new RuntimeException(e); }
		catch (InvocationTargetException e) { throw new RuntimeException(e); }
	}

	public static Object createObject(String classname) {
		return createObject(findClass(classname));
	}
	public static Object createObject(Class<?> c) {
		try {
			return c.newInstance();
		}
		catch (IllegalArgumentException e) { throw new RuntimeException(e); }
		catch (IllegalAccessException e) { throw new RuntimeException(e); } 
		catch (InstantiationException e) { throw new RuntimeException(e); }
	}
	
	public static String toString(Object obj, String... fields) {
		StringBuilder result= new StringBuilder(obj.getClass().getSimpleName()+"(");
		String sep="";
		if (fields!=null) for (String field: fields) {
			if (field!=null) {
				result.append(sep);
				result.append(field);
				sep=",";
			}
		}
		result.append(")");
		return result.toString();
	}
	
	public static String toString(Object obj) { return toString(obj,0); }
	public static String toString(Object obj, int depth) {
		// TODO: special support for Hashmaps? Lists, etc. Better indentation, newline support etc?
		if (obj==null)
			return "null";
		if (obj instanceof String)
			return "\""+obj+"\""; // TODO: escape quotes?
		if (obj instanceof Number)
			return obj.toString();
		if (obj instanceof Boolean)
			return obj.toString();
		if (depth<0)
			return obj.getClass().getSimpleName()+"()";
		depth--;
		StringBuilder result= new StringBuilder();
		result.append(obj.getClass().getSimpleName()+"(");
		String sep="";
		for (Field field : obj.getClass().getFields()) {
			try {
				if (! Modifier.isStatic(field.getModifiers())) {
					result.append(sep+field.getName()+"="+toString(field.get(obj), depth));
					sep=", ";
				}
			} 
			catch (IllegalArgumentException e) { throw new RuntimeException(e);} 
			catch (IllegalAccessException e) { throw new RuntimeException(e);}
		}
		result.append(")");
		return result.toString();
	}
}