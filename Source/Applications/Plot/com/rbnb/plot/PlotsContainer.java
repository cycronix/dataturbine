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

// PlotsContainer.java
// Copyright 2000 Creare Incorporated

//EMF 11/8/00: added Environment parameter to constructor, to avoid 
//             collisions between multiple instances of rbnbPlot running
//             in the same JVM

//EMF 10/15/04: added new PlotContainerImage, if channel is JPEG or PNG
//              Need to deal with Table mode and Config save/load.

// JPW 04/07/2005: Convert to Swing

// EMF: added new PlotContainerText

// EMF 8/19/05: switched plot displays to JInternalFrames

package com.rbnb.plot;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
// import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
// import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener; //to catch resize events
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

// public class PlotsContainer extends Container implements ActionListener {
public class PlotsContainer extends JComponent implements ActionListener, ComponentListener {
   private RBNBCubby rbnbCubby=null; //for passing changes in display group
   private PosDurCubby posDurCubby=null;
   private Dimension size;
   //private int dg=0;
   private LWContainer buttons=null;
   private LWContainer top=null;
   private TableContainer table=null;
   private boolean tableIn=false;
   private JButton[] button=null;
   private LWContainer card = null; //cardlayout outer display
   //private LWContainer[] group = null; //display groups
   private JDesktopPane[] group = null; //display groups
   private CardLayout tf = null; //poor man's tabbed folders
   private int displayMode=LayoutCubby.PlotMode;
   private int displayGroup=0;
   private int nextFrameX=0;
   private int nextFrameY=0;
   private int frameDistance=0;
   private Hashtable[] pc2jif;
   //EMF 11/8/00
   private Environment environment=null;
   private boolean auto=true; //auto layout after changes (resize, add, remove)
	
   public PlotsContainer(RBNBCubby rbc,PosDurCubby pdc, Environment e) {
        
        //EMF 4/10/07: use Environment fonts so size can be changed
        // JPW 04/12/2005: Set the font
	//setFont(new Font("Dialog", Font.PLAIN, 12));
	setFont(Environment.FONT12);
        
	rbnbCubby=rbc;
	posDurCubby=pdc;
        environment=e;

        //EMF 3/27/02: set SCROLLGRIDLINES to true; may have set by GUI later
	//EMF 1/13/04: now set by command line argument g, do not reset here
        //e.SCROLLGRIDLINES=false;

        button=new JButton[environment.DISPLAYGROUPS];
        //group=new LWContainer[environment.DISPLAYGROUPS];
        group=new JDesktopPane[environment.DISPLAYGROUPS];
	setLayout(new BorderLayout());
	top=new LWContainer();
	top.setLayout(new GridLayout(0,1));
	buttons = new LWContainer();
	buttons.setLayout(new GridLayout(1,0));	//single row, multiple columns
	for (int i=0;i<environment.DISPLAYGROUPS;i++) {
	   button[i]=new JButton(Integer.toString(i+1));
	   button[i].setFont(Environment.FONT10);
	   button[i].setBackground(Environment.BGCOLOR);
	   button[i].addActionListener(this);
	   buttons.add(button[i]);
	   }
	button[0].setBackground(Environment.BGCOLOR.darker());
	top.add(buttons);
	table=new TableContainer();
	add(top,BorderLayout.NORTH);
	//add(buttons,"North");
	card=new LWContainer();
	tf=new CardLayout();
	card.setLayout(tf);
	pc2jif=new Hashtable[environment.DISPLAYGROUPS];
	for (int i=0;i<environment.DISPLAYGROUPS;i++) {
	   //group[i]=new LWContainer();
	   group[i]=new JDesktopPane();
	   group[i].setBackground(e.BGCOLOR);
	   group[i].addComponentListener(this);
	   pc2jif[i]=new Hashtable();
	   //group[i].setLayout(new GridLayout(0,1)); //plots in single column, multiple rows
	   card.add(group[i],Integer.toString(i));
	   }
	add(card,BorderLayout.CENTER);
	tf.show(card,"0");
	}
	
	//EMF 8/23/05: ComponentListener methods, to catch resize events
	public void componentHidden(ComponentEvent e) {
		//System.err.println("PlotsContainer.componentHidden: event "+e);
	}
	
