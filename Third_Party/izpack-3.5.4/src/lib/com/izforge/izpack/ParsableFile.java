/*
 *  $Id: ParsableFile.java,v 1.14 2003/09/10 13:49:53 jpz Exp $
 *  IzPack
 *  Copyright (C) 2001 Johannes Lehtinen
 *
 *  File :               Pack.java
 *  Description :        Contains informations about a parsable file.
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
package com.izforge.izpack;

import java.io.Serializable;
import java.util.List;

/**
 *  Encloses information about a parsable file. This class abstracts the way the
 *  information is stored to package.
 *
 * @author     Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
public class ParsableFile implements Serializable
{

  /**  The file path */
  public String path = null;

  /**  The file type (or null for default) */
  public String type = null;

  /**  The file encoding (or null for default) */
  public String encoding = null;

  /**  The list of OS constraints limiting file installation. */
  public List osConstraints = null;

  /**
   *  Constructs and initializes a new instance.
   *
   * @param  path      the file path
   * @param  type      the file type (or null for default)
   * @param  encoding  the file encoding (or null for default)
   * @param  os        the OS constraint (or null for any OS)
   */
  public ParsableFile(String path, String type, String encoding, List osConstraints)
  {
    this.path = path;
    this.type = type;
    this.encoding = encoding;
    this.osConstraints = osConstraints;
  }

}

