/*
 * $Id: PasswordGroup.java,v 1.3 2003/09/10 13:49:50 jpz Exp $
 * Copyright (C) 2003 Elmar Grom
 *
 * File :               PasswordGroup.java
 * Description :        This class supports handling of multiple
 *                      related password fields. The primary use
 *                      is in the UserInputPanel.
 * Author's email :     elmar@grom.net
 * Author's Website :   http://www.izforge.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package   com.izforge.izpack.panels;

import java.util.Vector;

import javax.swing.JPasswordField;
  
/*---------------------------------------------------------------------------*/
/**
 * This class can be used to manage multiple related password fields. This is
 * used in the <code>UserInputPanel</code> to manage communication with the
 * validator and processor for password fields.
 *
 * @see      com.izforge.izpack.panels.UserInputPanel
 *
 * @version  0.0.1 / 2/22/03
 * @author   Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class PasswordGroup implements ProcessingClient
{
  // ------------------------------------------------------------------------
  // Variable Declarations
  // ------------------------------------------------------------------------
  private Vector      fields    = new Vector ();
  private Validator   validator = null;
  private Processor   processor = null;
  
 /*--------------------------------------------------------------------------*/
 /**
  * Creates a passowrd group to manage one or more password fields.
  *
  * @param     validator  A string that specifies a class that provides a
  *                       password validation service. The class must
  *                       implement the <code>Validator</code> interface. If
  *                       an attempt to instantiate this class fails, no
  *                       validation will be performed.
  * @param     processor  A string that specifies a class that provides a
  *                       password processing service, such as password
  *                       encryption. The class must implement the
  *                       <code>Processor</code> interface. If an attempt to
  *                       instantiate this class fails, no processing will
  *                       be performed. Insted the contents of the first
  *                       field will be returned.
  */
 /*--------------------------------------------------------------------------*/
  public PasswordGroup (String validator,
                        String processor)
  {
    // ----------------------------------------------------
    // attempt to create an instance of the Validator
    // ----------------------------------------------------
    try
    {
      this.validator = (Validator)Class.forName (validator).newInstance ();
    }
    catch (Throwable exception)
    {
      this.validator = null;
    }

    // ----------------------------------------------------
    // attempt to create an instance of the Processor
    // ----------------------------------------------------
    try
    {
      this.processor = (Processor)Class.forName (processor).newInstance ();
    }
    catch (Throwable exception)
    {
      this.processor = null;                   
    }
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Returns the number of sub-fields.
  *
  * @return    the number of sub-fields
  */
 /*--------------------------------------------------------------------------*/
  public int getNumFields ()
  {
    return (fields.size ());
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Returns the contents of the field indicated by <code>index</code>.
  *
  * @param     index  the index of the sub-field from which the contents
  *                   is requested.
  *
  * @return    the contents of the indicated sub-field.
  *
  * @exception IndexOutOfBoundsException if the index is out of bounds.
  */
 /*--------------------------------------------------------------------------*/
  public String getFieldContents (int index) throws IndexOutOfBoundsException
  {
    if ((index < 0) || (index >= fields.size ()))
    {
      throw (new IndexOutOfBoundsException ());
    }
    
    String contents = new String (((JPasswordField)fields.elementAt (index)).getPassword ());
    return (contents);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Adds a <code>JPasswordField</code> to the group of fields being managed
  * by this object.
  *
  * @param     the <code>JPasswordField</code> to add
  */
 /*--------------------------------------------------------------------------*/
  public void addField (JPasswordField field)
  {
    if (field != null)
    {
      fields.add (field);
    }
  }
 /*--------------------------------------------------------------------------*/
 /**
  * This method validates the group content. Validating is performed through
  * a user supplied service class that provides the validation rules.
  *
  * @return    <code>true</code> if the validation passes or no implementation
  *            of a validation rule exists. Otherwise <code>false</code> is
  *            returned.
  */
 /*--------------------------------------------------------------------------*/
  public boolean validateContents ()
  {
    if (validator != null)
    {
      return (validator.validate (this));
    }
    else
    {
      return (true);
    }
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Returns the password. If a processing service class was supplied it will
  * be used to process the password before it is returned, otherwise the
  * content of the first field will be returned.
  *
  * @return    the password
  */
 /*--------------------------------------------------------------------------*/
  public String getPassword ()
  {
    if (processor != null)
    {
      return (processor.process (this));
    }
    else
    {
      String contents = "";
      
      if (fields.size () > 0)
      {
        contents = new String (((JPasswordField)fields.elementAt (0)).getPassword ());
      }

      return (contents);
    }
  }
}
/*---------------------------------------------------------------------------*/
