/*
 *  $Id: VariableValueMapImpl.java,v 1.3 2003/09/10 13:49:51 jpz Exp $
 *  IzPack
 *  Copyright (C) 2002 Johannes Lehtinen
 *
 *  File :               VariableValueMap.java
 *  Description :        Map of variable values implementation.
 *  Author's email :     johannes.lehtinen@iki.fi
 *  Author's Website :   http://www.iki.fi/jle/
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.izforge.izpack.installer;

import java.util.Properties;

/**
 *  A Properties based implementation for VariableValueMap interface.
 *
 * @author     Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
public final class VariableValueMapImpl extends Properties implements VariableValueMap
{

  /**
   *  Returns the current value for the specified variable.
   *
   * @param  var  the name of the variable
   * @return      the current value or null if not set
   */
  public String getVariable(String var)
  {
    return getProperty(var);
  }


  /**
   *  Sets a new value for the specified variable.
   *
   * @param  var  the name of the variable
   * @param  val  the new value for the variable
   */
  public void setVariable(String var, String val)
  {
    setProperty(var, val);
  }
}

