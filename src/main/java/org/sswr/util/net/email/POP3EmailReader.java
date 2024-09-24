package org.sswr.util.net.email;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;

import org.sswr.util.net.SSLEngine;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class POP3EmailReader implements EmailReader
{
	public enum ConnType
	{
		PLAIN,
		STARTTLS,
		SSL
	}
	private Properties props;
	private Authenticator auth;
	private String serverHost;
	private String username;
	private String password;

	private Session session;
	private Folder inbox;
	private Store store;

	public POP3EmailReader(@Nonnull String serverHost, int port, @Nonnull ConnType connType, @Nullable SSLEngine ssl, @Nonnull String username, @Nonnull String password)
	{
		this.serverHost = serverHost;
		this.username = username;
		this.password = password;
		this.props = new Properties();
		this.props.setProperty("mail.debug", "false");
        this.props.put("mail.pop3.port", String.valueOf(port));
        this.props.put("mail.pop3.host", serverHost);
        this.props.put("mail.pop3.user", username);
		if (ssl != null)
			this.props.put("mail.pop3.ssl.socketFactory", ssl.getSocketFactory());
		else
			this.props.put("mail.pop3.ssl.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		this.props.put("mail.pop3.socketFactory.fallback", "false");
		this.props.put("mail.pop3.socketFactory.port", String.valueOf(port));
		this.props.put("mail.pop3.ssl.protocols", "TLSv1.2");
		if (connType == ConnType.SSL)
		{
	        this.props.put("mail.store.protocol", "pop3s");
			this.props.put("mail.pop3.ssl.enable", true);
		}
		else if (connType == ConnType.STARTTLS)
		{
	        this.props.put("mail.store.protocol", "pop3");
			this.props.put("mail.pop3.starttls.required", true);
			this.props.put("mail.pop3.starttls.enable", true);
		}
		else
		{
	        this.props.put("mail.store.protocol", "pop3");
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

	public void setSSLProtocols(@Nonnull String sslProtocols)
	{
		this.props.put("mail.pop3.ssl.protocols", sslProtocols);
	}

	public boolean open()
	{
		this.session = Session.getInstance(this.props, this.auth);
		try
		{
			this.store = session.getStore("pop3");
			this.store.connect(this.serverHost, this.username, this.password);
			this.inbox = null;
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
		if (this.inbox != null)
			return true;
		try
		{
			this.inbox = this.store.getFolder(folderName);
			this.inbox.open(Folder.READ_WRITE);
			return true;
		}
		catch (MessagingException ex)
		{
			ex.printStackTrace();
			this.inbox = null;
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
		try
		{
			this.store.close();
		}
		catch (MessagingException ex)
		{
			ex.printStackTrace();
		}
		this.inbox = null;
		this.store = null;
	}

	@Nullable
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
		catch (IllegalStateException ex)
		{
			ex.printStackTrace();
			try
			{
				this.inbox.open(Folder.READ_WRITE);
			}
			catch (MessagingException ex2)
			{
				ex2.printStackTrace();
				return null;
			}
			return null;
		}
		catch (MessagingException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
}
