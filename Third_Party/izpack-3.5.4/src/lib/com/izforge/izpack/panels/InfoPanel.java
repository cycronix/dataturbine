/*
 *  $Id: InfoPanel.java,v 1.13 2004/02/20 09:01:39 jponge Exp $
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
 *
 *  File :               InfoPanel.java
 *  Description :        A panel to show some textual information.
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
package com.izforge.izpack.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.ResourceManager;

/**
 *  The info panel class. Displays some raw-text informations.
 *
 * @author     Julien Ponge
 */
public class InfoPanel extends IzPanel
{
  /**  The layout. */
  private GridBagLayout layout;

  /**  The layout constraints. */
  private GridBagConstraints gbConstraints;

  /**  The info label. */
  private JLabel infoLabel;

  /**  The text area. */
  private JTextArea textArea;

  /**  The scrolling container. */
  private JScrollPane scroller;

  /**  The info string. */
  private String info;

  /**
   *  The constructor.
   *
   * @param  parent  The parent window.
   * @param  idata   The installation data.
   */
  public InfoPanel(InstallerFrame parent, InstallData idata)
  {
    super(parent, idata);

    // We initialize our layout
    layout = new GridBagLayout();
    gbConstraints = new GridBagConstraints();
    setLayout(layout);

    // We load the text
    loadInfo();

    // We add the components

    infoLabel =
      new JLabel(
        parent.langpack.getString("InfoPanel.info"),
        parent.icons.getImageIcon("edit"),
        JLabel.TRAILING);
    parent.buildConstraints(gbConstraints, 0, 0, 1, 1, 1.0, 0.1);
    gbConstraints.insets = new Insets(5, 5, 5, 5);
    gbConstraints.fill = GridBagConstraints.NONE;
    gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
    layout.addLayoutComponent(infoLabel, gbConstraints);
    add(infoLabel);

    textArea = new JTextArea(info);
    textArea.setCaretPosition(0);
    textArea.setEditable(false);
    scroller = new JScrollPane(textArea);
    parent.buildConstraints(gbConstraints, 0, 1, 1, 1, 1.0, 0.9);
    gbConstraints.fill = GridBagConstraints.BOTH;
    gbConstraints.anchor = GridBagConstraints.CENTER;
    layout.addLayoutComponent(scroller, gbConstraints);
    add(scroller);
  }

  /**  Loads the info text.  */
  private void loadInfo()
  {
    try
    {
      String resNamePrifix = "InfoPanel.info";
      info = ResourceManager.getInstance().getTextResource(resNamePrifix);
    } catch (Exception err)
    {
      info = "Error : could not load the info text !";
    }
  }

  /**
   *  Indicates wether the panel has been validated or not.
   *
   * @return    Always true.
   */
  public boolean isValidated()
  {
    return true;
  }
}
