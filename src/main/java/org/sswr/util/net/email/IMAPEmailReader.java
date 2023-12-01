package org.sswr.util.net.email;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;

public class IMAPEmailReader implements EmailReader
{
	private Properties props;
	private Authenticator auth;
	private String serverHost;
	private String username;
	private String password;

	private Session session;
	private Folder inbox;
	private Store store;

	public IMAPEmailReader(String serverHost, int port, boolean ssl, String username, String password)
	{
		this.serverHost = serverHost;
		this.username = username;
		this.password = password;
		this.props = new Properties();
		this.props.setProperty("mail.debug", "false");
        this.props.put("mail.imap.port", String.valueOf(port));
        this.props.put("mail.imap.host", serverHost);
        this.props.put("mail.imap.user", username);
        this.props.put("mail.store.protocol", "imap");
		if (ssl)
		{
			this.props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			this.props.put("mail.imap.socketFactory.fallback", "false");
			this.props.put("mail.imap.socketFactory.port", String.valueOf(port));
			this.props.put("mail.imap.ssl.protocols", "TLSv1.2");
			this.props.put("mail.imap.ssl.enable", "true");
		}
		if (username != null && username.length() > 0 && password != null && password.length() > 0)
		{
			auth = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			};
		}
	}
	
	public boolean open()
	{
		if (this.store != null) return true;
		this.session = Session.getInstance(this.props, this.auth);
		try
		{
			this.store = session.getStore("imap");
			this.store.connect(this.serverHost, this.username, this.password);
			return true;
		}
		catch (MessagingException ex)
		{
			ex.printStackTrace();
			return false;
		}

	}

	public boolean openFolder(String folderName)
	{
		if (this.store == null)
		{
			if (!this.open())
				return false;
		}
		if (this.inbox != null)
		{
			return false;
		}
		try
		{
			this.inbox = this.store.getFolder(folderName);
			this.inbox.open(Folder.READ_WRITE);
			return true;
		}
		catch (MessagingException ex)
		{
			ex.printStackTrace();
			return false;
		}
	}

	public void closeFolder()
	{
		if (this.inbox != null)
		{
			try
			{
				this.inbox.close(true);
			}
			catch (MessagingException ex)
			{
				ex.printStackTrace();
			}
			this.inbox = null;
		}
	}

	public void close()
	{
		this.closeFolder();
		if (this.store != null)
		{
			try
			{
				this.store.close();
			}
			catch (MessagingException ex)
			{
				ex.printStackTrace();
			}
			this.store = null;
		}
	}

	public Message[] getMessages()
	{
		if (this.inbox == null)
		{
			return null;
		}
		try
		{
			return this.inbox.getMessages();
		}
		catch (MessagingException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
}
