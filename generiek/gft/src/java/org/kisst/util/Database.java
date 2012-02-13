/**
Copyright 2008, 2009 Mark Hooijkaas

This file is part of the RelayConnector framework.

The RelayConnector framework is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The RelayConnector framework is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the RelayConnector framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kisst.util;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.kisst.gft.GftContainer;
import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;


public class Database  {
	private final BasicDataSource ds= new BasicDataSource();

	public Database(GftContainer gft, Props props) {
		SimpleProps context=gft.getContext(); // TODO: remove this dependency on GftContainer

		ds.setMinEvictableIdleTimeMillis(props.getLong("minEvictableIdleTimeMillis",10*60*1000));
		ds.setTimeBetweenEvictionRunsMillis(props.getLong("timeBetweenEvictionRunsMillis",20*60*1000));
		ds.setNumTestsPerEvictionRun(props.getInt("numTestsPerEvictionRun",8));
		
		int size=props.getInt("poolSize",8);
		ds.setInitialSize(props.getInt("poolInitialSize",0));
		ds.setMaxActive(props.getInt("poolMaxActive",size));
		ds.setMaxIdle(props.getInt("poolMaxIdle",size));
		ds.setMinIdle(props.getInt("poolMinIdle",0));
		
		ds.setTestOnBorrow(props.getBoolean("testOnBorrow",true));
		String validationQuery = props.getString("validationQuery","VALUES 1");
		if (validationQuery!=null)
			ds.setValidationQuery(validationQuery);
		
		ds.setDriverClassName(props.getString("driver","com.ibm.as400.access.AS400JDBCDriver"));
		ds.setUsername(TemplateUtil.processTemplate(props.getString("username"),context));
		String password=props.getString("password", null);
		if (password==null)
			password=CryptoUtil.decrypt(props.getString("encryptedPassword"));
		ds.setPassword(password);
		ds.setUrl(TemplateUtil.processTemplate(props.getString("url"),context));
	} 	

	public Connection getConnection() throws SQLException { return ds.getConnection();	}

}