	public void componentMoved(ComponentEvent e) {
		//System.err.println("PlotsContainer.componentMoved: event "+e);
	}
	
	public void componentResized(ComponentEvent e) {
		//System.err.println("PlotsContainer.componentResized: event "+e);
		if (displayMode==LayoutCubby.TableMode) tile(true);
		else if (auto) tile(false);
	}
	
	public void componentShown(ComponentEvent e) {
		//System.err.println("PlotsContainer.componentShown: event "+e);
	}
		
	//EMF 8/23/05: callback from auto menu item, sets state
	public void setAuto(boolean autoI) {
		auto=autoI;
	}
	
	//EMF 8/19/05: callback from cascade menu item
	// code based on Horstmann, _Core Java 2_
	public void cascade() {
		if (displayMode==LayoutCubby.TableMode) {
			tile(true);
			return;
		}
		JInternalFrame[] frames=group[displayGroup].getAllFrames();
		int x = 0;
		  int y = 0;
		  int width = group[displayGroup].getWidth() / 2;
		  int height = group[displayGroup].getHeight() / 2;
	
		  for (int i = 0; i < frames.length; i++)
		  {  if (true) //(!frames[i].isIcon())
			 {  try
				{  /* try to make maximized frames resizable
					  this might be vetoed
				   */
				   frames[i].setMaximum(false);
				   frames[i].setIcon(false);
				   frames[i].reshape(x, y, width, height);
				   frames[i].moveToFront();
	
				   x += frameDistance;
				   y += frameDistance;
				   // wrap around at the desktop edge
				   if (x + width > group[displayGroup].getWidth()) x = 0;
				   if (y + height > group[displayGroup].getHeight()) y = 0;
				}
				catch(java.beans.PropertyVetoException e)
				{}
			 }
		  }

	}
	
	//EMF 8/19/05: callback from tile menu item
	// code based on Horstmann, _Core Java 2_
	public void tile(boolean singleColumn) {
		if (displayMode==LayoutCubby.TableMode) singleColumn=true;
		JInternalFrame[] frames = group[displayGroup].getAllFrames();
		
		// count frames that aren't iconized
		int frameCount = 0;
		frameCount=frames.length;
		if (frameCount==0) return;
		//for (int i = 0; i < frames.length; i++)
		//{  if (!frames[i].isIcon())
			//frameCount++;
		//}
		
		//note rows and columns switched to favor wider/shorter windows
		int rows = (int)Math.sqrt(frameCount);
		int cols = frameCount / rows;
		int extra = frameCount % rows;
		 // number of columns with an extra row
		 
		 if (singleColumn) {
			 rows=1;
			 cols=frameCount;
			 extra=0;
		 }
		
		int width = group[displayGroup].getHeight() / cols;
		int height = group[displayGroup].getWidth() / rows;
		int r = 0;
		int c = 0;
		//for (int i = 0; i < frames.length; i++)
		for (int i=frames.length-1; i>=0; i--)
		   {  if (true) //(!frames[i].isIcon())
			 {  try
				{  frames[i].setMaximum(false);
				   frames[i].setIcon(false);
				   frames[i].reshape(r * height,c * width,
				   height,width);
				   r++;
				   if (r == rows)
				   {  r = 0;
					  c++;
					  if (c == cols - extra)
					  {  // start adding an extra row
						 rows++;
						 height = group[displayGroup].getWidth() / rows;
					  }
				   }
				}
				catch(java.beans.PropertyVetoException e)
				{}
			 }
		   }
		}
	
	//getConfig method - adds relevent key/value pairs to provided hashtable
	public synchronized void getConfig(Hashtable ht) {
		ht.put("mode",String.valueOf(displayMode));
		ht.put("dg.num",String.valueOf(environment.DISPLAYGROUPS));
		ht.put("dg.current",String.valueOf(displayGroup));
		for (int i=0;i<environment.DISPLAYGROUPS;i++) {
			int num=group[i].getComponentCount();
			ht.put("dg["+i+"].chans",String.valueOf(num));
			if (num>0) {
				int j=0;
				Enumeration e=pc2jif[i].keys();
				while (e.hasMoreElements()) {
					((PlotContainer)e.nextElement()).getConfig(ht,"dg["+i+"]["+j+"].");
					j++;
				}
			}
		}
	}
	
