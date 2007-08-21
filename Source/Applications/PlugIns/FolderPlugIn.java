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

/**
 * File folder publishing plugin application for V2 <bold>RBNBs</bold>.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 05/12/2003
 */

/*
 * Copyright 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 05/12/2003  INB	Ensure that the server answers registration requests
 *			as much as possible. If we still get one in this
 *			plugin, answer it with a blank response.
 * 03/10/2003  INB	Use new <code>Directory</code> code class.
 * 01/21/2002  INB	Created.
 *
 */
public class FolderPlugIn
    extends java.lang.Thread
{
    /**
     * debugging level.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/23/2002
     */
    private int debugLevel = 0;

    /**
     * <code>FileNameMap</code> for getting file types.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/15/2002
     */
    private java.net.FileNameMap fnMap = null;

    /**
     * the plugin's name.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/15/2002
     */
    private String pluginName = "Folder";

    /**
     * the address of the <bold>RBNB</bold>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/21/2002
     */
    private String serverAddress = "localhost:3333";

    /**
     * the name of the folder to attach to.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/21/2002
     */
    private String folderName = ".";

    /**
     * the period between updates of the folder.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/21/2002
     */
    private double period = 1.0;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/21/2002  INB	Created.
     *
     */
    private FolderPlugIn() {
	super();
    }

    /**
     * Calculates the MIME type (if any) for a file.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name of the file.
     * @return the MIME type or null.
     * @since V2.0
     * @version 03/18/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/18/2003  EMF  Made jdk 1.1 compatible.  URLConnection.
     *                  getFileNameMap returns null map, so need
     *                  to look at file extension and guess.
     * 01/25/2002  INB	Created.
     *
     */
    private final String calculateMIMEType(String nameI) {
	int dot = nameI.lastIndexOf(".");
	String extension = ((dot >= 0) ?
			    nameI.substring(dot + 1) :
			    null),
	    mimeTypeR = null;

	if (extension != null) {
	    if (fnMap == null) {
		fnMap = java.net.URLConnection.getFileNameMap();
	    }
            if (fnMap == null) { //jdk1.1 may return null
	    	if (extension.equalsIgnoreCase("txt")) {
			mimeTypeR = "text/plain";
	    	} else if (extension.equalsIgnoreCase("htm") ||
		       	extension.equalsIgnoreCase("html")) {
			mimeTypeR = "text/html";
	    	} else if (extension.equalsIgnoreCase("xml")) {
			mimeTypeR = "text/xml";
	    	} else if (extension.equalsIgnoreCase("jpg") ||
		       	extension.equalsIgnoreCase("jpeg")) {
			mimeTypeR = "image/jpeg";
	    	} else if (extension.equalsIgnoreCase("gif")) {
			mimeTypeR = "image/gif";
	    	} else if (extension.equalsIgnoreCase("tiff")) {
			mimeTypeR = "image/tiff";
	    	}
	    } else {
	    	mimeTypeR = fnMap.getContentTypeFor(nameI);
	    }
	}

	if (mimeTypeR == null) {
	    mimeTypeR = "text/plain";
	}

	return (mimeTypeR);
    }

    /**
     * Expands the input folder into an <code>Rmap</code> hierarchy for
     * registration. 
     * <p>
     *
     * @author Ian Brown
     *
     * @param folderI       the folder.
     * @param registrationI the point in the registration <code>Rmap</code>
     *			    hierarchy to add the folder's contents.
     * @param addChildI	    add a child?
     * @exception java.lang.Exception
     *		  thrown if there is a problem.
     * @since V2.0
     * @version 03/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/10/2003  INB	Use new <code>Directory</code> class.
     * 01/21/2002  INB	Created.
     *
     */
    private final void expandFolder(java.io.File folderI,
				    com.rbnb.api.Rmap registrationI,
				    boolean addChildI)
	throws java.lang.Exception
    {
	com.rbnb.api.Rmap registration;
	if (addChildI) {
	    registration = new com.rbnb.api.Rmap(folderI.getName());
	    registrationI.addChild(registration);
	} else {
	    registration = registrationI;
	}

	if (!folderI.isDirectory()) {
	    /*
	    registration.setDblock(new com.rbnb.api.DataBlock(new byte[1],
							      1,
							      1));
	    */
	    /*
	    String metaData =
		"Content-Type: " +
		calculateMIMEType(folderI.getName()) + "\n" +
		"Content-Length: " + folderI.length() + "\n" +
		"Last-Modified: " +
		(new java.text.SimpleDateFormat
		    ("EEE dd-MMM-yyyy HH:mm:ss.sss zzz")).format
		(new java.util.Date(folderI.lastModified()));
	    */
	    String metaData = com.rbnb.api.Rmap.xmlRegistration
		(folderI.length(),
		 calculateMIMEType(folderI.getName()));
	    registration.setTrange
		(new com.rbnb.api.TimeRange(folderI.lastModified()/1000.,
					    0.));
	    registration.setDblock
		(new com.rbnb.api.DataBlock(metaData,
					    1,
					    metaData.length(),
					    com.rbnb.api.DataBlock.TYPE_STRING,
					    com.rbnb.api.DataBlock.ORDER_MSB,
					    false,
					    0,
					    metaData.length()));
	    registration.getDblock().setMIMEType("text/xml");
				   
	} else {
	    com.rbnb.api.Directory directory =
		new com.rbnb.api.Directory(folderI);
	    java.io.File[] files = directory.listFiles();

	    for (int idx = 0; (files != null) && (idx < files.length); ++idx) {
		expandFolder(files[idx],registration,true);
	    }
	}
    }

    /**
     * Converts the input folder <code>File</code> to an <code>Rmap</code>
     * hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @param folderI the folder.
     * @return the <code>Rmap</code>.
     * @exception java.lang.Exception
     *		  thrown if there is a problem.
     * @since V2.0
     * @version 01/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/21/2002  INB	Created.
     *
     */
    private final com.rbnb.api.Rmap folderToRmap(java.io.File folderI)
	throws java.lang.Exception
    {
	String path = folderI.getAbsolutePath();

	if (folderI.separator.compareTo("/") != 0) {
	    int idx,
		idx1;
	    String oPath = path;
	    path = "";
	    for (idx = 0,
		     idx1 = oPath.indexOf(folderI.separator,idx);
		 idx < oPath.length();
		 idx = idx1 + 1,
		     idx1 = oPath.indexOf(folderI.separator,idx)) {
		if (idx1 == -1) {
		    break;
		}
		path += oPath.substring(idx,idx1) + "/";
	    }
	    if (idx < oPath.length()) {
		path += oPath.substring(idx);
	    }
	}

	if (path.indexOf("/.") != -1) {
	    path = path.substring(0,path.indexOf("/."));
	}
	if (path.charAt(0) != '/') {
	    path = "/" + path;
	}
	com.rbnb.api.Rmap rmapR = com.rbnb.api.Rmap.createFromName(path);

	return (rmapR);
    }

    /**
     * Gets the debugging level.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the debugging level.
     * @see #setDebugLevel(int)
     * @since V2.0
     * @version 01/23/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2002  INB	Created.
     *
     */
    private final int getDebugLevel() {
	return (debugLevel);
    }

    /**
     * Gets the folder name.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the folder name.
     * @see #setFolderName(String)
     * @since V2.0
     * @version 01/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/21/2002  INB	Created.
     *
     */
    private final String getFolderName() {
	return (folderName);
    }

    /**
     * Gets the period between updates of the folder.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the period.
     * @see #setPeriod(double)
     * @since V2.0
     * @version 01/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/21/2002  INB	Created.
     *
     */
    private final double getPeriod() {
	return (period);
    }

    /**
     * Gets the plugin name.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the plugin name.
     * @see #setPlugInNameString)
     * @since V2.0
     * @version 05/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/15/2002  INB	Created.
     *
     */
    private final String getPluginName() {
	return (pluginName);
    }

    /**
     * Gets the server address.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the server address.
     * @see #setServerAddress(String)
     * @since V2.0
     * @version 01/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/21/2002  INB	Created.
     *
     */
    private final String getServerAddress() {
	return (serverAddress);
    }

    /**
     * Handles a request.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI the request <code>DataRequest</code>.
     * @param dataI    get data?
     * @return the response <code>Rmap</code>.
     * @exception java.lang.Exception
     *		  thrown if there is a problem.
     * @since V2.0
     * @version 05/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/12/2003  INB	Instead of retrieving the registration, simply
     *			reply with an empty registration map.
     * 01/21/2002  INB	Created.
     *
     */
    private final com.rbnb.api.Rmap handleRequest
	(com.rbnb.api.DataRequest requestI,
	 boolean dataI)
	throws java.lang.Exception
    {
	com.rbnb.api.Rmap responseR =
	    new com.rbnb.api.Rmap(requestI.getName()),
	    response;

	if (!dataI) {
	    //response = retrieveRegistration(requestI);
	    response = new com.rbnb.api.Rmap();

	} else if (requestI.getNchildren() == 0) {
	    response = new com.rbnb.api.EndOfStream
		(com.rbnb.api.EndOfStream.REASON_NONAME);

	} else if ((requestI.getReference() == requestI.OLDEST) &&
		   (requestI.getNchildren() == 1) &&
		   (requestI.getChildAt(0).compareNames("...") == 0)) {
 	    response = new com.rbnb.api.EndOfStream();
	    java.io.File folder = new java.io.File(getFolderName());
	    com.rbnb.api.Rmap registration = /*folderToRmap(folder)*/
					     new com.rbnb.api.Rmap(),
			      bottom = registration.moveToBottom();
	    expandFolder(folder,bottom,false);
	    response.addChild(registration);

	} else {
 	    response = new com.rbnb.api.EndOfStream();
	    parseRequest(requestI,response,false,null);
	    responseR.setFrange(requestI.getFrange());
	}

	responseR.addChild(response);

	return (responseR);
    }

    /**
     * The main method for running this plugin.
     * <p>
     *
     * @author Ian Brown
     *
     * @param argsI the list of command arguments. Valid ones are:
     *		    <p><ul>
     *		    <li><it>-a serveraddress</it> - the address of the
     *			    server.</li>
     *		    <li><it>-d debuglevel</it> - the debug level. Values
     *						 greater than 0 will produce
     *						 printout.</li>
     *		    <li><it>-f foldername</it> - the name of the folder.</li>
     *		    <li><it>-n pluginname</it> - the name to display for the
     *						 plugin.</li>
     *		    <li><it>-p period</it> - the period (seconds) between
     *			                     updates of the folder
     *					     contents.<\li>
     *		    </ul>
     * @since V2.0
     * @version 05/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/21/2002  INB	Created.
     *
     */
    public final static void main(String[] argsI) {
	try {
	    FolderPlugIn fp = new FolderPlugIn();
	    com.rbnb.utility.ArgHandler argHandler =
			new com.rbnb.utility.ArgHandler(argsI);
	    String value;
		
		if (argHandler.checkFlag('?') || argHandler.checkFlag('h'))
		{
			fp.showSyntax();
			return;
		}

	    if ((value = argHandler.getOption('a')) != null) {
		fp.setServerAddress(value);
	    }

	    if ((value = argHandler.getOption('d')) != null) {
		fp.setDebugLevel(Integer.parseInt(value));
	    }

	    if ((value = argHandler.getOption('f')) != null) {
		fp.setFolderName(value);
	    }

	    if ((value = argHandler.getOption('n')) != null) {
		fp.setPluginName(value);
	    }

	    if ((value = argHandler.getOption('p')) != null) {
                //EMF 3/18/03: make jdk1.1 compatible
		//fp.setPeriod(Double.parseDouble(value));
                fp.setPeriod(Double.valueOf(value).doubleValue());
	    }

	    fp.start();

	} catch (java.lang.Exception e) {
	    e.printStackTrace();
	}
    }

	private static void showSyntax()
	{
		System.err.println(
			"FolderPlugin [options]"
				+"\n\t-a\tServer address [localhost:3333]"
				+"\n\t-d\tDebug level [0]"
				+"\n\t-f\tFolder name [.]"
				+"\n\t-n\tPlugIn name [FolderPlugIn]"
				+"\n\t-p\tUpdate period [1.0 sec]\n");
	}
	
    /**
     * Parses (moves down) the request <code>Rmap</code> hierarchy to find out
     * what things are wanted.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI the request <code>Rmap</code> hierarchy.
     * @param responseI the response <code>Rmap</code> hierarchy.
     * @param afterModifiedI handling an AFTER or MODIFIED request?
     * @param amTimeI after/modified time.
     * @exception java.lang.Exception
     *		  thrown if there is a problem.
     * @since V2.0
     * @version 05/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/21/2002  INB	Created.
     *
     */
    private final void parseRequest(com.rbnb.api.Rmap requestI,
				    com.rbnb.api.Rmap responseI,
				    boolean afterModifiedI,
				    com.rbnb.api.TimeRange amTimeI)
	throws java.lang.Exception
    {
	boolean afterModified = afterModifiedI;
	com.rbnb.api.TimeRange amTime = amTimeI;

	if (afterModifiedI) {
	    if (requestI.getTrange() != null) {
		amTime = requestI.getTrange();
	    } else if (requestI.getFrange() != null) {
		amTime = requestI.getFrange();
	    }

	} else if (requestI instanceof com.rbnb.api.DataRequest) {
	    com.rbnb.api.DataRequest dr = (com.rbnb.api.DataRequest) requestI;

	    if ((dr.getReference() == dr.AFTER) ||
		(dr.getRelationship() == dr.GREATER) ||    // mjm 10/20/04 handle "next"
		(dr.getReference() == dr.MODIFIED)) {
		afterModified = true;
		if (dr.getTrange() != null) {
		    amTime = dr.getTrange();
		} else if (dr.getFrange() != null) {
		    amTime = dr.getFrange();
		}
	    }
	}

	if (requestI.getNchildren() == 0) {
	    readFile(responseI,afterModified,amTime);
	} else {
	    com.rbnb.api.Rmap child,
		response;
	    for (int idx = 0; idx < requestI.getNchildren(); ++idx) {
		child = requestI.getChildAt(idx);
		response = new com.rbnb.api.Rmap(child.getName());
		responseI.addChild(response);
		parseRequest(child,response,afterModified,amTime);
		if ((response.getDblock() == null) &&
		    (response.getNchildren() == 1) &&
		    (response.getChildAt
		     (0) instanceof com.rbnb.api.EndOfStream)) {
		    com.rbnb.api.Rmap eos = response.getChildAt(0);
		    response.removeChildAt(0);
		    responseI.removeChild(response);
		    int idx1;
		    for (idx1 = 0; idx1 < responseI.getNchildren(); ++idx1) {
			com.rbnb.api.Rmap child2 = responseI.getChildAt(idx1);

			if ((child2.getName() != null) ||
			    (child2 instanceof com.rbnb.api.EndOfStream)) {
			    break;
			}
		    }
		    if (idx1 == responseI.getNchildren()) {
			responseI.addChild(eos);
		    }
		}
	    }
	}
    }

    /**
     * Reads the file associated with the input response <code>Rmap</code> into
     * a byte array and places that into the response.
     * <p>
     *
     * @author Ian Brown
     *
     * @param responseI the response <code>Rmap</code> hierarchy.
     * @param afterModifiedI handling an AFTER or MODIFIED request?
     * @param amTimeI after/modified time.
     * @exception java.lang.Exception
     *		  thrown if there is a problem.
     * @since V2.0
     * @version 12/17/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/21/2002  INB	Created.
     *
     */
    private final void readFile(com.rbnb.api.Rmap responseI,
				boolean afterModifiedI,
				com.rbnb.api.TimeRange amTimeI)
	throws java.lang.Exception
    {
	//	java.io.File file = new java.io.File(responseI.getFullName());
	java.io.File file = new java.io.File
	    (getFolderName() + "/" + responseI.getFullName());

	try {
	    //System.err.println("afterModified: "+afterModifiedI);

	    if (!afterModifiedI ||
		(file.lastModified()/1000. > amTimeI.getTime())) {
		if (!file.isDirectory()) {
		    String mimeType = calculateMIMEType(file.getName());
		    responseI.setTrange
			(new com.rbnb.api.TimeRange
			    (System.currentTimeMillis()/1000.,0));
		    byte[] data;
		    java.io.FileInputStream fis = new java.io.FileInputStream
			(file);
		    if (file.length() <= Integer.MAX_VALUE) {
			data = new byte[(int) file.length()];
			fis.read(data);
			if (data.length == 0) {
			    data = new byte[1];
			    data[0] = 0;
			}
			responseI.setDblock(new com.rbnb.api.DataBlock
			   (data,
			   1,
			   data.length,
			   com.rbnb.api.DataBlock.TYPE_BYTEARRAY,
			   com.rbnb.api.DataBlock.ORDER_MSB,
			   true,
			   0,
			   data.length));
			if (mimeType != null) {
			    responseI.getDblock().setMIMEType(mimeType);
			}
		    } else {
			com.rbnb.api.Rmap response;
			long idx;
			int idx1;
			for (idx = 0,
				 idx1 = (int) (((idx + Integer.MAX_VALUE) >
						file.length()) ?
					       (file.length() - idx) :
					       Integer.MAX_VALUE);
			     idx < file.length();
			     idx += idx1,
				 idx1 = (int) (((idx + Integer.MAX_VALUE) >
						file.length()) ?
					       (file.length() - idx) :
					       Integer.MAX_VALUE)) {
			    data = new byte[idx1];
			    fis.read(data);
			    response = new com.rbnb.api.Rmap();
			    response.setDblock(new com.rbnb.api.DataBlock
			       (data,
			       1,
			       data.length,
			       com.rbnb.api.DataBlock.TYPE_BYTEARRAY,
			       com.rbnb.api.DataBlock.ORDER_MSB,
			       true,
			       0,
			       data.length));
			    if (mimeType != null) {
				response.getDblock().setMIMEType(mimeType);
			    }
			    responseI.addChild(response);
			}
		    }

		} else {
		    responseI.addChild
			(new com.rbnb.api.EndOfStream
			    (com.rbnb.api.EndOfStream.REASON_NODATA));
		    if (getDebugLevel() >= 1) {
			System.err.println
			    (responseI.getFullName() +
			     " is a directory and cannot be read.");
		    }
		}
	    }

	} catch (java.io.IOException e) {
	    if (responseI.getDblock() != null) {
		responseI.setDblock(null);
		responseI.setTrange(null);
	    } else {
		for (int idx = 0; idx < responseI.getNchildren(); ++idx) {
		    com.rbnb.api.Rmap child = responseI.getChildAt(idx);

		    if (child.getName() != null) {
			break;
		    } else {
			responseI.removeChild(child);
			--idx;
		    }
		}

		responseI.addChild
		    (new com.rbnb.api.EndOfStream
			(com.rbnb.api.EndOfStream.REASON_NODATA));
		if (getDebugLevel() >= 1) {
		    System.err.println("Failed to read: " +
				       responseI.getFullName());
		    e.printStackTrace();
		}
	    }
	}
    }

    /**
     * Retrieves the registration.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI the request (ignored).
     * @return the registration.
     * @exception java.lang.Exception
     *		  thrown if there is a problem.
     * @since V2.0
     * @version 04/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/15/2002  INB	Created.
     *
     */
    private final com.rbnb.api.Rmap retrieveRegistration
	(com.rbnb.api.DataRequest requestI)
	throws java.lang.Exception
    {
	java.io.File folder = new java.io.File(getFolderName());
	com.rbnb.api.Rmap registrationR = /*folderToRmap(folder)*/
					  new com.rbnb.api.Rmap(),
			  bottom = registrationR.moveToBottom();

	expandFolder(folder,bottom,false);

	if (getDebugLevel() >= 3) {
	    System.err.println("New registration: " + registrationR);
	}

	return (registrationR);
     }

    /**
     * Runs the plugin.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/12/2003  INB	Register a minimal map to ensure that the server always
     *			has something in its registration map for this object.
     * 01/21/2002  INB	Created.
     *
     */
    public final void run() {
	try {
	    if (getDebugLevel() >= 2) {
		System.err.println("Running folder plugin on folder " +
				   getFolderName() + " to server address " +
				   getServerAddress() + ".");
	    }

	    com.rbnb.api.Server server = com.rbnb.api.Server.newServerHandle
		(null,
		 getServerAddress());
	    com.rbnb.api.PlugIn plugin = server.createPlugIn(getPluginName());
	    plugin.start();
	    plugin.register(new com.rbnb.api.Rmap());

	    updateFolder(plugin);
	    com.rbnb.api.DataRequest request;
	    com.rbnb.api.Rmap response;
	    Object question;
	    while (true) {
		while ((question = plugin.fetch
			((long) (getPeriod()*1000.))) != null) {

		    // mjm 11-3/04:  update folder list every request 
		    // (otherwise repetitive queries prevent any updates!)
		    updateFolder(plugin);  
		    
		    if (question instanceof com.rbnb.api.EndOfStream) {
			break;
		    } else if (question instanceof com.rbnb.api.Ask) {
			com.rbnb.api.Ask ask = (com.rbnb.api.Ask)
			    question;
			request = (com.rbnb.api.DataRequest)
			    ask.getAdditional().firstElement();

			if (getDebugLevel() >= 3) {
			    System.err.println("Got registration request: " +
					       request);
			}
			response = handleRequest(request,false);
			if (getDebugLevel() >= 3) {
			    System.err.println("Send registration response: " +
					       response);
			}

		    } else {
			request = (com.rbnb.api.DataRequest) question;

			if (getDebugLevel() >= 3) {
			    System.err.println("Got request: " + request);
			}
			response = handleRequest(request,true);
			if (getDebugLevel() >= 3) {
			    System.err.println("Send response: " + response);
			}
		    }

		    plugin.addChild(response);
		}
		updateFolder(plugin);
	    }

	} catch (java.lang.Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Sets the debugging level.
     * <p>
     *
     * @author Ian Brown
     *
     * @param debugLevelI the debugging level.
     * @see #getDebugLevel()
     * @since V2.0
     * @version 01/23/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2002  INB	Created.
     *
     */
    private final void setDebugLevel(int debugLevelI) {
	debugLevel = debugLevelI;
    }

    /**
     * Sets the folder name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param folderNameI the folder name.
     * @see #getFolderName()
     * @since V2.0
     * @version 01/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/21/2002  INB	Created.
     *
     */
    private final void setFolderName(String folderNameI) {
	folderName = folderNameI;
    }

    /**
     * Sets the period between updates of the folder.
     * <p>
     *
     * @author Ian Brown
     *
     * @param periodI the period.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the period is zero or negative.
     * @see #getPeriod()
     * @since V2.0
     * @version 01/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/21/2002  INB	Created.
     *
     */
    private final void setPeriod(double periodI) {
	period = periodI;
    }

    /**
     * Sets the plugin name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param pluginNameI the plugin name.
     * @see #getPluginName()
     * @since V2.0
     * @version 01/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/21/2002  INB	Created.
     *
     */
    private final void setPluginName(String pluginNameI) {
	pluginName = pluginNameI;
    }

    /**
     * Sets the server address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverAddressI the server address.
     * @see #getServerAddress()
     * @since V2.0
     * @version 01/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/21/2002  INB	Created.
     *
     */
    private final void setServerAddress(String serverAddressI) {
	serverAddress = serverAddressI;
    }

    /**
     * Updates the registration list for the plugin from the folder.
     * <p>
     *
     * @author Ian Brown
     *
     * @param pluginI the plugin connection.
     * @exception java.lang.Exception
     *		  thrown if there is a problem.
     * @since V2.0
     * @version 03/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/21/2002  INB	Created.
     *
     */
    private final void updateFolder(com.rbnb.api.PlugIn pluginI)
	throws java.lang.Exception
    {
	com.rbnb.api.Rmap registration = retrieveRegistration(null);

	pluginI.reRegister(registration);
    }

    /**
     * Creates a valid <code>Rmap</code> name from the input name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name to fix.
     * @return the fixed name.
     * @since V2.0
     * @version 01/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/21/2002  INB	Created.
     *
     */
    private final String validRmapName(String nameI) {
	String nameR = nameI;

	if (nameI.indexOf("/") != -1) {
	    nameR = "";
	    for (int idx = 0,
		     idx1 = nameI.indexOf("/",idx);
		 idx < nameI.length();
		 idx = idx1 + 1,
		     idx1 = nameI.indexOf("/",idx)) {
		if (idx1 == idx) {
		    continue;
		}
		if (idx1 == -1) {
		    idx1 = nameI.length();
		    nameR += nameI.substring(idx,idx1);
		} else {
		    nameR += nameI.substring(idx,idx1) + "_";
		}
	    }
	}

	return (nameR);
    }
}
