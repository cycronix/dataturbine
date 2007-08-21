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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.net.URLDecoder;

import javax.servlet.*;
import javax.servlet.http.*;

// 2001  WHF  Created.
// 02/15/2002  WHF  Removed V1 Compatability.
// 02/19/2002  WHF  Added duration, byFrame options.  Class needs cleaning up!
// 05/14/2002  WHF  Cleaned up debug options.  Now accepts debug=request and
//	debug=response as parameters.
// 06/21/2002  WHF  Fixed bug where Server/source form no longer works.
//  Although it had worked before, it doesn't now.  (Bug fix in server?)
//  Now uses pipe character (|) to represent first leading slash.

public class rbnbBinaryDataServlet extends HttpServlet
{
	
	public rbnbBinaryDataServlet()
	{
		
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		String timeRef=req.getParameter("reference"), 
			pathInfo=req.getPathInfo();
		double start=0, duration=0;
		boolean byFrame=false;
		int debug=0;

		{ // scope for parameter strings
		String time=req.getParameter("time"),
			dStr=req.getParameter("duration"),
			bfStr=req.getParameter("byFrame"),
			debugStr=req.getParameter("debug");
			
		if (time!=null)
		{ try { start=Double.parseDouble(time);  } 
			catch (NumberFormatException nfe) { 
			start=0; timeRef="newest"; } 
		}
		if (dStr!=null)
		{ try { duration=Double.parseDouble(dStr); }
			catch (NumberFormatException nfe) { duration=0; } 
		}
		if (bfStr!=null)
			byFrame=Boolean.valueOf(bfStr).booleanValue();
		if ("request".equals(debugStr)) debug=1;
		else if ("response".equals(debugStr)) debug=2;
		} // end parameter scope		
		if (timeRef==null) timeRef="newest";
			
		int slash=pathInfo.indexOf('/',1);
		String addr=pathInfo.substring(1,slash),  // exclusive
			chan=pathInfo.substring(slash+1),
			base, originalURI;

		chan=chan.replace('|','/');			
			
		// Effort to set the base path of the resource.  Only
		//  partly successful.
		slash=chan.indexOf('/',chan.indexOf('/')+1);
		if (slash!=-1)
			base="http://"+req.getServerName()+':'+req.getServerPort()+
				req.getContextPath()+req.getServletPath()+'/'
				+addr+'/'+chan.substring(0,slash+1);
		else
			base="http://"+req.getServerName()+':'+req.getServerPort()
				+req.getRequestURI()+'/';
		originalURI="http://"+chan.substring(chan.indexOf('/')+1);

		if (debug==1) { // debug request
			res.setContentType("text/html");
			Writer w=res.getWriter();
			w.write("<HTML><BODY><H1>Headers</H1>"
				+"<p>Path Info: "+pathInfo
				+"<p>Request URI: "+req.getRequestURI()
				+"<p>Context Path: "+req.getContextPath()
				+"<p>Request URL: "+req.getRequestURL()
				+"<p>Servlet Path: "+req.getServletPath()
				+"<p>Query String: "+req.getQueryString()
				+"<H1>Results</H1>"
				+"<p>Addr: "+addr
				+"<p>Chan:"+chan
				+"<p>Base: "+base
				+"<p>Original URI: "+originalURI
				+"</BODY></HTML>");
			w.close();  
		} else {			
			byte[] b;
			StringBuffer mimeB=new StringBuffer("");
			b=getBinaryChannelDataV2(addr,chan,start,duration,
				byFrame,timeRef,mimeB,debug);
			if (b==null) if (false) return; else 
			{ 
				// 05/16/02  WHF  This 'if' and the 'else' below commented out.
				//		There is currently no reliable way to distinguish
				//  between a web browser generated request, and an AccessPortal
				//  generated request.  In the former, the redirection is 
				//  only sometimes appropriate; in the later, it never is.
				//  Therefore, we comment this kluge until we can approach it
				//  in a more reliable fashion.
				//if (start!=0)
				{
					res.setHeader("Expires","0");
					res.setHeader("Pragma","no-cache");
					res.setContentType("text/html");
					Writer w=res.getWriter();
					w.write("<HTML><BODY onLoad=\"alert("+
						"'No data is available for channel `"
						+chan+"` at time "+start
						+".'); history.back()\"></BODY></HTML>"
						);
				}
				//else res.sendRedirect(originalURI);				
			} else
			{
				//res.setContentType("image/jpeg");
				int index=chan.lastIndexOf('.');
				String mime=(mimeB.length()>0?mimeB.toString()
					:(index>=0?getServletContext()
					.getMimeType("a"+chan.substring(index))
					:"text/plain"));
				res.setContentType(mime==null?"text/html":mime);
				res.setHeader("Expires","0");
				res.setHeader("Pragma","no-cache");
	// Should set "Age" in seconds			
				res.setHeader("Content-Base",base);
				res.setHeader("Content-Location",base);
				res.setHeader("Content-Length",String.valueOf(b.length));
				ServletOutputStream sos=res.getOutputStream();
		
				sos.write(b);
				sos.close();
			} // end if b==null
		} // end if debug 1  		
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		doGet(req,res);
	}