	//setConfig method - extracts and applies configuration information from hashtable
	public synchronized void setConfig(Hashtable ht) {
		//System.out.println("PlotsContainer.setConfig: "+ht.toString());
		for (int i=0;i<environment.DISPLAYGROUPS;i++) {
			int num=group[i].getComponentCount();
			if (num>0) {
				int j=0;
				Enumeration e=pc2jif[i].keys();
				while (e.hasMoreElements()) {
					((PlotContainer)e.nextElement()).setConfig(ht,"dg["+i+"]["+j+"].");
					j++;
				}
			}
		}
	}
   
   //actionPerformed method - sets appropriate display group in card layout
   public void actionPerformed(ActionEvent e) {
		JButton hit = (JButton)e.getSource();
		for (int i=0;i<button.length;i++) {
			if (button[i].equals(hit)) setDisplayGroup(i);
			}
		}
	
	//setDisplayGroup method
	public void setDisplayGroup(int i) {
		for (int j=0;j<environment.DISPLAYGROUPS;j++) button[j].setBackground(Environment.BGCOLOR);
		button[i].setBackground(Environment.BGCOLOR.darker());
		rbnbCubby.setGroup(i);
		displayGroup=i;
		tf.show(card,(new Integer(i)).toString());
		}
	
	//setMode method - sets display mode
	public void setDisplayMode(int dm) {
		displayMode=dm;
		if (displayMode==LayoutCubby.TableMode && !tableIn) {
			top.add(table);
			tableIn=true;
			}
		else if (displayMode!=LayoutCubby.TableMode && tableIn) {
			top.remove(table);
			tableIn=false;
			}
		for (int i=0;i<pc2jif.length;i++) {
			Enumeration e=pc2jif[i].keys();
			while (e.hasMoreElements())
				((PlotContainer)e.nextElement()).setDisplayMode(displayMode);
			//set display mode in plotcontainers
			//Component[] comp = group[i].getComponents();
			//for (int j=0; j<comp.length; j++) {
			    //((PlotContainer)comp[j]).setDisplayMode(displayMode);
			    //}
			//set number of columns in plotscontainer
			//GridLayout gl=(GridLayout)group[i].getLayout();
			//int numCol=gl.getColumns();
			// For Table mode, numColumns() returns 1
			//int reqCol=numColumns(group[i].getComponentCount());
			//if (numCol != reqCol) gl.setColumns(reqCol);
			}
		//EMF 8/23/05: redo layout
		if (dm==LayoutCubby.TableMode) tile(true);
		else tile(false);
        // JPW 7/26/2013:
        //     calling validateTree() was causing an exception to be
        //     thrown when run under Java 1.7; see the following for a
        //     description of the issue:
        //     http://stackoverflow.com/questions/9758069/validatetree-in-java-7-x-doesnt-work-in-java-6-x-was-fine
        //     The fix was to call validate() instead.
        // validateTree();
        validate();
		}
	
	//labelDisplayGroups method - sets labels on buttons
	public void labelDisplayGroups(String[] dgLabel) {
		if (dgLabel.length!=button.length) {
			System.out.println("PlotsContainer.labelDisplayGroups: input array length incorrect!");
			return;
			}
		for (int i=0;i<button.length;i++) {
			if (dgLabel[i]!=null) {
				//EMF 4/10/07: set Environment font, so size can be changed
				button[i].setFont(Environment.FONT10);
				button[i].setText(dgLabel[i]);
				button[i].invalidate();
				}
			}
		validate();
		}
	
	// numColumns method - heuristic to determine the appropriate number of columns to display
	// the specified number of plots
	private int numColumns(int p) {
		if (displayMode==LayoutCubby.TableMode) return 1;
		if (p<=25) return (p-1)/5 + 1;
		else if (p<=36) return (p-1)/6 + 1;
		else if (p<=49) return (p-1)/7 + 1;
		else if (p<=64) return (p-1)/8 + 1;
		else if (p<=81) return (p-1)/9 + 1;
		else return (p-1)/10 + 1;
		}

