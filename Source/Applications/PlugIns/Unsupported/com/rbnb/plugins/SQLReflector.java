/*
Copyright 2007 Creare Inc.

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/

package com.rbnb.plugins;

import java.sql.*;
/**
  * PlugIn which reflects input data as the output.  If no input is provided,
  *  a string is returned to that effect.
  */
public class SQLReflector implements SimplePlugIn.PlugInCallback
{
private boolean dbOpen=false;
private Statement stmt=null;

private void openDB() throws Exception {
	Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
	Connection db=DriverManager.getConnection("jdbc:odbc:sw");
	stmt=db.createStatement();
}

	public void setOptions(java.util.Hashtable ht) { }

	public void processRequest(com.rbnb.sapi.PlugInChannelMap picm)
		throws com.rbnb.sapi.SAPIException
	{
		for (int ii=0; ii<picm.NumberOfChannels(); ++ii)
		{
			int type=picm.GetType(ii);
			byte[] res=picm.GetData(ii);
			String mime=picm.GetMime(ii);
			
System.err.println("SQL: Got request, type = "+picm.GetRequestReference()
	+", "+res.length+" bytes: \""
	+new String(res)+'\"');
			
//query database - slow to keep opening connection, but quick hack for now

StringBuffer sb=new StringBuffer();
try {
if (!dbOpen) openDB();
dbOpen=true;
String query=new String(res);
boolean ok=stmt.execute(query);
if (ok) {
	ResultSet rs=stmt.getResultSet();
	while (rs.next()) {
		for (int i=1;i<=8;i++) {
			sb=sb.append(rs.getString(i));
			sb=sb.append(",");
		}
		sb=sb.append("\n");
	}
} else {
	sb=sb.append("Update count "+stmt.getUpdateCount());
}
} catch (Exception e) {
	sb=sb.append(e.getMessage());
}
res=sb.toString().getBytes();
			// Put it back:
			picm.PutTimeAuto("timeofday");
			picm.PutData(ii, res, type);
			picm.PutMime(ii,mime);
		}
	} 
	
	public boolean recycle() { return true; } // can recycle
}
