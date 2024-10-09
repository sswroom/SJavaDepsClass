package org.sswr.util.net.email;

import jakarta.annotation.Nonnull;
import jakarta.mail.Message;

public interface EmailReader {

    public boolean open();
    public boolean openFolder(@Nonnull String folderName);
    public void closeFolder();
    public void close();
 	public Message[] getMessages();
}