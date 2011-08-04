/*
Copyright 2011 Erigo Technologies LLC

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

/*****************************************************************************
 * 
 * Read NMEA strings from a named pipe and output the corresponding IWG1
 * string to an RBNB.
 * <p>
 * 
 * @author Stephen Carlson, USRP intern, NASA DFRC
 * @author John Wilson, Erigo Technologies
 * 
 * @version 03/23/2010
 * 
 */

/*
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 03/01/2010  SC	Created
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;

public class NMEAParser {
	
	/*
	 * Connect to the specified RBNB server and subscribe to
	 * the specified channel.
	 */
	public static void main(String[] args) {
		
		boolean bSubscribeMode = true;
		long msecDelayBetweenRequests = 5000;
		boolean bVerbose = false;
		
		double prevTimestampD = -Double.MAX_VALUE;
		
		if ((args.length < 3) || (args[0].equals("-h"))) {
			System.err.println("Usage:");
			System.err.println("NMEAParser <RBNB server address> <source/chan to subscribe to> <source/chan to flush IWG1 to> [-v (turn on verbose mode)]");
			System.exit(0);
		}
		
		// Check if user wants verbose mode
		if ( (args.length == 4) && (args[3].equals("-v")) ) {
		    bVerbose = true;
		}
		
		String address = args[0];
		String fetchChan = args[1];
		int slashIdx = args[2].indexOf('/');
		if (slashIdx == -1) {
			System.err.println("Must specify source/chan for storing IWG1 in the server.");
			System.exit(0);
		}
		String flushSource = args[2].substring(0, slashIdx);
		String flushChan = args[2].substring(slashIdx+1);
		Sink sink = new Sink();
		Source source = new Source(100,"create",1000000);
		try {
			sink.OpenRBNBConnection(address, "NMEAParser");
			source.OpenRBNBConnection(address, flushSource);
		} catch (SAPIException e) {
			System.err.println("Error opening connection to server:\n" + e);
			System.exit(0);
		}
		if (bSubscribeMode) {
		    ChannelMap requestMap = new ChannelMap();
		    try {
			requestMap.Add(fetchChan);
			sink.Subscribe(requestMap);
		    } catch (SAPIException e) {
			System.err.println("Error setting up Subscription for requested channel, " + fetchChan + ":\n" + e);
			sink.CloseRBNBConnection();
			System.exit(0);
		    }
		}
		if (bSubscribeMode) {
		    System.err.println("Running in Subscribe mode.");
		} else {
		    System.err.println("Running in Request newest mode.");
		    if (msecDelayBetweenRequests > 0) {
			System.err.println("Delay between requests = " + msecDelayBetweenRequests + " msec.");
		    }
		}
		ChannelMap fetchMap = null;
		ChannelMap flushMap = null;
		while (true) {
			if (bSubscribeMode) {
		            try {
		        	fetchMap = sink.Fetch(10000);
		            } catch (SAPIException e) {
		        	System.err.println("Caught exception trying to fetch data:\n" + e);
		        	continue;
		            }
		        } else {
		            // Request/response mode
		            ChannelMap requestMap = new ChannelMap();
		            try {
		        	requestMap.Add(fetchChan);
		        	sink.Request(requestMap, 0.0, 0.0, "newest");
		        	fetchMap = sink.Fetch(10000);
		            } catch (SAPIException e) {
		        	System.err.println("Caught exception trying to fetch data:\n" + e);
		        	continue;
		            }
		        }
			if (fetchMap.GetIfFetchTimedOut()) {
				continue;
			}
			if (fetchMap.NumberOfChannels() != 1) {
				System.err.println("Error - expected 1 channel, got " + fetchMap.NumberOfChannels());
				continue;
			}
			if (!fetchChan.equals(fetchMap.GetName(0))) {
				System.err.println("Error - expected channel named \"" + fetchChan + "\", got \"" + fetchMap.GetName(0) + "\"");
				continue;
			}
			byte[][] data = fetchMap.GetDataAsByteArray(0);
			// Break data up into a series of NMEA sentences
			for (int i = 0; i < data.length; ++i) {
				String textBlock = new String(data[i]);
				// System.err.println("\n\n\nInput block:\n" + textBlock.trim() + "\n");
				BufferedReader bufRead = new BufferedReader(new StringReader(textBlock));
				try {
				String sentence = null;
				while ((sentence=bufRead.readLine()) != null) {
					sentence = sentence.trim();
					if (sentence.isEmpty()) {
						continue;
					}
					// System.err.println("\nInput = <" + sentence + ">");
					IWG1 iwg = new IWG1(sentence);
					if (!iwg.isValid) {
					    if ( (bVerbose) && (!iwg.errorStr.isEmpty()) ) {
						System.err.println("\nInput = <" + sentence + ">");
						System.err.println("Error output = <" + iwg.errorStr.trim() + ">");
					    }
					} else {
						if (bVerbose) {
						    System.err.println("\nInput = <" + sentence + ">");
						    System.err.println("Output = <" + iwg.toString().trim() + ">");
						    if (!iwg.errorStr.isEmpty()) {
							System.err.println("Error output = <" + iwg.errorStr.trim() + ">");
						    }
						}
						// Send IWG1 to the RBNB as long as time has been set
						if (IWG1.time_double == 0) {
							// Time hasn't been set yet
							continue;
						}
						double timestampD = IWG1.time_double;
						if (prevTimestampD >= timestampD) {
							// No new time has been parsed
							// Inrement the previous timestamp by 1 msec and use it as the timestamp
							timestampD = prevTimestampD + 0.001;
						}
						flushMap = new ChannelMap();
						try {
							flushMap.Add(flushChan);
							flushMap.PutTime(timestampD,0.0);
							flushMap.PutDataAsString(0, iwg.toString());
							source.Flush(flushMap);
						} catch (SAPIException e) {
							System.err.println("Error sending data to RBNB:\n" + e);
							continue;
						}
						prevTimestampD = timestampD;
					}
				}
				} catch (IOException ioe) {
					System.err.println("Error exception reading input text block:\n" + ioe);
					continue;
				}
			}
			if ( (!bSubscribeMode) && (msecDelayBetweenRequests > 0) ) {
			    try {
				Thread.sleep(msecDelayBetweenRequests);
			    } catch (InterruptedException e) {
				// Nothing to do
			    }
			}
		}
	}
	
	/*
	 * Based on Steve Carlson's original code;
	 * read NMEA strings from stdin
	 * 
	public static void main(String[] args) {
		IWG1 iwg = new IWG1();
		Scanner scan = new Scanner(System.in);
		String sentence = new String();
		
		boolean done = false;
		
		do {
			try {
				sentence = scan.nextLine();
			} catch(NoSuchElementException e) {
				System.out.println("Scanner tripped, too many lines?");
				//scan.close(); //With this disabled, Program works with dozens of lines pasted 
				scan = new Scanner(System.in); //I don't full understand what monster I've created
				//here; am I asking for 20000 new Scanners if 20000 lines are pasted into the shell?
			}
			if(sentence.equalsIgnoreCase("q")) {
				done=true;
			} else {
				// NOTE: We reuse the same IWG1 object, which maintains data not found in all input NMEA strings
				iwg.parse(sentence);
				System.err.println(iwg);
			}
		} while(!done);
	}
	*
	*
	*/
	
}
