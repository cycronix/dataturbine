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

package com.rbnb.chat;

/**
 * Chat room <code>Client</code> message receiver interface.
 * <p>
 * Classes implementing this interface can receive messages from a chat room
 * via the method <code>receive</code> or the <code>refresh<code> method.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.chat.Client
 * @since V2.0
 * @version 03/19/2003
 */

/*
 * Copyright 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 03/19/2003  INB	Added <code>abort</code> method.
 * 01/08/2003  INB	Created.
 *
 */
public interface ClientReceiverInterface {

    /**
     * Receives notification of an abort by the host side.
     * <p>
     * This method is called whenever a chat <code>Client</code> detects a
     * fatal error when trying to receive messages from the server.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/19/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/19/2003  INB	Created.
     *
     */
    public abstract void abort();

    /**
     * Receives a chat message.
     * <p>
     * This method is called whenever a chat <code>Client</code> receives a
     * message from the chat room <code>Host</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI  the <code>Client</code> that received the message.
     * @param messageI the message <code>ChannelMap</code>.
     * @since V2.0
     * @version 01/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2003  INB	Created.
     *
     */
    public abstract void receive(Client clientI,
				 com.rbnb.sapi.ChannelMap messageI);

    /**
     * Receives a refreshed chat message.
     * <p>
     * This method is called whenever a chat <code>Client</code> receives a
     * message from the chat room <code>Host</code> when refreshing.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI  the <code>Client</code> that received the message.
     * @param messageI the message <code>ChannelMap</code>.
     * @since V2.0
     * @version 01/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2003  INB	Created.
     *
     */
    public abstract void refresh(Client clientI,
				 com.rbnb.sapi.ChannelMap messageI);
}