	public static byte[] getBinaryChannelDataV2(String addr, String channel,
		double start, double duration, boolean byFrame,
		String timeRef, StringBuffer mime, int debug)
		throws ServletException
	{
		byte result[]=null;
		com.rbnb.sapi.Sink sinkV2=new com.rbnb.sapi.Sink();
		com.rbnb.sapi.ChannelMap cm=new com.rbnb.sapi.ChannelMap();
		try {
		sinkV2.OpenRBNBConnection(addr,"rbnbBinaryDataServlet");
		cm.Add(channel); //URLDecoder.decode(channel));
		sinkV2.Request(cm,start,duration,timeRef);
			//,byFrame); 			
		
		// Get data, with ten sec timeout:
		com.rbnb.sapi.ChannelMap response=sinkV2.Fetch(10000); 		

		if (debug==2) {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			OutputStreamWriter w=new OutputStreamWriter(baos);
			mime.append("text/html");
//			res.setContentType("text/html");
//			Writer w=res.getWriter();
			w.write(
				"<HTML><BODY><H1>Request:</H1>"
				+"<p>Start: "+start
				+"<p>Duration: "+duration
				+"<p>TimeRef: "+timeRef
				+"<p>Channel: "+channel
				+"<H1>Results</H1>"
				+(response.NumberOfChannels()>0?
				("<p>Type: "+response.GetType(0)
				+"</p><p>Mime: "+response.GetMime(0)
				+"</p><p>Size: "+response.GetData(0).length
				+"</p>"):
				(response.GetIfFetchTimedOut()?"timedout":
					"no data"))
				+"</BODY></HTML>");
			w.close(); return baos.toByteArray();
		} else {		
			if (response.NumberOfChannels()>0)
			{
				result=response.GetData(0);
				if (result!=null && result.length==1) // most likely a data marker
				{
					ByteArrayOutputStream baos=new ByteArrayOutputStream();
					OutputStreamWriter w=new OutputStreamWriter(baos);
					mime.append("text/html");
/*					
					w.write("<HTML><BODY><p>No meta-data is available for"
						+" channel \"");
					w.write(channel);
					w.write("\".</p></body></html>");
*/
					w.write("<HTML><BODY onLoad=\"alert("+
						"No meta-data is available for channel \"");
					w.write(channel);
					w.write("\".'); history.back()\"></BODY></HTML>");
					
					w.close();
					return baos.toByteArray();
				}
					
				String m=response.GetMime(0);
				if (m!=null) mime.append(m);
			}
			
			if (response.GetIfFetchTimedOut()) 
				System.err.println("++ Timeout on "+channel);		
			
			sinkV2.CloseRBNBConnection();
		}
		} catch(Exception e) 
		{ 
			sinkV2.CloseRBNBConnection();
			throw new ServletException(e);
		}
		return result;
	} // end getBinaryData

}

