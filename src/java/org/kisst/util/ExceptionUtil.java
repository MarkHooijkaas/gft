package org.kisst.util;

import javax.jms.JMSException;

import org.kisst.jms.JmsUtil;

public class ExceptionUtil {
	public static RuntimeException wrapException(Exception e) {
		if (e instanceof RuntimeException)
			return (RuntimeException) e;
		if (e instanceof JMSException)
			return JmsUtil.wrapJMSException((JMSException) e);
		return new RuntimeException(e);
	}
}
