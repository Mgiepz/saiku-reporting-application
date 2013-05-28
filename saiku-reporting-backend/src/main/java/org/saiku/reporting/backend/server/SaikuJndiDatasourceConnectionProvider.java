/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved.
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 *
 * @created Apr 6, 2009
 * @author wseyler
 */
package org.saiku.reporting.backend.server;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;

public class SaikuJndiDatasourceConnectionProvider implements ConnectionProvider {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String jndiName;
	private String username;
	private String password;

	private Log log = LogFactory.getLog(SaikuJndiDatasourceConnectionProvider.class);

	/*
	 * Default constructor
	 */
	public SaikuJndiDatasourceConnectionProvider() {
		super();
	}

	/**
	 * Although named getConnection() this method should always return a new
	 * connection when being queried or should wrap the connection in a way so
	 * that calls to "close()" on that connection do not prevent subsequent
	 * calls to this method to fail.
	 *
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Connection createConnection(final String user, final String password) throws SQLException {
        String dataSourceName = jndiName;
		try {

			Context initContext = new InitialContext();
			
			showJndiContext(initContext, "", "" );

			if (jndiName != null && !jndiName.startsWith("java:comp/env/")) {
				dataSourceName = "java:comp/env/" + jndiName;
			}

			DataSource datasource = (DataSource)initContext.lookup(dataSourceName);

			return datasource.getConnection();

		} catch (NamingException e) {
			throw new SQLException("Context not found: " + jndiName + " dsname: " + dataSourceName, e);
		}
	}
	

	public String getJndiName() {
		return jndiName;
	}

	public void setJndiName(final String jndiName) {
		this.jndiName = jndiName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public void showJndiContext( Context ctx, String name, String space )
	{
		if( null == name  ) name  = "";
		if( null == space ) space = "";
		try {
			NamingEnumeration<NameClassPair> en = ctx.list( name );
			while( en != null && en.hasMoreElements() ) {
				String delim = ( name.length() > 0 ) ? "/" : "";
				NameClassPair ncp = en.next();
				log.debug( space + name + delim + ncp );
				if( space.length() < 40 )
					showJndiContext( ctx, ncp.getName(), "    " + space );
			}
		} catch( javax.naming.NamingException ex ) {
			// Normalerweise zu ignorieren
		}
	}

	@Override
	public Object getConnectionHash() {
		// TODO Auto-generated method stub
		return null;
	}
}
