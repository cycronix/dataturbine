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

// Map - replacement for COM.Creare.RBNB.API.Map

package com.rbnb.plot;

import java.util.Vector;

public class Map {

private Vector chans=new Vector();

public Map() {
}

public Map(String[] chanList) {
  for (int i=0;i<chanList.length;i++) {
    addChannel(new Channel(chanList[i]));
  }
}

//EMF 3/28/05: new constructor which takes userinfo
public Map(String[][] chanList) {
	for (int i=0;i<chanList[0].length;i++) {
		addChannel(new Channel(chanList[0][i],chanList[1][i],chanList[2][i]));
	}
}

public void addChannel(Channel c) {
//System.err.println("Map.addChannel: adding "+c+" to "+this);
  chans.add(c);
}

public Channel[] channelList() {
  if (chans.isEmpty()) return new Channel[0];
  else {
    Object[] oa=chans.toArray();
    Channel[] ca=new Channel[oa.length];
    for (int i=0;i<oa.length;i++) ca[i]=(Channel)oa[i];
    return ca;
  }
}

public Channel findChannel(String name) {
//System.err.print("Map.findChannel: looking for "+name);
  Channel ch=new Channel(name);
  int idx=chans.indexOf(ch);
//if (idx==-1) System.err.println(", not found");
//else System.err.println(", found");
  if (idx==-1) return null;
  else return (Channel)chans.elementAt(idx);
}

public void removeChannel(Channel ch) {
  chans.remove(ch);
}

public void clear() {
  chans.clear();
}

}

