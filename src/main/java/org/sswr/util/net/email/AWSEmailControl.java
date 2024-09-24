package org.sswr.util.net.email;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.sswr.util.data.StringUtil;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.apache.ProxyConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

public class AWSEmailControl implements EmailControl{
	
	private static final int minSendInterval = 30000;
	private static long lastSendTime = 0;
	
	private LogTool log;
	private Region region;
	private String fromAddr;
	private boolean useProxy;
	private String proxyHost;
	private int proxyPort;
	private String proxyUser;
	private String proxyPassword;

	public AWSEmailControl(@Nonnull String fromAddr, @Nonnull Region region, @Nullable LogTool log)
	{
		this.fromAddr = fromAddr;
		this.region = region;
		this.log = log;
		this.useProxy = false;
		this.proxyHost = null;
		this.proxyPort = 0;
		this.proxyUser = null;
		this.proxyPassword = null;
	}
	
	public void setProxy(@Nullable String proxyHost, int proxyPort, @Nullable String proxyUser, @Nullable String proxyPassword)
	{
		this.useProxy = true;
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.proxyUser = proxyUser;
		this.proxyPassword = proxyPassword;
	}
	
	@Nullable
	private SdkHttpClient getProxyHTTPClient()
	{
		//Proxy Setting for GCIS
		SdkHttpClient httpClient = null;
		
		long t = System.currentTimeMillis();
		if ((t - lastSendTime) < minSendInterval)
		{
			try
			{
				Thread.sleep(minSendInterval - (t - lastSendTime));
			}
			catch (InterruptedException ex)
			{

			}
		}
		lastSendTime = System.currentTimeMillis();
		if (this.useProxy) {
			try {
				ProxyConfiguration proxyConfig = ProxyConfiguration.builder()
					.endpoint(URI.create("http://" + this.proxyHost + ":" + this.proxyPort))
					.username(this.proxyUser)
					.password(this.proxyPassword)
					.useSystemPropertyValues(false)
					//.useSystemPropertyValues(true)
					.build();
				
				if (this.log != null)
				{
					this.log.logMessage("AWS http proxy client detail -> Host: [" + proxyConfig.host() + "]", LogLevel.COMMAND);
					this.log.logMessage("AWS http proxy client detail -> Port: [" + proxyConfig.port() + "]", LogLevel.COMMAND);
				}
				
				httpClient = ApacheHttpClient.builder()
					.proxyConfiguration(proxyConfig)
	            	.build();
			}
			catch(Exception e) {
				if (this.log != null)
				{
					this.log.logMessage("AWS http proxy client error", LogLevel.ERROR);
					this.log.logException(e);
				}
			}
		}
		else {
			httpClient = ApacheHttpClient.builder()
				.proxyConfiguration(ProxyConfiguration.builder()
					.useSystemPropertyValues(false)
					.build())
				.build();
		}
		return httpClient;
	}
	

	@Override
	public boolean sendMail(EmailMessage message, String toList, String ccList) {
		if (message == null) return false;
		SesClient client = SesClient.builder().httpClient(getProxyHTTPClient())
		        .region(this.region)
		        .build();
		try{
			this.send(client, message, toList, ccList);
			client.close();
			
			if (this.log != null)
				log.logMessage("Send Email: Done", LogLevel.COMMAND);
			return true;
		} catch (IOException | MessagingException e) {
			if (this.log != null)
			{
				log.logMessage("Send Email: Failed", LogLevel.ERROR);
				log.logException(e);
			}
			return false;
		}
	}

	private void send(@Nonnull SesClient client, @Nonnull EmailMessage msg, @Nullable String toList, @Nullable String ccList) throws AddressException, MessagingException, IOException {
		Session session = Session.getDefaultInstance(new Properties());
		MimeMessage message = new MimeMessage(session);

		// Add subject, from and to lines
		message.setSubject(msg.getSubject(), "UTF-8");
		message.setFrom(new InternetAddress(this.fromAddr));
		if (this.log != null) log.logMessage("Sending email with subject("+msg.getSubject()+")", LogLevel.RAW);
		if (toList != null && toList.length() > 0)
		{
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toList));
			if (this.log != null) log.logMessage("Email To: "+toList, LogLevel.RAW);
		}
		if (ccList != null && ccList.length() > 0)
		{
			message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccList));
			if (this.log != null) log.logMessage("Email Cc: "+ccList, LogLevel.RAW);
		}

		MimeBodyPart wrap = new MimeBodyPart();
		wrap.setContent(msg.getContent(), msg.isContentHTML()?"text/html; charset=UTF-8":"text/plain; charset=UTF-8");

		// Create a multipart/mixed parent container
		MimeMultipart mimeMsg = new MimeMultipart("mixed");

		// Add the parent container to the message
		message.setContent(mimeMsg);

		// Add the multipart/alternative part to the message
		mimeMsg.addBodyPart(wrap);
		int i = 0;
		int j = msg.getAttachmentCount();
		while (i < j)
		{
			wrap = new MimeBodyPart();
			EmailAttachment att = msg.getAttachment(i);
			ByteArrayDataSource ds = new ByteArrayDataSource(att.content, att.contentType);
			wrap.setDataHandler(new DataHandler(ds));
			wrap.setFileName(att.fileName);
			wrap.setDisposition(att.isInline?"inline":"attachment");
			mimeMsg.addBodyPart(wrap);
			i++;
		}

		try {
			if (this.log != null) log.logMessage("Attempting to send email: [" + msg.getSubject() + "] through Amazon SES using the AWS SDK for Java...", LogLevel.RAW);

			 ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			 message.writeTo(outputStream);
			 ByteBuffer buf = ByteBuffer.wrap(outputStream.toByteArray());

			 byte[] arr = new byte[buf.remaining()];
			 buf.get(arr);

			 SdkBytes data = SdkBytes.fromByteArray(arr);
			 RawMessage rawMessage = RawMessage.builder()
					.data(data)
					.build();

			 SendRawEmailRequest rawEmailRequest = SendRawEmailRequest.builder()
					.rawMessage(rawMessage)
					.build();

			 client.sendRawEmail(rawEmailRequest);
		 } catch (SesException e) {        	
			if (this.log != null)
			{
				log.logMessage("AWS Send Mail error", LogLevel.ERROR);
				log.logException(e);
			}
		 }
	}

	@Override
	public boolean sendBatchMail(EmailMessage message, List<String> toList) {
		boolean succ = false;
		int i = 0;
		int j = toList.size();
		while (i < j)
		{
			if (this.sendMail(message, toList.get(i), null))
			{
				succ = true;
			}
			i++;
		}
		return succ;
	}

	@Override
	public boolean isServerOnline() {
		return true;
	}

	@Override
	public boolean validateDestAddr(String addr) {
		return StringUtil.isEmailAddress(addr);
	}

	@Override
	public String sendTestingEmail(String toAddress) {
		SimpleEmailMessage msg = new SimpleEmailMessage("Testing email", "Test content", false);
		return this.sendMail(msg, toAddress, null)?"Success":"Failed";
	}
}
