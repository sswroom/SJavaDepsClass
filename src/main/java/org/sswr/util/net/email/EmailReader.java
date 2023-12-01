package org.sswr.util.net.email;

import javax.mail.Message;

public interface EmailReader {

    public boolean open();
    public boolean openFolder(String folderName);
    public void closeFolder();
    public void close();
 	public Message[] getMessages();   
}