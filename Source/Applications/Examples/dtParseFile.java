/*
Copyright 2012 Cycronix

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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import com.rbnb.utility.ArgHandler;
import com.rbnb.sapi.Source;
import com.rbnb.sapi.ChannelMap;

/**
 * Parses text file, sends to RBNB source line by line
 *
 * @author Matt Miller
 *
 * @since V3.2
 * @version 12/10/2012
 */

/*
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 12/10/2012  MJM	Created.
 *
 */
public class dtParseFile {

	// Constructor
	dtParseFile() {};

	public final static void main(String[] argsI) {

		String address = "localhost";	
		String srcname = "dtParseFile";		// where to put results
		String fname = "sample.txt";		// where to get data
		String cname = "line.txt";			// DT output channel name
		int ncache = 10;
		int narchive = 1000;
		int nlines = 0;
		Source source=null;
		
		System.err.println("dtParseFile -a<address> -f<file> -s<srcName> -c<chanName> -n<narchive>");

		try {
			ArgHandler ah = new ArgHandler(argsI);
			String value;

			if ((value = ah.getOption('a')) != null) address = value;
			if ((value = ah.getOption('f')) != null) fname = value;
			if ((value = ah.getOption('s')) != null) srcname = value;
			if ((value = ah.getOption('c')) != null) cname = value;
			if ((value = ah.getOption('n')) != null) narchive = new Integer(value);

			InputStream    fis;
			BufferedReader br;
			String         line;

			fis = new FileInputStream(fname);
			br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
	
			source = new Source(ncache,"create",narchive);

			ChannelMap cput = new ChannelMap();
			cput.Add(cname);

			source.OpenRBNBConnection(address,srcname);	
			while ((line = br.readLine()) != null) {
				cput.PutTime((double)nlines, 0.);
			    cput.PutDataAsString(0, line+"\n");
				source.Flush(cput,false);	
				nlines++;
//				System.err.println("put: "+line);
			}
			source.Detach();
		}
		catch(Exception e) {
			System.err.println("Exception: "+e);
			source.CloseRBNBConnection();
		}
		
		System.err.println("Done, put lines: "+nlines);
	}
}