   //EMF 10/15/04: add PlotContainerImage if appropriate
   //EMF 5/5/08: add MIME type check for starting PlotContainerImage
   //JPW 7/9/08: add check on regChan.getMime() being null before checking if mime is "jpg" or "jpeg"
   public PlotContainer addPlot(RegChannel regChan,int dg) {
	   PlotContainer pc=null;
//System.err.println("PlotsContainer.addPlot: name="+regChan.name+", mime="+regChan.getMime());
	   //key off channel name
	   if ( (regChan.name != null)
	         && (regChan.name.toLowerCase().endsWith("jpg")
	   	    || regChan.name.toLowerCase().endsWith("jpeg")
		    || ( (regChan.getMime() != null)
			  && (regChan.mime.toLowerCase().endsWith("jpg")
			      || regChan.mime.toLowerCase().endsWith("jpeg")) ) ) )
	   {
		pc = (PlotContainer) (new PlotContainerImage(regChan,displayMode,posDurCubby,environment));
	   }
	   else if ( (regChan.name != null)
	   	      && ( (regChan.name.toLowerCase().endsWith("txt") || regChan.name.toLowerCase().endsWith("text"))
                           ||(regChan.getMime()!=null && regChan.getMime().toLowerCase().startsWith("text"))))
	   {
		pc = (PlotContainer) (new PlotContainerText(regChan,displayMode,posDurCubby,environment));
	   }
	   else
	   {
		pc = new PlotContainer(regChan,displayMode,posDurCubby,environment);
	   }
		//int n=numColumns(group[dg].getComponentCount()+1);
		//GridLayout gl=(GridLayout)group[dg].getLayout();
		//if (n != gl.getColumns()) gl.setColumns(n);
        //group[dg].add(pc);
//EMF 11/10/05: just use channel name, skip server/source
String shortname=regChan.name;
shortname=shortname.substring(shortname.lastIndexOf('/')+1);
//EMF 4/10/07: set Environment font so size can be changed
javax.swing.UIManager.put("InternalFrame.titleFont",Environment.FONT12);
	    JInternalFrame jif=new JInternalFrame(shortname,true,false,true,true);
	    //EMF 4/10/07: set Environment font, so size can be changed
            jif.setFont(Environment.FONT10);
	    //JInternalFrame jif=new JInternalFrame(regChan.name,true,false,true,true);
		java.net.URL url=this.getClass().getResource("/images/whirligig.gif");
		if (url!=null) jif.setFrameIcon(new javax.swing.ImageIcon(url));
		jif.getContentPane().add(pc);
		group[dg].add(jif);
		pc2jif[dg].put(pc,jif);
		int width=group[dg].getWidth()/2;
		int height=group[dg].getHeight()/2;
		jif.reshape(nextFrameX,nextFrameY,width,height);
		jif.show();
		//update position info
		if (frameDistance==0) frameDistance=jif.getHeight()-jif.getContentPane().getHeight();
		nextFrameX+=frameDistance;
		nextFrameY+=frameDistance;
		if (nextFrameX + width > group[dg].getWidth())
          nextFrameX = 0;
        if (nextFrameY + height > group[dg].getHeight())
          nextFrameY = 0;
		if (displayMode==LayoutCubby.TableMode) tile(true);
		else if (auto) tile(false);
		invalidate();
		validate();
		repaint();
      return(pc);
   }

   public void removePlot(PlotContainer pc,int dg) {
	    JInternalFrame jif=(JInternalFrame)pc2jif[dg].get(pc);
		try { jif.setIcon(false); } catch (Exception e) {}
		group[dg].remove(jif);
      //int n=numColumns(group[dg].getComponentCount()-1);
		/*int n=numColumns(group[dg].getComponentCount());
		GridLayout gl=(GridLayout)group[dg].getLayout();
		if (n != gl.getColumns()) gl.setColumns(n); */
      //if (group[dg].getComponentCount()==0) {
		//	Dimension size = getSize();
		//	group[dg].getGraphics().clearRect(0,0,size.width,size.height);
		//	}
		if (displayMode==LayoutCubby.TableMode) tile(true);
		else if (auto) tile(false);
		invalidate();
		validate();
		repaint();
		}
   
