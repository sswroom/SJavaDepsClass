package org.sswr.util.net.email;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.sswr.util.data.DateTimeUtil;
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

	public SMTPEmailControl(@Nonnull String smtpHost, @Nullable Integer smtpPort, boolean tls, @Nullable String username, @Nullable String password, @Nonnull String smtpFrom, @Nullable LogTool logger)
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

	public boolean sendMail(@Nonnull EmailMessage msg, @Nullable String toList, @Nullable String ccList, @Nullable String bccList)
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
			MimeMessage message = EmailUtil.createMimeMessage(session, msg, smtpFrom, toList, ccList, bccList);
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

	public boolean sendBatchMail(@Nonnull EmailMessage msg, @Nonnull List<String> toList)
	{
		return sendMail(msg, StringUtil.join(toList, ","), null, null);
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

	public boolean validateDestAddr(@Nonnull String addr)
	{
		return StringUtil.isEmailAddress(addr);
	}

	@Nonnull
	public String sendTestingEmail(@Nonnull String toAddress)
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
			message.setSentDate(DateTimeUtil.timestampNow());
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
