/*
 *  $Id: Packager.java,v 1.21 2004/02/20 09:01:39 jponge Exp $
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
 *
 *  File :               Packager.java
 *  Description :        The abstract class for the packagers.
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
 *
 *  Portions are Copyright (c) 2001 Johannes Lehtinen
 *  johannes.lehtinen@iki.fi
 *  http://www.iki.fi/jle/
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
package com.izforge.izpack.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.izforge.izpack.GUIPrefs;
import com.izforge.izpack.Info;
import com.izforge.izpack.Pack;

/**
 *  The packager class. A packager is used by the compiler to actually do the
 *  packaging job.
 *
 * @author     Julien Ponge
 */
public abstract class Packager
{
  /**  The path to the skeleton installer. */
  public final static String SKELETON_SUBPATH =
    "lib" + File.separator + "installer.jar";

  /**  The packs informations. */
  protected ArrayList packs;

  /**  The langpacks ISO3 names. */
  protected ArrayList langpacks;

  /**  The listeners. */
  protected PackagerListener listener;

  /**
   *  Adds a listener.
   *
   * @param  listener  The listener.
   */
  public void setPackagerListener(PackagerListener listener)
  {
    this.listener = listener;
  }

  /**
   *  Dispatches a message to the listeners.
   *
   * @param  job  The job description.
   */
  protected void sendMsg(String job)
  {
    listener.packagerMsg(job);
  }

  /**  Dispatches a start event to the listeners.  */
  protected void sendStart()
  {
    listener.packagerStart();
  }

  /**  Dispatches a stop event to the listeners.  */
  protected void sendStop()
  {
    listener.packagerStop();
  }

  /**
   * Write the skeleton installer to the output JAR. 
   * 
   * @param out
   * @throws Exception
   */
  public void writeSkeletonInstaller(JarOutputStream out) throws Exception
  {
    InputStream is = getClass().getResourceAsStream("/lib/installer.jar");
    ZipInputStream skeleton_is = null;
    if (is != null)
    {
      skeleton_is = new ZipInputStream(is);
    }

    if (skeleton_is == null)
    {
      skeleton_is =
        new ZipInputStream(
          new FileInputStream(
            Compiler.IZPACK_HOME + "lib" + File.separator + "installer.jar"));
    }

    ZipEntry zentry;

    while ((zentry = skeleton_is.getNextEntry()) != null)
    {
      // Puts a new entry
      out.putNextEntry(new ZipEntry(zentry.getName()));

      // Copy the data
      copyStream(skeleton_is, out);

      out.closeEntry();
      skeleton_is.closeEntry();
    }

  }

  /**
   *  Adds a pack (the compiler sends the merged data).
   *
   * @param  packNumber     The pack number.
   * @param  name           The pack name.
   * @param  required       Is the pack required?
   * @param  osConstraints  The target operation system(s) of this pack.
   * @param  description    The pack description.
   * @param  preselected    Is the pack selected by default?
   * @return                Description of the Return Value
   * @exception  Exception  Description of the Exception
   */
  public abstract ZipOutputStream addPack(
    int packNumber,
    String name,
    List osConstraints,
    boolean required,
    String description,
    boolean preselected)
    throws Exception;

  /**
   *  Adds a panel.
   *
   * @param  classFilename  The class filename.
   * @param  input          The stream to get the file data from.
   * @exception  Exception  Description of the Exception
   */
  public abstract void addPanelClass(String classFilename, InputStream input)
    throws Exception;

  /**
   *  Sets the GUI preferences.
   *
   * @param  prefs          The new gUIPrefs value
   * @exception  Exception  Description of the Exception
   */
  public abstract void setGUIPrefs(GUIPrefs prefs) throws Exception;

  /**
   *  Check if backrefs are allowed.
   *
   */
  public abstract boolean allowPackFileBackReferences();

  /**
   *  Sets the panels order.
   *
   * @param  order          The ordered list of the panels.
   * @exception  Exception  Description of the Exception
   */
  public abstract void setPanelsOrder(ArrayList order) throws Exception;

  /**
   *  Sets the informations related to this installation.
   *
   * @param  info           The info section.
   * @exception  Exception  Description of the Exception
   */
  public abstract void setInfo(Info info) throws Exception;

  /**
   *  Adds Variable Declaration.
   *
   * @param  varDef         The variables definitions.
   * @exception  Exception  Description of the Exception
   */
  public abstract void setVariables(Properties varDef) throws Exception;

  /**
   *  Adds a resource.
   *
   * @param  resId          The resource Id.
   * @param  input          The stream to get the data from.
   * @exception  Exception  Description of the Exception
   */
  public abstract void addResource(String resId, InputStream input)
    throws Exception;

  /**
   *  Adds a language pack.
   *
   * @param  iso3           The ISO3 code.
   * @param  input          The stream to get the data from.
   * @exception  Exception  Description of the Exception
   */
  public abstract void addLangPack(String iso3, InputStream input)
    throws Exception;

  /**
   *  Adds a native library.
   *
   * @param  name           The native library name.
   * @param  input          The stream to get the data from.
   * @exception  Exception  Description of the Exception
   */
  public abstract void addNativeLibrary(String name, InputStream input)
    throws Exception;

  /**
   *  Adds a jar file content to the installer.
   *
   * @param  file           The jar filename.
   * @exception  Exception  Description of the Exception
   */
  public abstract void addJarContent(String file) throws Exception;

  /**
   *  Tells the packager to finish the job (misc writings, cleanups, closings ,
   *  ...).
   *
   * @exception  Exception  Description of the Exception
   */
  public abstract void finish() throws Exception;

  /**
   *  Copies all the data from the specified input stream to the specified
   *  output stream. This is an utility method which may be used by the
   *  subclasses. by Johannes Lehtinen
   *
   * @param  in               the input stream to read
   * @param  out              the output stream to write
   * @return                  the total number of bytes copied
   * @exception  IOException  if an I/O error occurs
   */
  protected long copyStream(InputStream in, OutputStream out)
    throws IOException
  {
    byte[] buffer = new byte[5120];
    long bytesCopied = 0;
    int bytesInBuffer;
    while ((bytesInBuffer = in.read(buffer)) != -1)
    {
      out.write(buffer, 0, bytesInBuffer);
      bytesCopied += bytesInBuffer;
    }
    return bytesCopied;
  }

  /**
   *  Called by the Compiler when the pack content adding is done. (JP)
   *
   * @param  number  the pack number
   * @param  nbytes  the number of bytes written
   */
  protected void packAdded(int number, long nbytes)
  {
    ((Pack) packs.get(number)).nbytes = nbytes;
  }
}
