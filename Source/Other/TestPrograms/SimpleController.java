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

import com.rbnb.api.Controller;
import com.rbnb.api.Server;

/**
 * Simple controller application using the RBNB API.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 02/01/2001
 */

/*
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/12/2001  INB	Created.
 *
 */
public class SimpleController extends Thread {
    /**
     * are we running?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/23/2001
     */
     private boolean running = false;

    /**
     * the <code>Server</code> that this source application is attached to.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/12/2001
     */
    private Server server = null;

    /**
     * the name of the <code>Controller</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/12/2001
     */
    private String name = null;

    /**
     * the <code>Controller</code> object that this controller application uses
     * to  communicate with the actual <code>Controller</code> object in the
     * RBNB DataTurbine <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/12/2001
     */
    private Controller controller = null;

    /**
     * Class constructor to build a <code>SimpleController</code> attached to
     * the specified <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI    the name of the <code>Controller</code>.
     * @param serverI  the <code>Server</code>.
     * @see #setControllerName(String)
     * @see #setServer(com.rbnb.api.Server)
     * @since V2.0
     * @version 01/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/12/2001  INB	Created.
     *
     */
    public SimpleController(String nameI,Server serverI) {
	setControllerName(nameI);
	setServer(serverI);
    }

    /**
     * Gets the <code>Controller</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Controller</code>.
     * @see #setController(com.rbnb.api.Controller)
     * @since V2.0
     * @version 01/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/12/2001  INB	Created.
     *
     */
    public final synchronized Controller getController() {
	return (controller);
    }

    /**
     * Gets the name.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the name.
     * @see #setName(String)
     * @since V2.0
     * @version 01/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/12/2001  INB	Created.
     *
     */
    private final synchronized String getControllerName() {
	return (name);
    }

    /**
     * Gets the running flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return are we running?
     * @see #setRunning(boolean)
     * @since V2.0
     * @version 01/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2001  INB	Created.
     *
     */
    private final synchronized boolean getRunning() {
	return (running);
    }

    /**
     * Gets the <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Server</code>.
     * @see #setServer(com.rbnb.api.Server)
     * @since V2.0
     * @version 01/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/12/2001  INB	Created.
     *
     */
    private final synchronized Server getServer() {
	return (server);
    }

    /**
     * Is this <code>SimpleController</code> running?
     * <p>
     *
     * @author Ian Brown
     *
     * @return are we running?
     * @exception java.lang.InterruptedException
     *		  thrown if the check is interrupted.
     * @since V2.0
     * @version 01/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2001  INB	Created.
     *
     */
    public final synchronized boolean isRunning()
	throws java.lang.InterruptedException
    {
	while (isAlive() && !getRunning()) {
	    wait(1000);
	}

	return (getRunning());
    }

    /**
     * Runs the controller.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/12/2001  INB	Created.
     *
     */
    public final void run() {
	try {
	    setController(getServer().createController(getControllerName()));
	    getController().start();

	    // We're now running.
	    setRunning(true);

	    if (getController().isRunning()) {
		getController().stop();
		setController(null);
	    }

	    // We're no longer running.
	    setRunning(false);

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Sets the <code>Controller</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param controllerI  the <code>Controller</code>.
     * @see #getController()
     * @since V2.0
     * @version 02/01/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/12/2001  INB	Created.
     *
     */
    private final synchronized void setController(Controller controllerI) {
	controller = controllerI;
    }

    /**
     * Sets the name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI  the name.
     * @see #getControllerName()
     * @since V2.0
     * @version 01/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/12/2001  INB	Created.
     *
     */
    private final synchronized void setControllerName(String nameI) {
	name = nameI;
    }

    /**
     * Sets the running flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param runningI  are we running?
     * @see #getRunning()
     * @see #isRunning()
     * @since V2.0
     * @version 01/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2001  INB	Created.
     *
     */
    private final synchronized void setRunning(boolean runningI) {
	running = runningI;
	notifyAll();
    }

    /**
     * Sets the <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverI  the <code>Server</code>.
     * @see #getServer()
     * @since V2.0
     * @version 01/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/12/2001  INB	Created.
     *
     */
    private final synchronized void setServer(Server serverI) {
	server = serverI;
    }
}
