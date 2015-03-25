package org.kisst.gft.odwek;

import org.kisst.gft.task.Task;


public  interface OnDemandField {
	public String getName();
	public String getValue(Task task);
}
