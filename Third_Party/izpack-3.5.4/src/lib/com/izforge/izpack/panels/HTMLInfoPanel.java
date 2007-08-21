/*
 *  $Id: HTMLInfoPanel.java,v 1.13 2004/02/20 09:01:39 jponge Exp $
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
 *
 *  File :               HTMLInfoPanel.java
 *  Description :        A panel to show some HTML information.
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
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.ResourceManager;

/**
 *  The HTML info panel.
 *
 * @author     Julien Ponge
 */
public class HTMLInfoPanel extends IzPanel implements HyperlinkListener
{
  /**  The layout. */
  private GridBagLayout layout;

  /**  The layout constraints. */
  private GridBagConstraints gbConstraints;

  /**  The info label. */
  private JLabel infoLabel;

  /**  The text area. */
  private JEditorPane textArea;

  /**
   *  The constructor.
   *
   * @param  parent  The parent.
   * @param  idata   The installation data.
   */
  public HTMLInfoPanel(InstallerFrame parent, InstallData idata)
  {
    super(parent, idata);

    // We initialize our layout
    layout = new GridBagLayout();
    gbConstraints = new GridBagConstraints();
    setLayout(layout);

    // We add the components

    infoLabel =
      new JLabel(
        parent.langpack.getString("InfoPanel.info"),
        parent.icons.getImageIcon("edit"),
        JLabel.TRAILING);
    parent.buildConstraints(gbConstraints, 0, 0, 1, 1, 1.0, 0.0);
    gbConstraints.insets = new Insets(5, 5, 5, 5);
    gbConstraints.fill = GridBagConstraints.NONE;
    gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
    layout.addLayoutComponent(infoLabel, gbConstraints);
    add(infoLabel);

    try
    {
      textArea = new JEditorPane();
      textArea.setEditable(false);
      textArea.addHyperlinkListener(this);
      JScrollPane scroller = new JScrollPane(textArea);
      textArea.setPage(loadInfo());
      parent.buildConstraints(gbConstraints, 0, 1, 1, 1, 1.0, 1.0);
      gbConstraints.anchor = GridBagConstraints.CENTER;
      gbConstraints.fill = GridBagConstraints.BOTH;
      layout.addLayoutComponent(scroller, gbConstraints);
      add(scroller);
    } catch (Exception err)
    {
      err.printStackTrace();
    }
  }

  /**
   *  Loads the info.
   *
   * @return    The info URL.
   */
  private URL loadInfo()
  {
    String resNamePrifix = "HTMLInfoPanel.info";
    try
    {
      return ResourceManager.getInstance().getURL(resNamePrifix);
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return null;
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

  /**
   *  Hyperlink events handler.
   *
   * @param  e  The event.
   */
  public void hyperlinkUpdate(HyperlinkEvent e)
  {
    try
    {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
        textArea.setPage(e.getURL());
    } catch (Exception err)
    {
    }
  }
}
