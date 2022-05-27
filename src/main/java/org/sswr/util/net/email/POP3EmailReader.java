package org.sswr.util.net.email;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;

public class POP3EmailReader
{
	private Properties props;
	private Authenticator auth;
	private String serverHost;
	private String username;
	private String password;

	private Session session;
	private Folder inbox;
	private Store store;

	public POP3EmailReader(String serverHost, int port, boolean ssl, String username, String password)
	{
		this.serverHost = serverHost;
		this.username = username;
		this.password = password;
		this.props = new Properties();
		this.props.setProperty("mail.debug", "false");
        this.props.put("mail.pop3.port", String.valueOf(port));
        this.props.put("mail.pop3.host", serverHost);
        this.props.put("mail.pop3.user", username);
        this.props.put("mail.store.protocol", "pop3");
		if (ssl)
		{
			this.props.put("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			this.props.put("mail.pop3.socketFactory.fallback", "false");
			this.props.put("mail.pop3.socketFactory.port", String.valueOf(port));
			this.props.put("mail.pop3.ssl.protocols", "TLSv1.2");
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
		this.session = Session.getInstance(this.props, this.auth);
		try
		{
			this.store = session.getStore("pop3");
			this.store.connect(this.serverHost, this.username, this.password);
			this.inbox = store.getFolder("INBOX");
			this.inbox.open(Folder.READ_WRITE);
			return true;
		}
		catch (MessagingException ex)
		{
			ex.printStackTrace();
			return false;
		}
	}

	public void close()
	{
		try
		{
			this.inbox.close(true);
			this.store.close();
		}
		catch (MessagingException ex)
		{
			ex.printStackTrace();
		}
		this.inbox = null;
		this.store = null;
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
