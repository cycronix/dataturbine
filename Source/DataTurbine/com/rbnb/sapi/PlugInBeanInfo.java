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

package com.rbnb.sapi;

import java.lang.reflect.Method;
import java.beans.*;
import java.util.*;

/**
  * Provides information about the PlugIn class for JavaBeans
  * and the ActiveX bridge.  This class is not generally useful for
  *  developers.
 * <p>
 * After V2.1 this class has been removed from the API.
 * <p>
 *
 * @author WHF
 *
 * @see com.rbnb.sapi.PlugIn
 * @since V2.0
 * @version 07/22/2002
 */

/* Copyright 2002, 2004 Creare Inc.
   All Rights Reserved */

// 05/22/2002  WHF  Updated for changes to SAPI.
// 07/22/2002  WHF  Fixed bug in class used to generate method list.

/* public */ class PlugInBeanInfo extends SimpleBeanInfo
{
	MethodDescriptor[] md;
	PropertyDescriptor[] pd=new PropertyDescriptor[0];
	BeanDescriptor bd=new BeanDescriptor(PlugIn.class);

	public PlugInBeanInfo()
	{
//		ArrayList list=new ArrayList();
		Vector list=new Vector();

		Method[] methods=PlugIn.class.getMethods();
		for (int ii=0; ii<methods.length; ++ii)
		{
			Method m=methods[ii];
			// Add all methods except these:
			if (m.getName().equals("SetRingBuffer"))
				continue;
			if (m.getName().equals("Fetch")
				&&m.getParameterTypes().length!=2)
				continue;
			if (m.getName().equals("Flush")
				&&m.getParameterTypes().length!=2)
				continue;
			if (m.getName().equals("GetChannelList")
//				&&m.getParameterTypes().length==0
				)
				continue;
			if (m.getName().equals("GetList")
//				&&m.getParameterTypes().length==0
				)
				continue;
			if (m.getName().equals("CloseRBNBConnection")
				&&m.getParameterTypes().length==0)
				continue;
			if (m.getName().equals("OpenRBNBConnection")
				&&m.getParameterTypes().length!=4)
				continue;
			if (m.getName().equals("equals"))
				continue;
			if (m.getName().equals("getClass"))
				continue;
			if (m.getName().equals("hashCode"))
				continue;
			if (m.getName().equals("notify"))
				continue;
			if (m.getName().equals("notifyAll"))
				continue;
			if (m.getName().equals("toString"))
				continue;
			if (m.getName().equals("wait"))
				continue;
		//	list.add(m);
			list.addElement(m);
		}

		md=new MethodDescriptor[list.size()];
		for (int ii=0; ii<list.size(); ++ii)
			md[ii]=new MethodDescriptor((Method) //list.get(ii));
				list.elementAt(ii));
	}

	// Mods:
	// 07/22/2002  WHF  Created.
	public BeanDescriptor getBeanDescriptor() { return bd; }

	public MethodDescriptor[] getMethodDescriptors() { return md; }
	public PropertyDescriptor[] getPropertyDescriptors() { return pd; }
}
