/*
 *  $Id: VariableValueMap.java,v 1.12 2003/09/10 13:49:51 jpz Exp $
 *  IzPack
 *  Copyright (C) 2002 Johannes Lehtinen
 *
 *  File :               VariableValueMap.java
 *  Description :        Interface for map of variable values.
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

import java.util.Map;

/**
 *  Maps variable names to a variable value.
 *
 * @author     Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
public interface VariableValueMap extends Map
{

  /**
   *  Returns the current value for the specified variable.
   *
   * @param  var  the name of the variable
   * @return      the current value or null if not set
   */
  String getVariable(String var);


  /**
   *  Sets a new value for the specified variable.
   *
   * @param  var  the name of the variable
   * @param  val  the new value for the variable
   */
  void setVariable(String var, String val);
}

