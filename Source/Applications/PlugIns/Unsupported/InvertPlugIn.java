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
 * Source channel inversion plugin application for V2 <bold>RBNBs</bold>.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 01/21/2002
 */

/*
 * Copyright 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/18/2002  INB	Created.
 *
 */
public class InvertPlugIn
    extends java.lang.Thread
{
    /**
     * have we seen an <code>EndOfStream</code>?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/18/2002
     */
    private boolean seenEOS = false;

    /**
     * the address of the <bold>RBNB</bold>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/18/2002
     */
    private String serverAddress = "localhost:3333";

    /**
     * source channel names.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/18/2002
     */
    private String[] sourceNames = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/18/2002  INB	Created.
     *
     */
    private InvertPlugIn() {
	super();
    }

    /**
     * Builds the output channel list based on the input source channel names.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sinkI   the <code>Sink</code> connection to use to retrieve
     *		      information.
     * @return the registration <code>Rmap</code> for the plugin.
     * @exception java.lang.Exception
     *		  thrown if there is an problem.
     * @since V2.0
     * @version 01/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/18/2002  INB	Created.
     *
     */
    private final com.rbnb.api.Rmap buildChannels(com.rbnb.api.Sink sinkI)
	throws java.lang.Exception
    {
	com.rbnb.api.Rmap outputListR = new com.rbnb.api.Rmap(),
			  request,
			  bottom,
			  newOne,
			  sourceMatch;
	String[] channels;
	for (int idx = 0; idx < getSourceNames().length; ++idx) {
	    request = com.rbnb.api.Rmap.createFromName(getSourceNames()[idx]);
	    sourceMatch = sinkI.getRegistered(request);
	    if (sourceMatch != null) {
		channels = sourceMatch.extractNames();

		if (channels != null) {
		    for (int idx1 = 0; idx1 < channels.length; ++idx1) {
			newOne =
			    com.rbnb.api.Rmap.createFromName(channels[idx1]);
			for (bottom = newOne;
			     bottom.getNchildren() > 0;
			     bottom = bottom.getChildAt(0)) {
			}
			bottom.setDblock
			    (new com.rbnb.api.DataBlock(new byte[1],1,1));
			
			outputListR.mergeWith(newOne);
		    }
		}
	    }
	}

	return (outputListR);
    }

    /**
     * Fetches the next response from the <code>Sink</code>.
     * <p>
     * This method places a new <code>Rmap</code> at the top of the returned
     * hierarchy that has the name of the input <code>DataRequest</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sinkI    the <code>Sink</code> to use to get the response.
     * @param requestI the <code>DataRequest</code>.
     * @return the response <code>Rmap</code> or null if no more.
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
     * 01/18/2002  INB	Created.
     *
     */
    private final com.rbnb.api.Rmap fetch(com.rbnb.api.Sink sinkI,
					  com.rbnb.api.DataRequest requestI)
	throws java.lang.Exception
    {
	com.rbnb.api.Rmap responseR = null;

	if (!getSeenEOS()) {
	    com.rbnb.api.Rmap fetched = sinkI.fetch(sinkI.FOREVER);

	    if (fetched instanceof com.rbnb.api.EndOfStream) {
		setSeenEOS(true);
	    }

	    String[] names = fetched.extractNames();
	    com.rbnb.api.DataArray da;
	    com.rbnb.api.DataBlock dBlock;
	    com.rbnb.api.TimeRange tr,
		fr;
	    byte type;
	    com.rbnb.api.Rmap response =
		(getSeenEOS() ?
		 new com.rbnb.api.EndOfStream
		     (((com.rbnb.api.EndOfStream) fetched).getReason()) :
		 new com.rbnb.api.Rmap()),
		mergeWith,
		top,
		bottom;
	    int idx1,
		points,
		ptsize;
	    for (int idx = 0; idx < names.length; ++idx) {
		da = fetched.extract(names[idx],
				     true,
				     true,
				     true);

		tr = new com.rbnb.api.TimeRange(da.getTime(),0.);
		fr = new com.rbnb.api.TimeRange(da.getFrame(),0.);
		if (da.getData() instanceof short[]) {
		    short[] data = (short[]) da.getData();
		    type = com.rbnb.api.DataBlock.TYPE_INT16;
		    points = data.length;
		    ptsize = 2;
		    for (idx1 = 0; idx1 < data.length; ++idx1) {
			data[idx1] = (short) -data[idx1];
		    }
		} else if (da.getData() instanceof int[]) {
		    int[] data = (int[]) da.getData();
		    type = com.rbnb.api.DataBlock.TYPE_INT32;
		    points = data.length;
		    ptsize = 4;
		    for (idx1 = 0; idx1 < data.length; ++idx1) {
			data[idx1] = -data[idx1];
		    }
		} else if (da.getData() instanceof long[]) {
		    long[] data = (long[]) da.getData();
		    type = com.rbnb.api.DataBlock.TYPE_INT64;
		    points = data.length;
		    ptsize = 8;
		    for (idx1 = 0; idx1 < data.length; ++idx1) {
			data[idx1] = -data[idx1];
		    }
		} else if (da.getData() instanceof float[]) {
		    float[] data = (float[]) da.getData();
		    type = com.rbnb.api.DataBlock.TYPE_FLOAT32;
		    points = data.length;
		    ptsize = 4;
		    for (idx1 = 0; idx1 < data.length; ++idx1) {
			data[idx1] = -data[idx1];
		    }
		} else if (da.getData() instanceof long[]) {
		    double[] data = (double[]) da.getData();
		    type = com.rbnb.api.DataBlock.TYPE_FLOAT64;
		    points = data.length;
		    ptsize = 8;
		    for (idx1 = 0; idx1 < data.length; ++idx1) {
			data[idx1] = -data[idx1];
		    }
		} else {
		    continue;
		}

		top = com.rbnb.api.Rmap.createFromName(names[idx]);
		for (bottom = top;
		     bottom.getNchildren() == 1;
		     bottom = bottom.getChildAt(0)) {
		}
		dBlock = new com.rbnb.api.DataBlock
		    (da.getData(),
		     points,
		     ptsize,
		     type,
		     com.rbnb.api.DataBlock.ORDER_MSB,
		     false,
		     0,
		     ptsize);
		bottom.setDblock(dBlock);
		bottom.setTrange(tr);
		bottom.setFrange(fr);
		if (response.getNchildren() == 0) {
		    response.addChild(top);
		} else {
		    mergeWith = response.findDescendant(top.getName(),false);
		    if (mergeWith == null) {
			response.addChild(top);
		    } else {
			mergeWith.mergeWith(top);
		    }
		}
	    }

	    responseR = new com.rbnb.api.Rmap(requestI.getName());
	    responseR.setTrange(requestI.getTrange());
	    responseR.setFrange(requestI.getFrange());
	    responseR.addChild(response);
	}

	return (responseR);
    }

    /**
     * Gets the seenEOS flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return have we seen an <code>EndOfStream</code>?
     * @see #setSeenEOS(boolean)
     * @since V2.0
     * @version 01/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/18/2002  INB	Created.
     *
     */
    private final boolean getSeenEOS() {
	return (seenEOS);
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
     * @version 01/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/18/2002  INB	Created.
     *
     */
    private final String getServerAddress() {
	return (serverAddress);
    }

    /**
     * Gets the source channel names.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the source channel names.
     * @see #setSourceNames(String[])
     * @since V2.0
     * @version 01/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/18/2002  INB	Created.
     *
     */
    private final String[] getSourceNames() {
	return (sourceNames);
    }

    /**
     * Initiates a request for data.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sinkI    the <code>Sink</code> to handle the request.
     * @param requestI the request <code>DataRequest</code>.
     * @exception java.lang.Exception
     *		  thrown if there is a problem.
     * @since V2.0
     * @version 01/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/18/2002  INB	Created.
     *
     */
    private final void initiateRequest
	(com.rbnb.api.Sink sinkI,
	 com.rbnb.api.DataRequest requestI)
	throws java.lang.Exception
    {
	setSeenEOS(false);
	sinkI.addChild(requestI);
	sinkI.initiateRequestAt(0);
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
     *		    <li><it>-c chan1[,...,chanN]</it> - the channel names.<li>
     *		    </ul>
     * @since V2.0
     * @version 01/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     *
     */
    public final static void main(String[] argsI) {
	try {
	    InvertPlugIn ip = new InvertPlugIn();
	    com.rbnb.utility.ArgHandler argHandler =
		new com.rbnb.utility.ArgHandler(argsI);
	    String value;

	    if ((value = argHandler.getOption('a')) != null) {
		ip.setServerAddress(value);
	    }

	    if ((value = argHandler.getOption('c')) != null) {
		ip.parseSourceNames(value);
	    }

	    ip.start();
	} catch (java.lang.Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Parses the source names.
     * <p>
     *
     * @author Ian Brown
     *
     * @param namesI the names string. This is a comma separated list of names.
     * @since V2.0
     * @version 01/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/18/2002  INB	Created.
     *
     */
    private final void parseSourceNames(String namesI) {
	java.util.Vector indexes = new java.util.Vector();

	for (int idx = 0,
		 idx1 = namesI.indexOf(",",idx);
	     idx < namesI.length();
	     idx = idx1 + 1,
		 idx1 = ((idx < namesI.length()) ?
			 namesI.indexOf(",",idx) :
			 -1)) {
	    if (idx1 == -1) {
		idx1 = namesI.length();
	    }
	    indexes.addElement(new Integer(idx1));
	}

	setSourceNames(new String[indexes.size()]);
	for (int idx =  0,
		 idx1 = 0,
		 idx2 = 0;
	     idx1 < indexes.size();
	     idx = idx2,
		 ++idx1) {
	    idx2 = ((Integer) indexes.elementAt(idx1)).intValue();
	    getSourceNames()[idx1] = namesI.substring(idx,idx2);
	}
    }

    /**
     * Runs the plugin.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/18/2002  INB	Created.
     *
     */
    public final void run() {
	try {
	    com.rbnb.api.Server server = com.rbnb.api.Server.newServerHandle
		(null,
		 getServerAddress());
//	    com.rbnb.api.PlugIn plugin = server.createPlugIn("Invert");
	    com.rbnb.api.PlugIn plugin = server.createPlugIn("invert");
	    plugin.start();

	    com.rbnb.api.Sink sink = server.createSink
		("_" + plugin.getName() + ".sink");
	    sink.start();

	    com.rbnb.api.Rmap regList = buildChannels(sink);
	    if (regList.getNchildren() == 0) {
		throw new java.lang.IllegalArgumentException
		    ("Unable to find any matching source channels.");
	    }
	    plugin.register(regList);

	    com.rbnb.api.DataRequest request;
	    com.rbnb.api.Rmap response;
	    while ((request =
		    (com.rbnb.api.DataRequest) plugin.fetch
		    (plugin.FOREVER)) != null) {
		initiateRequest(sink,request);
		while ((response = fetch(sink,request)) != null) {
		    plugin.addChild(response);
		}
	    }

	} catch (java.lang.Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Sets the seenEOS flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param seenEOSI have we seen an <code>EndOfStream</code>?
     * @see #getSeenEOS()
     * @since V2.0
     * @version 01/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/18/2002  INB	Created.
     *
     */
    private final void setSeenEOS(boolean seenEOSI) {
	seenEOS = seenEOSI;
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
     * @version 01/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/18/2002  INB	Created.
     *
     */
    private final void setServerAddress(String serverAddressI) {
	serverAddress = serverAddressI;
    }

    /**
     * Sets the source channel names.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sourceNamesI the source channel names.
     * @see #getSourceNames()
     * @since V2.0
     * @version 01/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/18/2002  INB	Created.
     *
     */
    private final void setSourceNames(String[] sourceNamesI) {
	sourceNames = sourceNamesI;
    }
}
