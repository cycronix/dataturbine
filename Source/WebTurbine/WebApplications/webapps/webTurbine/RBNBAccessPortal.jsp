<HTML>
<jsp:useBean id="view" class="webTurbine.Viewer">
	<jsp:setProperty name="view" property="*" />
</jsp:useBean>

<HEAD>
	<title>RBNB Web Access Portal</title>
	<LINK REL=STYLESHEET HREF="/stylesheets/rbnbstyles.css" TYPE="text/css">
	
	<script language="javascript">
	<% /* 
	   Lessons learned in this javascript:
	   1) Netscape 4.74 does not support global named form references.
	   2) Netscape 4.74 does not support the "Select.value" shortcut
	   		for "Select.options[Select.selectedIndex].value".
	   3) Netscape 4.74 does not allow ':' in URLs other than the hostname:port.
	   4) IE 5.0 does not transmit the value of disabled form items. 
	   5) Do not use subscripting [] with strings.  This is not C++!
	*/ %>
	<!--
	function buildURL(channel)
	{
		inputFormRef=document.inputForm;
		newURL="rbnbServlet/";
		newURL=newURL.concat(inputFormRef.address.value).concat("/");
		newURL=newURL.concat(inputFormRef.convertOther.value);
		if (newURL.charAt(newURL.length-1)!='/')
			newURL=newURL.concat("/");
		newURL=newURL.concat(channel)
			.concat("?time=").concat(inputFormRef.time.value)
			.concat("&duration=").concat(inputFormRef.duration.value);
//			.concat("&byFrame=").concat(inputFormRef.byFrame.value);
		if (inputFormRef.metaData.value=="true")
			newURL=newURL.concat("&reference=registration");
		else
			newURL=newURL.concat("&reference=")
				.concat(inputFormRef.reference.options[inputFormRef
					.reference.selectedIndex].value);
		newURL=newURL.replace(/:/,"%3A");
//alert(newURL);
		window.open(newURL,"_blank",
			"scrollbars=yes,location=yes");
		return false; // don't follow original link
	}
	
	function expandList(listID,toExpand)
	{
		inputFormRef=document.inputForm;
		inputFormRef.listID.value=listID;		
		inputFormRef.toExpand.value=toExpand;		
/*		
		oldAct=inputFormRef.action;
		dest="RBNBAccessPortal.jsp#";
		dest=dest.concat(toExpand);
		inputFormRef.action=dest;
*/
		inputFormRef.submit();
//		inputFormRef.action=oldAct;
		return false;
	}

	function replaceChannel(toReplace,replaceWith)
	{
		inputFormRef=document.inputForm;
		currChan=inputFormRef.channels.value;
		if (toReplace.length==0||currChan.search(toReplace)!=0)
			inputFormRef.channels.value=replaceWith.concat('/')
				.concat(currChan);
		else
		{
			toReplace="^".concat(toReplace);
			inputFormRef.channels.value=currChan.replace(
				toReplace,replaceWith);
		}
		inputFormRef.submit();
		return false;
	}
	
	function doMeta(s)
	{
		inputFormRef=document.inputForm;
		inputFormRef.metaData.value=s;
		if (s=="true")
		{
			inputFormRef.time.disabled=true;
			inputFormRef.duration.disabled=true;
			inputFormRef.reference.disabled=true;
			// inputFormRef.byFrameCheck.disabled=true;
		}
		else
		{
			inputFormRef.time.disabled=false;
			inputFormRef.duration.disabled=false;
			inputFormRef.reference.disabled=false;
			// inputFormRef.byFrameCheck.disabled=false;
		}
	}
	
	function onFormatChange()
	{
		inputFormRef=document.inputForm;
		s=inputFormRef.convert.options[inputFormRef.convert.selectedIndex]
			.value;
		if (s=='other')
		{
			inputFormRef.convertOther.disabled=false;
			inputFormRef.convertOther.focus();
		}
		else 
		{
			inputFormRef.convertOther.value=s;
			inputFormRef.convertOther.disabled=true;
		}
	}
