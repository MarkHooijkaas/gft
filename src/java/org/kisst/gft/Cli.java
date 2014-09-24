package org.kisst.gft;

import java.util.LinkedHashMap;

public class Cli {
	public class Option {
		public final String shortName;
		public final String longName;
		public final String desc;
		//private final Object defaultValue;
		protected Object value;
		private boolean isSet;
		public Option(String shortName, String longName, String desc, Object defaultValue) {
			this.shortName=shortName;
			this.longName=longName;
			this.desc=desc;
			//this.defaultValue=defaultValue;
			value=defaultValue;
			add(this);
		}
		public void set(Object value) { this.value=value; isSet=true;}
		public boolean isSet() { return isSet; }
		public int parse(String[] args, int index) {
			if (index+1>=args.length)
				throw new RuntimeException("option "+getNames()+" should have argument");
			set(args[index+1]);
			return index+2;
		}
		public String getNames() {
			if (longName==null)
				return "-"+shortName;
			else if (shortName==null)
				return "   --"+longName;
			else
				return "-"+shortName+",--"+longName;
		}
	}
	public class StringOption extends Option {
		private StringOption(String shortName, String longName, String desc, String defaultValue) {
			super(shortName, longName, desc, defaultValue);
		}
		public String get() { return (String) value; }
		@Override public String getNames() { return super.getNames()+" <str>"; }
	}
	public class BooleanOption extends Option {
		private BooleanOption(String shortName, String longName, String desc, boolean defaultValue) {
			super(shortName, longName, desc, ""+defaultValue);
		}
		public boolean get() { return Boolean.getBoolean((String) value); }
		@Override public String getNames() { return super.getNames()+" <bool>"; }
	}
	public class Flag extends Option {
		private Flag(String shortName, String longName, String desc) { 
			super(shortName, longName, desc, null);	
		}
		@Override public int parse(String[] args, int index) { set("true"); return index+1; }
	}
	
	
	
	private final LinkedHashMap<String, Option> options=new LinkedHashMap<String, Option>();
	private void add(Option o) {
		if (o.shortName!=null)
			options.put(o.shortName, o);
		if (o.longName!=null)
			options.put(o.longName, o);
	}
	public StringOption stringOption(String shortName, String longName, String desc, String defaultValue) {
		return new StringOption(shortName, longName, desc, defaultValue);
	}
	public BooleanOption booleanOption(String shortName, String longName, String desc, boolean defaultValue) {
		return new BooleanOption(shortName, longName, desc, defaultValue);
	}
	public Flag flag(String shortName, String longName, String desc) {
		return new Flag(shortName, longName, desc);
	}
	
	public String[] parse(String[] args) { 
		int index=0;
		while (index<args.length) {
			String arg=args[index];
			Option o=null;
			// Note: short options and long options are all stored in same hashtable
			// They should never clash, since a long option should be longer than 1 character
			if (arg.startsWith("--"))
				o=options.get(arg.substring(2));
			else if (arg.startsWith("-") || arg.startsWith("+"))
				o=options.get(arg.substring(1));
			else
				return subArgs(args, index);
			if (o==null)
				throw new RuntimeException("Unknown option "+arg);
			index=o.parse(args, index);
		}
		return new String[]{};
	}
	
	protected static String[] subArgs(String[] args, int pos) {
		String result[]= new String[args.length-pos];
		for (int i=pos; i<args.length; i++)
			result[i-pos]=args[i];
		return result;
	}
	
	public String getSyntax(String prefix) {
		int maxlen=0;
		for (Option opt:options.values()) {
			if (opt.longName!=null && opt.longName.length()>maxlen)
				maxlen=opt.longName.length();
		}
		StringBuilder result=new StringBuilder();
		Option last=null;
		for (Option opt:options.values()) {
			if (opt==last)
				continue;
			String name=opt.getNames();
			while (name.length()<maxlen+5)
				name+=" ";
			result.append(prefix+name+"   "+opt.desc+"\n");
			last=opt;
		}
		return result.toString();
	}
}
