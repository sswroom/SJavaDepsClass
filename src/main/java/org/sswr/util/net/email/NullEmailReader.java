package org.sswr.util.net.email;

import javax.mail.Message;

public class NullEmailReader implements EmailReader
{

    @Override
    public boolean open() {
        return false;
    }

    @Override
    public boolean openFolder(String folderName) {
        return false;
    }

    @Override
    public void closeFolder() {
    }

    @Override
    public void close() {
    }

    @Override
    public Message[] getMessages() {
        return null;
    }
}