/*	
	function doByFrame()
	{
		inputFormRef=document.inputForm;
		if (inputFormRef.byFrameCheck.checked)
			inputFormRef.byFrame.value="true";
		else
			inputFormRef.byFrame.value="false";
	}
*/	
	function bodyLoad()
	{
		if ("<%= view.getToExpand() %>".length>0)
			window.location.hash="<%= view.getToExpand() %>";
		inputFormRef=document.inputForm;
		doMeta("<%= view.getMetaData() %>");
		for (ii=0; ii<inputFormRef.reference.length; ++ii)
			if (inputFormRef.reference.options[ii].value==
				"<%= view.getReference() %>")
			{
				inputFormRef.reference.selectedIndex=ii;
				break;
			}
		if (inputFormRef.elements[inputFormRef.length-2].name=="convert")
		{
			for (ii=0; ii<inputFormRef.convert.length; ++ii)
				if (inputFormRef.convert.options[ii].value==
					"<%= view.getConvert() %>")
				{
					inputFormRef.convert.selectedIndex=ii;
					onFormatChange();
					break;
				}
		}
	}
	-->
	</script>

</HEAD>
<%@ page language="java" 
import="webTurbine.*, java.util.LinkedList, java.util.Iterator, utility.WebUtil" 
%>
<%!
	private final static int FIELDSIZE=30;

	// Recursive function to generate list of links.
	private static void addList(LinkedList list, JspWriter out, Viewer view,
		String listID) 
	throws java.io.IOException
	{
		if (list.isEmpty()) return;
		Iterator iter=list.iterator();
		out.println("<ol>");
		while (iter.hasNext()) 
		{
			ViewEntity next=(ViewEntity) iter.next();
			out.print("<li>");
			if (!next.subList.isEmpty()||next.isParent()||next.isChild())
			{
				out.println("<a name=\""+next+"\" href=\"\" ");
				String folderImage;
				if (next.isParent())
				{
					folderImage=//"FolderParent.gif";
						"Parent.gif";
					out.println("onClick=\"return replaceChannel('"
						+view.getCurrentServer()+"','"+next.str+"')\" >");
				}
				else if (next.isChild())
				{
					folderImage=//"FolderChild.gif";
						"Source.gif";
					out.println("onClick=\"return replaceChannel('"
						+view.getCurrentServer()+"','"+next.str+"')\" >");
				}
				else 
				{
					folderImage=(next.isExpandSubList()?"FolderOpen.gif":
						"FolderClosed.gif");
					out.println("onClick=\"return expandList('"
						+listID+"','"+next+"')\" >");
				}
				out.println("<img align=bottom border=0 src=\""
					+folderImage+"\" /></a>&nbsp; ");
			}
			if (next.link!=null)
			{
				String chan=utility.WebUtil.replace(
					"|"+view.getCurrentServer().substring(1)
					+'/'+next.link,' ',"%20");
				out.println(
					"\t<A href=\"\" onClick=\"return buildURL('"+chan
						+"')\" >\n\t\t"+next.str+"</A></LI>"
				);
			}
			else
				out.println("\t"+next.str+"</li>");
			if (next.subList!=null && next.isExpandSubList())
				addList(next.subList,out,view,listID);
		}
		out.println("</ol>");
	}
%>

