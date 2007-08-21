<%--
	StartPlugIn.jsp
	JSP script to start applications on the Web server via a web browser
		client.
	2003/04/04  ???  Modified.
	2004/05/10  WHF  Added local host verification. 
--%><%! 
	// In the absence of a security manager, prevents admin, plot, etc
	//  from killing the servlet engine on System.exit():
	static {
	com.rbnb.utility.RBNBProcess.setHardExit(false);
	}	
%><% 	
	// Validate that requesting host is local:
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
		<jsp:forward page="PlugInError.html" />
<%		return;
	}

	// Check to see if we've received arguments from the form yet:
	String[] args=request.getParameterValues("arg");
	if (args==null)
	{ // First time request, forward to form: 
		RequestDispatcher rd=request.getRequestDispatcher(
			request.getParameter("configForm"));
		rd.forward(request,response);
		return; // return immediately.
	} else {
%>
<html>
<head>
	<title>PlugIn Configuration Success</title>
	<LINK REL=STYLESHEET HREF="/stylesheets/rbnbstyles.css" TYPE="text/css">
</head>
<%@ page language="java" import="java.util.ArrayList" %>
<%@ page import="com.rbnb.utility.MainRunnable" %>
<body>
<%
	ArrayList argList=new ArrayList();
	for (int ii=0; ii<args.length; ++ii)
		if (args[ii].length()>0) argList.add(args[ii]);

System.err.println("** Starting new instance of "
	+request.getParameter("className")+':');
	try {
	new Thread(new MainRunnable(Class.forName(
		request.getParameter("className")), 
		(String[]) argList.toArray(new String[argList.size()])
		)).start();
	%>
	<h1>PlugIn Started</h1>
	<p>The <%= request.getParameter("plugInName") %> was 
		launched.</p>
<%	} catch (ClassNotFoundException cnfe) { %>
	<h1>PlugIn Launch Error</h1>
	<p>The PlugIn could not be launched: class 
		"<%= request.getParameter("className") %>" not found.</p>
<%	}  %>	
	<p class="centered"><a href=".">WebTurbine Home</a></p>
<%	}   %>				
</body>
</html>
