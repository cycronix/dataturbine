<%@ page 
	language="java" 
	import="com.rbnb.sapi.*, java.util.ArrayList, org.xml.sax.*"
	isELIgnored="false"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%!
	/* ViewRoutingPI.jsp.  Servlet to view current Robust Routing connections.
	
		2005/10/25  WHF  Created.
		2005/12/15  WHF  Made modifications as per MJM.
	*/
	
	/**
	  * Bean which stores information for each Routing PlugIn found.
	  */
	public static class PluginEntry extends org.xml.sax.helpers.DefaultHandler
	{
		public void parse(XMLReader xmlReader) throws java.io.IOException,
				SAXException
		{
			xmlReader.setContentHandler(this);
			xmlReader.setErrorHandler(this);
			xmlReader.parse(new org.xml.sax.InputSource(
					new java.io.StringReader(info)));
		}
		
//*******************************  Accessors  *******************************//
		public String getName() { return name; }
		public String getLocalAddr() { return localAddr; }
		public String getServerAddr() { return serverAddr; }
		public String getServerName() { return serverName; }
		public String getInfo() { return info; }
		public long getBytes() { return bytes; }
		public long getRate() { return rate; }
		public int getInterval() { return interval; }
		
		// ContentHandler interface:
		public void startElement(String uri, String localName,
				String qName, Attributes attributes)
		{
			sbuffer.setLength(0);
		}
		
		public void characters(char[] ch, int start, int length)
		{
			sbuffer.append(ch, start, length);
		}
		
		public void endElement(String uri, String localName, String qName)
		{
			if ("pluginaddress".equals(qName)) {
				localAddr = sbuffer.toString();
			} else if ("sinkaddress".equals(qName)) {
				serverAddr = sbuffer.toString();
			} else if ("sinkname".equals(qName)) {
				// Sinkname has leading and trailing slashes:
				serverName = sbuffer.substring(1, sbuffer.length()-1);
			} else if ("interval".equals(qName)) {
				try {
					interval = Integer.parseInt(sbuffer.toString());
				} catch (NumberFormatException nfe) { }
			}
		}
		
		public void error(SAXParseException e)
		{ System.err.println("Parse error: "); e.printStackTrace(); }
		
		public void warning(SAXParseException e)
		{ System.err.println("Parse warning: "); e.printStackTrace(); }
		
//*******************************  Data Members  ****************************//
		private final StringBuffer sbuffer = new StringBuffer();
		private String name, localAddr, serverAddr, serverName, info;
		private long bytes, rate;
		private int interval;
	} // end class PlugInEntry
	
	private static int repeat;
	
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN" 
		"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>RBNB Routing Console</title>
	<LINK REL=STYLESHEET HREF="/stylesheets/rbnbstyles.css" TYPE="text/css">
</head>
<body style="margin: 0.1in;">
	<div class="routing">
	<h2>RBNB DataTurbine</h2>
	<h1>Routing Console</h1>
<%
	// Set no-cache:
	response.addHeader("Pragma", "No-cache");
	response.addHeader("Cache-Control", "no-cache");
	response.addDateHeader("Expires", -1);

	final ArrayList plugIns = new ArrayList(); 
	final Sink sink = new Sink();
	final ChannelMap cmap = new ChannelMap();
	final XMLReader xmlReader 
			= org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
	String parentAddr = request.getParameter("parent");
	if (parentAddr == null) parentAddr = "localhost:3333";
	String action = request.getParameter("theAction");
	String repeatStr = request.getParameter("repeat");
	int repeatVal;

	if (repeatStr != null) {
		repeatVal = Integer.parseInt(repeatStr);
		if (repeatVal > repeat) repeat = repeatVal;
		else action = "refresh";
	}

	pageContext.setAttribute("plugIns", plugIns);
	pageContext.setAttribute("action", action);
%>
	<!-- Action = ${action} -->
	<c:if test="${action == 'start' || action == 'loopback'}">
		<% // Validate that requesting host is local:
		java.net.InetAddress requestAddr 
				= java.net.InetAddress.getByName(request.getRemoteAddr());
		java.net.InetAddress[] addrs = java.net.InetAddress.getAllByName(
				request.getServerName());
		boolean ok = false;
		for (int ii = 0; ii < addrs.length; ++ii) {
			if (addrs[ii].equals(requestAddr)) {
				ok = true;
				break;
			}
		}
		if (!ok) { %>
			<jsp:include page="PlugInError.html" />
	<%	} else { %>
			<%-- We comment the include so it is not displayed. --%> 
			<!-- <jsp:include page="StartPlugIn.jsp" /> -->
		<% 	Thread.sleep(1000); 
		} %>
	</c:if>
