package org.kisst.gft;

import org.kisst.cfg4j.BooleanSetting;
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.IntSetting;
import org.kisst.cfg4j.StringSetting;


public class GftSettings {
	//private final static CompositeSetting gft=new CompositeSetting(null,"gft");

	private final static CompositeSetting jamon=new CompositeSetting(null,"jamon");
	public final static BooleanSetting jamonEnabled=new BooleanSetting(jamon, "enabled",true);
	public final static StringSetting jamonfilename=new StringSetting(jamon, "filename", "logs/jamon/gft-jamon-${yyyy}-${mm}-${dd}.log");
	public final static IntSetting jamonIntervalInSeconds=new IntSetting(jamon, "intervalInSeconds",300);
}