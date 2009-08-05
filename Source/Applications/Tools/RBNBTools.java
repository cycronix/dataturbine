import java.awt.Menu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;


public class RBNBTools implements ActionListener {
	private MenuHolder mh=null;
	private Menu menu=null;
	private Hashtable<String,Process> servers=new Hashtable<String,Process>();

	public static void main(String[] argv) {
		try {
			new RBNBTools();
		} catch (Throwable e) {
			System.err.println("RBNBTools instantiation exception");
			e.printStackTrace();
		}
	}
		
	public RBNBTools() {
		try {
			System.err.println("attempting to load TrayIconHolder");
			//mh=new TrayIconHolder();
			mh=(MenuHolder)Class.forName("TrayIconHolder").newInstance();
			System.err.println("loaded: "+mh);
		} catch (Throwable e) {
			System.err.println("need java 1.6 features to use system tray,");
			System.err.println("loading FrameHolder instead");
			mh=new FrameHolder();
			System.err.println("loaded: "+mh);
		}
		System.err.println("menu "+mh.getMenu());
		//add items to menu, set up callbacks
		menu=mh.getMenu();
		menu.addActionListener(this);
		menu.add("Start rbnbAdmin");
		menu.add("Start rbnbServer");
		menu.add("Quit");

	}

	public void actionPerformed(ActionEvent e) {
		String action=e.getActionCommand();
		if (action.equals("Quit")) System.exit(0);
		else if (action.equals("Start rbnbAdmin")) startAdmin();
		else if (action.equals("Start rbnbServer")) startServer();
		else if (action.startsWith("Kill rbnbServer")) {
			//pull process from hashtable, kill it
			String name=action.substring(action.lastIndexOf(" ")+1);
			Process p=servers.get(name);
			if (p!=null) p.destroy();
			else System.err.println("server process not found!");
			//remove from menu
			for (int i=0;i<menu.getItemCount();i++) {
				if (menu.getItem(i).getLabel().equals(action)) {
					menu.remove(i);
					break;
				}
			}
		}
	}

	private void startAdmin() {
		System.err.println("starting rbnbAdmin process");
		try {
			Process adminP=Runtime.getRuntime().exec("java -jar admin.jar");
			System.err.println("admin process "+adminP);
		} catch (Exception e) {
			System.err.println("failed to start rbnbAdmin");
			e.printStackTrace();
		}
	}

	private void startServer() {
		//run in separate Frame, to capture and display stout and sterr
		try {
			String serverName=javax.swing.JOptionPane.showInputDialog("rbnbServer name","myserver"+(menu.getItemCount()-2));
			while (servers.containsKey(serverName)) serverName=javax.swing.JOptionPane.showInputDialog("rbnbServer "+serverName+" already running, try again","myserver"+(menu.getItemCount()-2));
			Process serverP=Runtime.getRuntime().exec("java -jar rbnb.jar -n "+serverName);
			System.err.println("server process "+serverP);
			servers.put(serverName,serverP);
			menu.insert("Kill rbnbServer "+serverName,menu.getItemCount()-1);
		} catch (Exception e) {
			System.err.println("failed to start rbnbServer");
			e.printStackTrace();
		}
	}
	
}