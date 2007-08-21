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

// ExportData - null replacement for ExportData

// EMF 2/7/05: filled in the class.  Note that old constructors
//             deprecated, new one gives direct access to V2
//             data structures for efficiency and, hopefully,
//             eventually portability.

package com.rbnb.plot;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Source;
import java.awt.Frame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

public class ExportData {

public final static int EXPORT_TO_CLIPBOARD = 1;
public final static int EXPORT_TO_DATABASE = 2;
public final static int EXPORT_TO_DATATURBINE = 3;

private Frame frame=null;
private Environment environment=null;
private Sink sink=null;
private Source src=null;

//public ExportData(java.awt.Frame f,Map m,int i,RegChannel[] r,Environment e) {
	//System.err.println("ExportData constructor called");
//}

//public ExportData(java.awt.Frame f,Map m,Channel[] ch,Environment e) {
	//System.err.println("ExportData constructor called");
//}

public ExportData(Frame f)  {
	frame=f;
}

public void export(Sink s, int mode, Environment e) {
	environment=e;
	sink=s;
	show();
	dispose();
}

public void show() {
	//JOptionPane pane = new JOptionPane();
	//pane.showMessageDialog(frame,"working on it.  patience.","Exporting Data",
			//JOptionPane.INFORMATION_MESSAGE);
	/*JProgressBar bar = new JProgressBar();
	bar.setString("Exporting data ...");
	bar.setStringPainted(true);
	bar.setIndeterminate(true);
	try{Thread.currentThread().sleep(500);}catch(Exception e){}
	bar.setValue(50);
	bar.setIndeterminate(false);
	try{Thread.currentThread().sleep(500);}catch(Exception e){}
	bar.setValue(100);*/
	
	// put RBNB stuff here for now, probably should be in run() and use
	// ProgressMonitor to show user something is happening
	try{
		if (src!=null) src.CloseRBNBConnection();
		src=new Source(1,"none",0);
		src.OpenRBNBConnection(environment.HOST+":"+environment.PORT,"rbnbPlotExport");
		ChannelMap inMap=sink.getLastMap();
		ChannelMap outMap=new ChannelMap();
		for (int i=0;i<inMap.NumberOfChannels();i++) {
			String name=inMap.GetName(i);
//System.err.print("channel "+name+" exported as ");
			name=name.substring(name.lastIndexOf('/')+1);
			if (outMap.GetIndex(name)>-1) {
//System.err.print("...duplicate name, modifying...");
				int j=1;
				while (outMap.GetIndex(name+"_"+j)>-1) j++;
				name=name+"_"+j;
			}
			int j=outMap.Add(name);
//System.err.println(name);
			if (j==i) { //new channel, fill in time and data
				outMap.PutTimeRef(inMap,i);
				outMap.PutDataRef(j,inMap,i);
			}
		}
		ChannelMap cmReg=new ChannelMap();
		for (int i=0;i<outMap.NumberOfChannels();i++) {
			cmReg.Add(outMap.GetName(i));
		}
		src.Register(cmReg);
		src.Flush(outMap,true);
System.err.println("Exported "+outMap);
		//src.Detach();
//System.err.println("Detached");

	} catch (Exception e) {
		e.printStackTrace();
	}
}

public void dispose() {
}

}

