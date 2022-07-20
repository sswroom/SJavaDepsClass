package org.sswr.util.net.email;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.sswr.util.data.StringUtil;
import org.sswr.util.io.LogTool;

public class SMTPEmailControl implements EmailControl
{
	private Properties props;
	private Authenticator auth;
	private String username;
	private String password;
	private String smtpFrom;
	private LogTool logger;

	public SMTPEmailControl(String smtpHost, Integer smtpPort, boolean tls, String username, String password, String smtpFrom, LogTool logger)
	{
		this.props = new Properties();
		if (tls)
		{
			this.props.put("mail.smtp.starttls.enable", "true");
			this.props.put("mail.smtp.ssl.trust", smtpHost);
		}
		this.props.put("mail.smtp.host", smtpHost);
		this.props.put("mail.smtp.port", (smtpPort == null)?(""+getDefaultPort()):(""+smtpPort));
		this.username = username;
		this.password = password;
		this.smtpFrom = smtpFrom;
		this.logger = logger;

		if (this.username != null && this.username.length() > 0 && this.password != null && this.password.length() > 0)
		{
			this.props.put("mail.smtp.auth", "true");
			auth = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			};
		}
	}

	public boolean sendMail(EmailMessage msg, String toList, String ccList)
	{
		Session session;
		if (this.auth != null)
		{
			session = Session.getInstance(this.props, this.auth);
		}
		else
		{
			session = Session.getInstance(this.props);
		}
		try
		{
			MimeMessage message = new MimeMessage(session);
			message.setSubject(msg.getSubject());
			message.setContent(msg.getContent(), "text/html; charset=utf-8");
			message.setSentDate(new Date(System.currentTimeMillis()));
			message.setFrom(new InternetAddress(smtpFrom));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toList));
			if (ccList != null && ccList.length() > 0)
			{
				message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccList));
			}
			Transport.send(message);
			return true;
		}
		catch (Exception ex)
		{
			if (this.logger != null)
			{
				this.logger.logException(ex);
			}
			else
			{
				ex.printStackTrace();
			}
			return false;
		}
	}

	public boolean isServerOnline()
	{
		Session session;
		if (this.auth != null)
		{
			session = Session.getInstance(this.props, this.auth);
		}
		else
		{
			session = Session.getInstance(this.props);
		}
		try
		{
			Transport transport = session.getTransport("smtp");
			transport.connect();
			transport.close();
			return true;
		}
		catch (Exception ex)
		{
			if (this.logger != null)
			{
				this.logger.logException(ex);
			}
			else
			{
				ex.printStackTrace();
			}
			return false;
		}
	}

	public boolean validateDestAddr(String addr)
	{
		return StringUtil.isEmailAddress(addr);
	}

	public String sendTestingEmail(String toAddress)
	{
		Session session;
		if (this.auth != null)
		{
			session = Session.getInstance(this.props, this.auth);
		}
		else
		{
			session = Session.getInstance(this.props);
		}
		try
		{
			MimeMessage message = new MimeMessage(session);
			message.setSubject("Email Testing");
			message.setContent("This is a test email", "text/html; charset=utf-8");
			message.setSentDate(new Date(System.currentTimeMillis()));
			message.setFrom(new InternetAddress(smtpFrom));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
			Transport.send(message);
			return "Sent";
		}
		catch (Exception ex)
		{
			StringWriter writer = new StringWriter();
			ex.printStackTrace(new PrintWriter(writer));
			return writer.toString();
		}
	}

	public static int getDefaultPort()
	{
		return 25;
	}
}