   public void update(Graphics g) {
      System.out.println("PlotsContainer.update()");
      paint(g);
      }
   
   //EMF 12/15/04: note PlotsContainer need only paint areas not covered by individual plots,
   //              namely the gap at the bottom and right, and any empty spots in the grid
   public void paint(Graphics g) {
		// layout manager does not clear residue from unused areas of available space, so need
		// to clean up manually...Fortunately, Sun normally does better than this.
		Dimension contS=getSize();
		int topH=top.getSize().height;
		Dimension groupS=group[displayGroup].getSize();
		int nComp=group[displayGroup].getComponentCount();
		if (nComp==0) {
			//g.setColor(Color.magenta);
			g.clearRect(0,contS.height-groupS.height-1,groupS.width,groupS.height);
			}
		else { /*
			GridLayout gl=(GridLayout)group[displayGroup].getLayout();
			int nCols=gl.getColumns();
			//rows reported by gl.getRows() as zero, so must infer from nComp and nCol
			int nRows=nComp/nCols;
			if (nComp%nCols>0) nRows++;
			//clear bottom edge
			//g.setColor(Color.red);
			g.clearRect(0,contS.height-groupS.height%nRows,contS.width,groupS.height%nRows);
			//clear right edge
			//g.setColor(Color.green);
			g.clearRect(contS.width-groupS.width%nCols,contS.height-groupS.height,
						  groupS.width%nCols,groupS.height);
			//clear any slots not filled
			int nHoles=nRows*nCols - nComp;
			//g.setColor(Color.blue);
			if (nHoles>0) g.clearRect((contS.width/nCols)*(nCols-nHoles),
												contS.height-(groupS.height%nRows)-(groupS.height/nRows),
												nHoles*(contS.width/nCols),
												groupS.height/nRows);
			*/
			}
		super.paint(g);
   }
   
// cannot create a Container with new, so extend - need multiple containers
// to do nested layout of user controls
// class LWContainer extends Container {
class LWContainer extends JComponent {

   public LWContainer() {
   }
   
   public void paint(Graphics g) {
	if (getComponentCount()==0) { //some systems to not clear screen properly
	   //size=getSize();
	   //g.clearRect(0,0,size.width-1,size.height-1);
	   }
      super.paint(g);
   }
}

// class TableContainer - handles column labels for table mode
// class TableContainer extends Container {
class TableContainer extends JComponent {
	Dimension oldSize=new Dimension(0,0);
	boolean newSize=false;
	Image bufferImage=null;

	public TableContainer() {
		}
	
	//EMF 8/22/05: drop name column, since now in JInternalFrames
	public void paint(Graphics g) {
		Dimension size=getSize();
		if (size.width!=oldSize.width || size.height!=oldSize.height) {
			newSize=true;
			oldSize.width=size.width;
			oldSize.height=size.height;
			}
		if (newSize) {
			bufferImage=createImage(size.width,size.height);
			Graphics bi=bufferImage.getGraphics();
			int block=size.width/7; //8;
			//make alternate columns white
			bi.setColor(Color.white);
			for (int i=1;i<7;i+=2) {
				bi.fillRect(i*block,0,block,size.height);
				}
			//make alternate columns light gray
			bi.setColor(Color.lightGray);
			for (int i=0;i<7;i+=2) {
				bi.fillRect(i*block,0,block,size.height);
				}
			bi.setColor(Color.black);
			bi.setFont(Environment.FONT12B);
			FontMetrics fm=getFontMetrics(Environment.FONT12B);
			int fh=fm.getHeight();
			//bi.drawString("Name",0,fh);
			bi.drawString("Units",0,fh);
			bi.drawString("First",block,fh);
			bi.drawString("Last",2*block,fh);
			bi.drawString("Min",3*block,fh);
			bi.drawString("Max",4*block,fh);
			bi.drawString("Ave",5*block,fh);
			bi.drawString("StdDev",6*block,fh);
			}
		g.drawImage(bufferImage,0,0,null);
		super.paint(g);
		} //end method paint
	} //end class TableContainer
}
