<html>
	<head>
		<title>RBNB Web Interface</title>
		<LINK REL=STYLESHEET HREF="/stylesheets/rbnbstyles.css" TYPE="text/css">
	</head>
	<body class="header">
		<table width="100%" ><tr>
			<th align="left">RBNB Web Interface: Local Server =
<%-- <jsp:include page="/rbnbUser/info/serverName" flush="true" /> --%> 
<% // Why the below works, while the above does not, is a mystery.
	out.flush();
	ServletContext sc=getServletContext().getContext("/rbnbUser");
//	System.err.println("Servlet Context obtained: "+sc);
	RequestDispatcher rd=sc.getRequestDispatcher("/info/serverName");
//	System.err.println("RequestDispatcher obtained: "+rd);
	try {
	rd.include(request, response);
	} catch (Exception e) { /*e.printStackTrace();  throw e; */
%>
	No local server available.
<%	
	}
%>				
			</th>
			<td align="right"><img src="WhirlingLogo.gif" /> <!-- 
				<a href="." target="_top">Home</a> &middot;
				<a href="logoff.jsp" target="_top">Log Out</a> &middot;
				<a href="help.html" target="ohm_main">Help</a> -->
			</td>
		</tr></table> 
	</body>
</html>
