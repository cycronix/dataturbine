
import java.awt.Frame;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FrameHolder extends WindowAdapter implements MenuHolder
{
	private Frame frame;
	private Menu m;

	public FrameHolder()
	{
		Image im=Toolkit.getDefaultToolkit().getImage("Creare.gif");
		frame=new Frame("rbnbTools by Creare");
		frame.addWindowListener(this);
		frame.setIconImage(im);
		MenuBar mb=new MenuBar();
		m=new Menu("rbnbTools");
		mb.add(m);
		frame.setMenuBar(mb);
		frame.setSize(new java.awt.Dimension(250,55));
		frame.setVisible(true);
	}

	public Menu getMenu() {
		return m;
	}

	public void windowClosing(WindowEvent e) {
		System.exit(0);
	}

}