<%
	try {
	sink.OpenRBNBConnection(parentAddr, "RoutingConsoleSink");
	// Because it lies in a different web application, we cannot 'include'
	//  from the RBNB WebDAVServlet.  So, just make a standard connection.
	if ("stop".equals(action)) {
		String toStop = request.getParameter("toStop");
		cmap.PutDataAsString(
				cmap.Add(toStop+'/'+toStop),
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
				+"<!DOCTYPE route>"
				+"<route>"
				+"<id>"+toStop+"</id>"
				+"<command>terminate</command>"
				+"</route>"
		);
		sink.Request(cmap, 0, 0, "newest");
		sink.Fetch(10000, cmap);
		Thread.sleep(1000);
	}

	sink.RequestRegistration();
	sink.Fetch(10000, cmap);

	if (cmap.GetIfFetchTimedOut()) {
		response.sendError(response.SC_GATEWAY_TIMEOUT,
				"Registration request timed out.");
		return;
	}
	ChannelTree ctree = ChannelTree.createFromChannelMap(cmap);
	for (java.util.Iterator iter = ctree.rootIterator(); iter.hasNext(); ) {
		ChannelTree.Node node = (ChannelTree.Node) iter.next();
		if (node.getType() == ChannelTree.PLUGIN) { // might be a router
			ChannelTree.Node metrics = ctree.findNode("_Route."+node.getName());
			if (metrics != null) {
				PluginEntry pie = new PluginEntry();
				pie.name = node.getName();
				cmap.Clear();
				// These names are hard coded into 
				//   com.rbnb.utility.MetricsHandler.
				cmap.Add(metrics.getName()+"/DataBytes");
				cmap.Add(metrics.getName()+"/DataRate");
				cmap.Add(metrics.getName()+"/Info");
				sink.Request(cmap, 0, 0, "newest");
				sink.Fetch(10000, cmap);
				if (cmap.GetIfFetchTimedOut()) {
					response.sendError(response.SC_GATEWAY_TIMEOUT,
							"Metrics request timed out.");
					return;
				}
				try {
					int dbI = cmap.GetIndex(metrics.getName()+"/DataBytes"),
						drI = cmap.GetIndex(metrics.getName()+"/DataRate"),
						iI  = cmap.GetIndex(metrics.getName()+"/Info");
					pie.bytes = dbI == -1?-1:cmap.GetDataAsInt64(dbI)[0];
					pie.rate  = drI == -1?-1:cmap.GetDataAsInt64(drI)[0];
					if (iI == -1) pie.serverAddr = "unavailable";
					else {
						pie.info  = cmap.GetDataAsString(iI)[0];
						pie.parse(xmlReader);
					}
				} catch (ClassCastException cce) {
					// The user may have refreshed before the information
					// is available.  Ignore.
				}
				plugIns.add(pie);
			}
		}
	}
	} finally {
		sink.CloseRBNBConnection();
	}			
				
%>
<form name="theForm" action="ViewRoutingPI.jsp" method="POST">
	<input type="hidden" name="className" value="RoutingPlugIn" />
	<input type="hidden" name="plugInName" value="Routing PlugIn" />
	<input type="hidden" name="theAction" value="" />
	<input type="hidden" name="toStop" value="" />
	<input type="hidden" name="repeat" value="<%= repeat+1 %>" />
	<input type="hidden" name="arg" value="-a" />
	<input type="hidden" name="arg" value="<%= parentAddr %>" />
	
	<p><table class="routing" border="1">
		<tr><td class="routing"><span style="font-weight:bold;">
			Parent Address<sup>1</sup>:</span>
			<input type="TEXT" name="parent" value="<%= parentAddr %>"
					size="24" />
		</td></tr>
	</table></p>
	
	<p class="centered"><input type="SUBMIT" value="    Refresh    " 
			onClick="theForm.theAction.value='refresh'; return true;" /></p>
			
	<p><table class="routing" border="1">
		<tr>
			<th class="routing">Start / Stop</th>
			<th class="routing">Child Address<sup>2</sup></th>
			<th class="routing">Route Name<sup>3</sup></th>
			<th class="routing">Transferred
					<span class="units">(Bytes)</span></th>
			<th class="routing">Bandwidth
					<span class="units">(Bytes/Sec)</span></th>
		</tr>
		<c:forEach var="pi" items="${plugIns}" >
			<tr>
				<td><input type="SUBMIT" value="Stop" onClick=
						"theForm.theAction.value='stop';
						theForm.toStop.value='${pi.name}';
						return true;" />
				</td>
				<td>${pi.serverAddr}</td>
				<td>${pi.name}</td>
				<td>${pi.bytes}</td>
				<td>${pi.rate}</td>
			</tr>
		</c:forEach>
		<tr>
			<td style="background-color: rgb(255, 204, 153);">
				<input type="SUBMIT" value="Start" onClick=
					"theForm.theAction.value='start'; return true;" />
			</td>
			<td style="background-color: rgb(255, 204, 153);">
				<input type="hidden" name="arg" value="-b"/>
				<input type="TEXT" name="arg" value="localhost:3333" size="20"/>
			</td>
			<td style="background-color: rgb(255, 204, 153);">
				<input type="hidden" name="arg" value="-n"/>
				<input type="TEXT" name="arg" value="" size="20"/>
			</td>
			<td style="background-color: rgb(255, 204, 153);">---</td>
			<td style="background-color: rgb(255, 204, 153);">---</td>
		</tr>
	</table></p>
	
	<p class="centered"><input type="SUBMIT" value="Loopback" onClick=
			"theForm.theAction.value='loopback';
			theForm.arg[5].value='_loopback'; return true;" />
	</p>
	</form>
	</div>
	<%@ include file="RoutingPIFooter.html" %>
</body>
</html>