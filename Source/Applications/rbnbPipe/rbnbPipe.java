// MJM 6/2010
// read stream of data from a pipe and send to RBNB

import com.rbnb.sapi.Source;
import com.rbnb.sapi.ChannelMap;
import java.io.*;

class rbnbPipe extends Thread {
    
    public static void main (String[] args) {

        String fname = new String((args.length>0)?args[0]:"pipeName");
        String host = new String((args.length>1)?args[1]:"localhost:3333");
        String Scache = new String((args.length>2)?args[2]:"10");
        String Sarchive = new String((args.length>3)?args[3]:"10000");
        String Amode = new String((args.length>4)?args[4]:"append");

    	System.out.println(rbnbPipe.class.getName()+" "+fname+" [host ("+host+")] [cache ("+Scache+")] [archive ("+Sarchive+")] [amode ("+Amode+")]");
        if(args.length == 0) System.exit(0);
    
        Integer Ncache = Integer.parseInt(Scache);
        Integer Narchive = Integer.parseInt(Sarchive);
        
	    Source source = new Source(Ncache,Amode,Narchive);	
		try {
		// open RBNB Source
			String sname = fname;
			String[] srcName = fname.split("/");		// last part of folder/name
			if(srcName.length > 0) sname = srcName[srcName.length-1]; 
			srcName = sname.split("[.]");				// first part of name.suffix
			if(srcName.length > 0) sname = srcName[0];
		    source.OpenRBNBConnection(host,sname); 
		    sname = source.GetClientName();
		    System.err.println("Opened RBNB source: "+sname);
		    
		// define RBNB channels
		    ChannelMap cMap = new ChannelMap();
		    cMap.Add(fname);		// matches filename
    	
            System.err.println("rbnbPipe opening... "+fname);
            FileInputStream infile = new FileInputStream(fname);
            InputStream in = new BufferedInputStream(infile);

            while (true) {		// loop fetching data
                byte[] buf = null;
                byte[] buf2 = null;
                int nread=0;
                
            	try {
           			while(in.available() > 0) {	// concatenate chunks (if any)
           				buf2 = buf;
           				buf = new byte[in.available()];
           				nread = in.read(buf);	// blocking read
           				System.err.println("rbnbPipe nread: "+nread);

           				if(buf2 != null) {	// append
           					byte[] buf3 = new byte[buf.length + buf2.length];
           					System.arraycopy(buf, 0, buf3, 0, buf.length);
           					System.arraycopy(buf2, 0, buf3, buf.length, buf2.length);
           					buf = buf3;
           				}
            		};
            	} catch (IOException e) {
            		System.err.println("IOException on read: "+e);
                    infile = new FileInputStream(fname); // try again? (not sure this succeeds)
                    in = new BufferedInputStream(infile);
            		sleep(500);
            	}
            	
            	if(buf != null) {			// got some, send to RBNB
					cMap.PutDataAsByteArray(0, buf);
					source.Flush(cMap);	
					System.out.println("rbnbPipe Put: "+buf.length);
            	}
            	else sleep(100);		// ease up
//                System.err.println("try again...");
            }
        } catch (Exception e) {
            System.err.println(e);
        }
		finally {			
		    /* Close the archive. */
			System.err.println("closed RBNB source: "+source.GetClientName());
		    source.CloseRBNBConnection();
		} 
    }
}