<body onLoad="bodyLoad()" style="margin:0in;" >
	<h1>RBNB Web Access Portal</h1>
	<table valign="top"><tr>
		<td valign="top" style="width=1in;">
			<center><div class="form">
		<form method=POST name="inputForm" action="RBNBAccessPortal.jsp">
		<p style="border: solid rgb(128,128,255); text-align:center;
			background-image: url(c_bg.gif);" >
			<table valign="top" cellspacing=20 align="center"><tr>
				<td><table>
					<tr><th colspan=2>Server/Channel Selection</th></tr>
					<tr>
						<td>RBNB Address</td>
						<td><INPUT type=TEXT name="address" 
								size=<%= FIELDSIZE %>
								value='<%= view.getAddress() %>' />
							<input type="hidden" name="listID" value="" />
							<input type="hidden" name="toExpand" value="" />
						</td>
					</tr>
					<tr>
						<td>Channel Match:</td>
						<td><input type="text" value="<%= view.getChannels() %>" 
							name="channels" size=<%= FIELDSIZE %> /></td>
					</tr>
					<tr>
						<td>Keyword Match:</td>
						<td><input type="text" value="<%= view.getKeywords() %>"
							name="keywords" size=<%= FIELDSIZE %> /></td>
					</tr>
					<tr><td colspan=2><center><INPUT value="Submit / Refresh"
						type=SUBMIT /></center></td>
					<tr><td colspan=2>&nbsp; </td></tr>
					<tr><td colspan=2><center>
							<a href="/documentation/ViewChannelHelp.html" 
								target=_top>Help</a>
							&nbsp; &middot; &nbsp; 
							<a href="/webTurbine" target=_top>Home</a></center>
					</td></tr>
					</tr>
				</table></td></tr>
				<tr><td><table class="leftjustified">
					<tr><th colspan=3><center>Request Options</center></th></tr>
					<tr>
						<td><input type="radio" name="metaRadio" 
							<%= "true".equals(view.getMetaData())?"checked"
								:"" %> 
							onClick="doMeta('true')" /></td>
						<td colspan=2>Meta Data</td>
					</tr><tr>
						<td><input type="radio" name="metaRadio" 
								<%= "true".equals(view.getMetaData())?""
									:"checked" %> 
								onClick="doMeta('false')" />
							<input type="hidden" name="metaData" 
								value="<%= view.getMetaData() %>" /></td>
						<td colspan=2>Source Data</td>
					</tr><tr>
						<td></td>
						<td>Start:</td>
						<td><INPUT type=text size=10 name="time" 
							value="<%= view.getTime() %>"/></td>
					</tr><tr>
						<td></td>
						<td>Duration:</td>
						<td><INPUT type=text size=10 name="duration" 
							value="<%= view.getDuration() %>"/>
						</td>
					</tr><tr>
						<td></td>
						<td>Reference:</td>
						<td><SELECT name="reference">
							<OPTION value="newest">Newest</OPTION>
							<OPTION value="oldest">Oldest</OPTION>
							<OPTION value="absolute">Absolute</OPTION>
<!--			These options are of insignificant utility on this form,
					so are removed for clarity.
							<OPTION value="modified">Modified</OPTION>
							<OPTION value="after">After</OPTION> -->
						</SELECT><input name="byFrame" type="hidden" 
								value="false" /></td>
					</tr>
					</table></td></tr></table>
					Via PlugIn:
						<select name="convert" onChange="onFormatChange()" >
							<option selected value="">Raw data</option>
							<option value="PNGPlugIn/">Portable Network Graphic
								</option>
							<option value="other">Other -></option>
						</select>
						<input name="convertOther" 
							value="<%= view.getConvertOther() %>" size=30
							disabled=true />					
		</p>
		</form></div></center></td>
		<td valign="top" style="width=75%; vertical-align: top;" >
<%
	if (view.processRequest(request))
	{
		LinkedList list = view.getChannelList();
		if (list.size()>0)
		{   %>
		<p>
<%
			//  Use definition list
			// formatting to get alignment with other lists without numbers.
			out.println("<dl><dt></dt><dd><b>"
				+view.getCurrentServer()+"</b></dd></dl>");

 			addList(list,out,view,view.getListID()); %>
		</p>
<%		} else
			out.write("<p>Error: "+view.getErrorString()+"</p>");		
	} else {
%>
		<h1>Please enter server address above.</h1>
<%	} %>
		</p></td>
	</tr></table>
</body>
</html>

