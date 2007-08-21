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

import com.rbnb.sapi.*;

public class SendMessage {

public static void main(String[] args) {
  try {
  Sink sink=new Sink();
  sink.OpenRBNBConnection();
  ChannelMap cm=new ChannelMap();
  int num=cm.Add("/Server/EchoMessage/text");
  cm.PutDataAsString(num,"Hello, world!");
  sink.Request(cm,0,0,"newest"); //parameters are irrelevent
  ChannelMap cm2=sink.Fetch(-1);
  } catch (Exception e) {}
}

}
