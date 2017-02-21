package org.kisst.props4j;

public class PrintProps {
	public static void main(String[] args) {
		SimpleProps props=new SimpleProps();
		props.read(System.in);
		props.put("dummy", "OK");
		for (String arg:args) {
			if (".".equals(arg))
				System.out.println(props.toString());
			else
				System.out.println(props.get(arg,null));
		}
	}
}
