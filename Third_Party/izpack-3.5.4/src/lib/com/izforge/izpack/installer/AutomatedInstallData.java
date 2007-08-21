/*
 *  $Id: AutomatedInstallData.java,v 1.7 2004/02/20 09:01:39 jponge Exp $
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
 *
 *  File :               AutomatedInstallData.java
 *  Description :        Installer internal data.
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipOutputStream;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.Info;
import com.izforge.izpack.LocaleDatabase;

/**
 *  Encloses information about the install process.
 *  This implementation is not thread safe.
 *
 * @author     Julien Ponge <julien@izforge.com>
 * @author     Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
public class AutomatedInstallData
{
  //--- Static members -------------------------------------------------

  /**
   *  A Properties based implementation for VariableValueMap interface.
   *
   * @author     Johannes Lehtinen <johannes.lehtinen@iki.fi>
   * @version    $Revision: 1.7 $
   */
  private final static class VariableValueMapImpl
    extends Properties
    implements VariableValueMap
  {

    public String getVariable(String var)
    {
      return getProperty(var);
    }

    public void setVariable(String var, String val)
    {
      if (var != null && val != null)
      {
        setProperty(var, val);
      }
    }
  }

  //--- Instance members -----------------------------------------------

  /**  The language code. */
  public String localeISO3;

  /**  The language pack. */
  public LocaleDatabase langpack;

  /**  The installer kind. */
  public String kind;

  /**  The uninstaller jar stream. */
  public ZipOutputStream uninstallOutJar;

  /**  The inforamtions. */
  public Info info;

  /**  The complete list of packs. */
  public List allPacks;

  /**  The available packs. */
  public List availablePacks;

  /**  The selected packs. */
  public List selectedPacks;

  /**  The panels list. */
  public List panels;

  /**  The panels order. */
  public List panelsOrder;

  /**  The current panel. */
  public int curPanelNumber;

  /**  Can we close the installer ? */
  public boolean canClose = false;

  /**  Did the installation succeed ? */
  public boolean installSuccess = true;

  /**  The xmlData for automated installers. */
  public XMLElement xmlData;

  /**  Maps the variable names to their values */
  protected VariableValueMap variableValueMap;

  /**  The attributes used by the panels */
  protected Map attributes;

  /**  Constructs a new instance of this class.  */
  public AutomatedInstallData()
  {
    availablePacks = new ArrayList();
    selectedPacks = new ArrayList();
    panels = new ArrayList();
    panelsOrder = new ArrayList();
    xmlData = new XMLElement("AutomatedInstallation");
    variableValueMap = new VariableValueMapImpl();
    attributes = new HashMap();
  }

  /**
   *  Returns the map of variable values. Modifying this map will directly
   *  affect the current value of variables.
   *
   * @return    the map of variable values
   */
  public VariableValueMap getVariableValueMap()
  {
    return variableValueMap;
  }

  /**
   *  Sets a variable to the specified value. This is short hand for <code>getVariableValueMap().setVariable(var, val)</code>
   *  .
   *
   * @param  var  the name of the variable
   * @param  val  the new value of the variable
   * @see         #getVariable
   */
  public void setVariable(String var, String val)
  {
    variableValueMap.setVariable(var, val);
  }

  /**
   *  Returns the current value of the specified variable. This is short hand
   *  for <code>getVariableValueMap().getVariable(var)</code>.
   *
   * @param  var  the name of the variable
   * @return      the value of the variable or null if not set
   * @see         #setVariable
   */
  public String getVariable(String var)
  {
    return variableValueMap.getVariable(var);
  }

  /**
   *  Sets the install path.
   *
   * @param  path  the new install path
   * @see          #getInstallPath
   */
  public void setInstallPath(String path)
  {
    setVariable(ScriptParser.INSTALL_PATH, path);
  }

  /**
   *  Returns the install path.
   *
   * @return    the current install path or null if none set yet
   * @see       #setInstallPath
   */
  public String getInstallPath()
  {
    return getVariable(ScriptParser.INSTALL_PATH);
  }

  /**
   *  Returns the value of the named attribute.
   *
   * @param  attr  the name of the attribute
   * @return       the value of the attribute or null if not set
   * @see          #setAttribute
   */
  public Object getAttribute(String attr)
  {
    return attributes.get(attr);
  }

  /**
   *  Sets a named attribute. The panels and other IzPack components can attach
   *  custom attributes to InstallData to communicate with each other. For
   *  example, a set of co-operating custom panels do not need to implement a
   *  common data storage but can use InstallData singleton. The name of the
   *  attribute should include the package and class name to prevent name space
   *  collisions.
   *
   * @param  attr  the name of the attribute to set
   * @param  val   the value of the attribute or null to unset the attribute
   * @see          #getAttribute
   */
  public void setAttribute(String attr, Object val)
  {
    if (val == null)
      attributes.remove(attr);
    else
      attributes.put(attr, val);

  }
}
