<%@ page import="com.rbnb.sapi.*" %>
<%
	Sink s = new Sink();
	s.OpenRBNBConnection("localhost:3333", "WebPlotChannelListSink");
try {
	String serverName = s.GetServerName() + '/';
	int slen = serverName.length();
	ChannelTree tree = com.rbnb.utility.FillInTree.buildFullTree(s);	
%>
<%--				<select name="channelSelect" id="idChannelSelect"
						onChange="channelChange('select');" > --%>
<% for (java.util.Iterator iter = tree.iterator(); iter.hasNext(); ) {
		ChannelTree.Node node = (ChannelTree.Node) iter.next();
				
		// Filter exclusions:
		String mime = node.getMime();
		if (node.getType() != ChannelTree.CHANNEL
				|| node.getFullName().indexOf("_Log/") != -1 
				|| mime != null
				&& (mime.startsWith("text/") || mime.startsWith("image/")))
			continue;
%>					<option><%= node.getFullName().substring(slen) %></option>
<% } %>			<%--	</select>  --%>
<% } finally { s.CloseRBNBConnection(); } %>
