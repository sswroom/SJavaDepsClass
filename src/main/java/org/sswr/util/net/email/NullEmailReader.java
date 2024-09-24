package org.sswr.util.net.email;

import javax.mail.Message;

import jakarta.annotation.Nonnull;

public class NullEmailReader implements EmailReader
{

    @Override
    public boolean open() {
        return true;
    }

    @Override
    public boolean openFolder(@Nonnull String folderName) {
        return true;
    }

    @Override
    public void closeFolder() {
    }

    @Override
    public void close() {
    }

    @Override
    public Message[] getMessages() {
        return new Message[0];
    }
}
