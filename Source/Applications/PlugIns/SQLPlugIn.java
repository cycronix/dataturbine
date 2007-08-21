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


// SQLPlugIn - connects to specified database, receives SQL queries, formats response as comma
//             separated values
// EMF
// April 2003
// Copyright 2003 Creare Incorporated
//
// JPW 09/28/2006: In the data String associated with the request channel: First check for a query string
//                 in either a "msg" or "message" munge.  If no such munge is present in the String then
//                 assume that the entire String is the query.  Also, URL decode the query String before
//                 using it.

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.PlugIn;
import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.KeyValueHash;
import com.rbnb.utility.RBNBProcess;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class SQLPlugIn {
	private String rbnbServer="localhost:3333";
	private String jdbcDriver="sun.jdbc.odbc.JdbcOdbcDriver";
	private String database=null;
	private String odbcPrepend="jdbc:odbc:";
	private String plugInName=null;
	private PlugIn plugin=null; //plugin connection

	public SQLPlugIn(String[] args) {
	//parse args
		try {
			ArgHandler ah=new ArgHandler(args);
			if (ah.checkFlag('h')) {
				throw new Exception("");
			}
			if (ah.checkFlag('a')) {
				rbnbServer=ah.getOption('a');
				if (rbnbServer==null) throw new Exception("Must specify rbnb server with -a");
			}
			if (ah.checkFlag('j')) {
				jdbcDriver=ah.getOption('j');
				odbcPrepend="";
				if (jdbcDriver==null) throw new Exception("Must specify JDBC driver with -j");
			}
			if (ah.checkFlag('d')) {
				database=ah.getOption('d');
				plugInName=database.substring(database.lastIndexOf('/')+1);
				if (database==null) throw new Exception("Must specify database name with -d");
			} else throw new Exception("Must specify database name with -d");
			if (ah.checkFlag('n')) {
				plugInName=ah.getOption('n');
				if (plugInName==null) throw new Exception("Must specify plugin name with -n");
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.err.println("Usage:  java SQLPlugIn");
			System.err.println("\t-h\tprint this usage guide");
			System.err.println("\t-j\t<jdbc driver>\tdefault sun.jdbc.odbc.JdbcOdbcDriver");
			System.err.println("\t-a\t<rbnb server host:port>\tdefault localhost:3333");
			System.err.println("\t-d\t<database name>\trequired option, no default");
			System.err.println("\t-n\t<plugin name>\tdefault SQLPlugIn");
			RBNBProcess.exit(0);
		}
	} //end constructor

	public static void main(String[] args) {
		(new SQLPlugIn(args)).exec();
	} //end method main

	// loop handling SQL requests, formatting and returning responses
	public void exec() {
		try {
			//make rbnb connection
			plugin=new PlugIn();
			plugin.OpenRBNBConnection(rbnbServer,plugInName);

			//register channel, so will never receive registration requests
			ChannelMap cm=new ChannelMap();
			cm.Add("text");
			cm.Add("html");
			cm.Add("binary");
			plugin.Register(cm);
			
			//make database connection
			Class.forName(jdbcDriver);
			Connection db=DriverManager.getConnection(odbcPrepend+database);
			Statement stmt=db.createStatement();

			//loop handling requests  - note ignores start, duration, reference, using only
			//                          contents of request (message)
			while (true) {
				PlugInChannelMap picm=plugin.Fetch(-1); //block until request arrives
				for (int i=0;i<picm.NumberOfChannels();i++) {
					picm.PutTime((double)System.currentTimeMillis(),0);
					//extract SQL statement
					if (picm.GetType(i)==ChannelMap.TYPE_STRING) {
						if (picm.GetName(i).equals("text")) {
						    	// JPW 09/28/2006: Extract the query string out of the
							//                 "msg" or "message" munge string
							String mungeStr=picm.GetDataAsString(0)[0];
							String query=null;
							char[] terminatorChars = {'&'};
							KeyValueHash kvh =
							    new KeyValueHash(mungeStr,terminatorChars);
							if (kvh.get("message") != null) {
							    query = kvh.get("message");
							} else if (kvh.get("msg") != null) {
							    query = kvh.get("msg");
							} else {
							    // If no msg munge, then assume that the entire String is the query
							    query = mungeStr;
							}
							// JPW 09/28/2006: Decode the query String
							// System.err.println("query before decode: \"" + query + "\"");
							try {
							    query = URLDecoder.decode(query,"UTF-8");
							} catch (UnsupportedEncodingException uee) {
							    // Can't decode
							    System.err.println("ERROR: Can't decode the query string \"" + query + "\"");
							    query = null;
							}
							// System.err.println("query after decode: \"" + query + "\"");
							if (query == null) {
							    System.err.println("No query for channel " + picm.GetName(i));
							    picm.PutMime(0,"text/plain");
							    picm.PutDataAsString(0,"No query was presented");
							    continue;
							}
							
							System.err.println("Time: " + System.currentTimeMillis() + "  Channel: " + picm.GetName(i) + "  Query: " + query);
							
							String response=null;
							try {
								boolean ok=stmt.execute(query);
								if (ok) { //got result set
									ResultSet rs=stmt.getResultSet();
									response=rsToString(rs);
								} else { //got update count
									int updateCount=stmt.getUpdateCount();
									response="Update count "+updateCount;
								}
							} catch (Exception e) {
								response=e.getMessage();
							}
							picm.PutMime(0,"text/plain");
							picm.PutDataAsString(0,response);
						} else if (picm.GetName(i).equals("html")) {
							picm.PutMime(i,"text/plain");
							picm.PutDataAsString(i,"html formatting not implemented, request text");
						} else if (picm.GetName(i).equals("binary")) {
							picm.PutMime(i,"text/plain");
							picm.PutDataAsString(i,"binary formatting not implemented, request text");
						}
					}
				}
				plugin.Flush(picm);
			}

		} catch (Exception e) {
			System.err.println(plugInName+" exception.  Aborting.");
			e.printStackTrace();
		}
  }//end exec method
  
  //rsToString method - formats result set as string in comma separated value table
  private String rsToString(ResultSet rs) {
	  // Note that java.sql.ResultSet diligently converts binary fields to strings, which can be
	  // problematic when they are large (e.g. images).  
	  StringBuffer response=new StringBuffer();
	  try {
		  ResultSetMetaData rsmd=rs.getMetaData();
		  int col=rsmd.getColumnCount();
		  for (int i=1;i<=col;i++) {
			  response=response.append(rsmd.getColumnName(i)).append(',');
			  //System.err.println(rsmd.getColumnTypeName(i));
		  }
		  response=response.append("\n");
		  while (rs.next()) {
			  for (int i=1;i<=col;i++) {
				  String item=rs.getString(i);
				  if (item==null) {
					  response=response.append(item).append(",");
				  } else { //check for and deal with special characters
					  if (item.indexOf('"')>=0) {
						  //repeat double quotes
						  StringBuffer sb=new StringBuffer();
						  int idx1=0;
						  int idx2=0;
						  while ((idx2=item.indexOf('"',idx1))>=0) {
							  sb=sb.append(item.substring(idx1,idx2+1)).append('"');
							  idx1=idx2+1;
						  }
						  sb=sb.append(item.substring(idx1));
						  item=sb.toString();
					  }
					  if (item.indexOf(',')>=0 || item.indexOf('\n')>=0 || item.indexOf('"')>=0) {
						  //encapsulate in double quotes if contains comma or newline
						  response=response.append('"').append(item).append('"').append(',');
					  } else {
						  response=response.append(item).append(',');
					  }
				  }
			  }
			  response=response.append("\n");
		  }
	  } catch (Exception e) {
		  response=new StringBuffer();
		  response=response.append("Exception parsing ResultSet: ");
		  response=response.append(e.getMessage());
		  e.printStackTrace();
	  }
	  return (response.toString());
  }

}//end SQLPlugIn class

