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

//Proxy - reads byte stream off server port, fills into RBNB frames, sends to server
//INB 12/05/2003 - rewrote to use the Proxy class to do the actual work.
//EMF 3/26/03
//Copyright 2003 Creare Incorporated

import com.rbnb.proxy.Proxy;

public class ProxyPlugIn implements Runnable {

    private Proxy proxy = null;

    public ProxyPlugIn(String[] args) {
	super();
	proxy = new Proxy(args);
    }

    public final static void main(String[] arg) {
	ProxyPlugIn proxyPI = new ProxyPlugIn(arg);
	new Thread(proxyPI).start();
    }

    public final void run() {
	proxy.run();
    }

} //end class Proxy



