
/**
 * Summary description for Class1.
 */
import java.awt.Image;
import java.awt.Menu;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

public class TrayIconHolder implements ActionListener, MenuHolder
{
	private PopupMenu pm;
	private TrayIcon ti;

	public TrayIconHolder()
	{
		URL url=TrayIconHolder.class.getResource("Creare.gif");
		System.err.println("image url: "+url);
		Image im=(new javax.swing.ImageIcon(url)).getImage();
		//Image im=Toolkit.getDefaultToolkit().getImage("Creare.gif");
		pm=new PopupMenu();
		ti=new TrayIcon(im,"RBNB Tools",pm);
		ti.setToolTip("RBNBTools by Creare");
		ti.addActionListener(this);
		SystemTray st=SystemTray.getSystemTray();
		try {
			st.add(ti);
		} catch (Exception e) {
			System.err.println("Failed to attach to system tray, aborting.");
			e.printStackTrace();
		}
	}

	public Menu getMenu() {
		return pm;
	}

	public void actionPerformed(ActionEvent e) {
		System.err.println("TrayIconHolder action event: "+e);
		ti.displayMessage("RBNBTools by Creare","Right click tray icon for menu options.",TrayIcon.MessageType.INFO);
	}
}
