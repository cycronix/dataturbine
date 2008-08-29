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

package webTurbine;

import java.util.LinkedList;

//
// 05/16/2002  WHF  Added expandSubList field, weak map of all VEs.
//

public class ViewEntity
{
	public String str, link;
	public LinkedList subList;
	
	private boolean 
		expandFlag=false,
		parentFlag=false,
		childFlag=false;		

	public ViewEntity(String str, String link)
	{ this(str,link,new LinkedList()); } 

	public ViewEntity(String str, String link, LinkedList subList)
	{ 
		this.str=str; this.link=link; this.subList=subList; 
	}
	
	public boolean isExpandSubList() { return expandFlag; }
	public void setExpandSubList(boolean expandFlag)
	{ this.expandFlag=expandFlag; }
	public boolean isParent() { return parentFlag; }
	public void setParent(boolean parentFlag) { this.parentFlag=parentFlag; }
	public boolean isChild() { return childFlag; }
	public void setChild(boolean childFlag) { this.childFlag=childFlag; }
	
	public String toString() { return Integer.toHexString(this.hashCode()); }
}
