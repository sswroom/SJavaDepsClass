package org.sswr.util.net.email;

import javax.mail.Message;

import jakarta.annotation.Nonnull;

public interface EmailReader {

    public boolean open();
    public boolean openFolder(@Nonnull String folderName);
    public void closeFolder();
    public void close();
 	public Message[] getMessages();
}