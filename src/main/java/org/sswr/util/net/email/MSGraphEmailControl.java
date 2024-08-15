package org.sswr.util.net.email;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.sswr.util.data.StringUtil;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;
import org.sswr.util.net.AccessTokenProvider;
import org.sswr.util.net.HTTPClient;
import org.sswr.util.net.MSGraphUtil;
import org.sswr.util.net.RequestMethod;
import org.sswr.util.net.MSGraphUtil.AccessTokenResult;

import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.AttachmentCreateUploadSessionParameterSet;
import com.microsoft.graph.models.AttachmentItem;
import com.microsoft.graph.models.AttachmentType;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.InternetMessageHeader;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.models.UploadSession;
import com.microsoft.graph.models.UserSendMailParameterSet;
import com.microsoft.graph.requests.GraphServiceClient;

import okhttp3.Request;

public class MSGraphEmailControl implements EmailControl
{
	@Nonnull
	private String clientId;
	@Nonnull
	private String tenantId;
	@Nonnull
	private String clientSecret;
	@Nonnull
	private String fromEmail;
	private AccessTokenResult accessToken;
	private LogTool log;
	private int attSplitSize;

	public MSGraphEmailControl(LogTool log, @Nonnull String clientId, @Nonnull String tenantId, @Nonnull String clientSecret, @Nonnull String fromEmail)
	{
		this.log = log;
		this.clientId = clientId;
		this.tenantId = tenantId;
		this.clientSecret = clientSecret;
		this.fromEmail = fromEmail;
		this.attSplitSize = 1048576 * 4;
		updateAccessToken();
	}

	public void setAttSplitSize(int splitSizeByte)
	{
		this.attSplitSize = splitSizeByte;
	}

	private void updateAccessToken()
	{
		if (this.accessToken != null && this.accessToken.expiresIn.getTime() > System.currentTimeMillis())
		{
			return;
		}
		this.accessToken = MSGraphUtil.getApplicationAccessToken(this.log, this.tenantId, this.clientId, this.clientSecret, null);
		if (this.accessToken == null)
		{
			this.log.logMessage("Update access token failed", LogLevel.ERROR);
		}
		else
		{
			this.log.logMessage("Access token updated, expire time = "+this.accessToken.expiresIn.toString(), LogLevel.ACTION);
		}
	}

	private GraphServiceClient<Request> createClient()
	{
		updateAccessToken();
		if (accessToken == null)
		{
			return null;
		}
		return GraphServiceClient
				.builder()
				.authenticationProvider(new AccessTokenProvider(accessToken.accessToken))
				.buildClient();
	}

	@Override
	public boolean sendMail(EmailMessage message, String toList, String ccList) {
		Message graphMsg = new Message();
		graphMsg.subject = message.getSubject();
		ItemBody body = new ItemBody();
		if (message.isContentHTML())
			body.contentType = BodyType.HTML;
		else
			body.contentType = BodyType.TEXT;
		body.content = message.getContent();
		graphMsg.body = body;
		List<Recipient> toRecipientsList = new ArrayList<Recipient>();
		String []toArr = toList.split(",");
		int i = 0;
		int j = toArr.length;
		while (i < j)
		{
			Recipient toRecipients = new Recipient();
			com.microsoft.graph.models.EmailAddress emailAddress = new com.microsoft.graph.models.EmailAddress();
			emailAddress.address = toArr[i];
			toRecipients.emailAddress = emailAddress;
			toRecipientsList.add(toRecipients);
			i++;
		}
		graphMsg.toRecipients = toRecipientsList;
		if (ccList != null && ccList.length() > 0)
		{
			List<Recipient> ccRecipientsList = new ArrayList<Recipient>();
			String []ccArr = ccList.split(",");
			i = 0;
			j = ccArr.length;
			while (i < j)
			{
				Recipient ccRecipients = new Recipient();
				com.microsoft.graph.models.EmailAddress emailAddress = new com.microsoft.graph.models.EmailAddress();
				emailAddress.address = ccArr[i];
				ccRecipients.emailAddress = emailAddress;
				ccRecipientsList.add(ccRecipients);
				i++;
			}
		}

		i = 0;
		j = message.getCustomHeaderCount();
		if (j > 0)
		{
			List<InternetMessageHeader> headers = new ArrayList<InternetMessageHeader>();
			while (i < j)
			{
				InternetMessageHeader header = new InternetMessageHeader();
				header.name = message.getCustomHeaderName(i);
				header.value = message.getCustomHeaderValue(i);
				headers.add(header);
				i++;
			}
			graphMsg.internetMessageHeaders = headers;
		}

		if (message.getAttachmentCount() > 0)
		{
			Message newMsg = null;
			GraphServiceClient<Request> client = createClient();
			if (client == null)
				return false;
			try
			{
				newMsg = client.users(this.fromEmail)
					.messages()
					.buildRequest()
					.post(graphMsg);
				if (newMsg == null)
					return false;
			}
			catch (ClientException ex)
			{
				log.logException(ex);
				return false;
			}
			boolean succ = true;
			try
			{
				EmailAttachment att;
				i = 0;
				j = message.getAttachmentCount();
				while (i < j)
				{
					att = message.getAttachment(i);
					if (true)//att.content.length > 1048576 * 3)
					{
						AttachmentItem item = new AttachmentItem();
						item.attachmentType = AttachmentType.FILE;
						item.contentId = att.contentId;
						item.contentType = att.contentType;
						item.isInline = att.isInline;
						item.name = att.fileName;
						item.size = (long)att.content.length;
						UploadSession sess = client.users(this.fromEmail).messages(newMsg.id).attachments()
							.createUploadSession(AttachmentCreateUploadSessionParameterSet
								.newBuilder()
								.withAttachmentItem(item)
								.build())
							.buildRequest()
							.post();
						int currOfst = 0;
						int endOfst;
						while (currOfst < att.content.length)
						{
							endOfst = currOfst + this.attSplitSize;
							if (endOfst > att.content.length)
								endOfst = att.content.length;
							HTTPClient cli = HTTPClient.createConnect(null, null, sess.uploadUrl, RequestMethod.HTTP_PUT, false);
							cli.setReadTimeout(5000);
							cli.addContentType("application/octet-stream");
							cli.addContentLength(endOfst - currOfst);
							cli.addHeader("Content-Range", "bytes "+currOfst+"-"+(endOfst - 1)+"/"+att.content.length);
							cli.addHeader("Accept", "*/*");
							cli.write(att.content, currOfst, endOfst - currOfst);
							int status = cli.getRespStatus();
							byte[] data = cli.readToEnd();
							cli.close();
							if (status == 200)
							{
								currOfst = endOfst;
								if (endOfst >= att.content.length)
								{
									log.logMessage("MSGraphEmailControl: File upload pass end of file: "+att.content.length, LogLevel.ERROR);
									succ = false;
									break;
								}
							}
							else if (status == 201)
							{
								if (endOfst != att.content.length)
								{
									log.logMessage("MSGraphEmailControl: File upload missing data: "+endOfst+" != "+att.content.length, LogLevel.ERROR);
									succ = false;
									break;
								}
								break;
							}
							else
							{
								log.logMessage("MSGraphEmailControl: File upload unknown response: "+status, LogLevel.ERROR);
								log.logMessage(new String(data, StandardCharsets.UTF_8), LogLevel.RAW);
								succ = false;
								break;
							}
						}
					}
/* 					else
					{
						FileAttachment fileAtt;
						fileAtt = new FileAttachment();
						fileAtt.name = att.fileName;
//						fileAtt.contentId = att.contentId;
						fileAtt.contentType = att.contentType;
						fileAtt.contentBytes = att.content;
//						fileAtt.lastModifiedDateTime = att.modifyTime.toOffsetDateTime();
//						fileAtt.isInline = att.isInline;
						client.users(this.fromEmail).messages(newMsg.id).attachments().buildRequest().post(fileAtt);
					}*/
					i++;
				}
				if (succ)
				{
					client.users(this.fromEmail).messages(newMsg.id).send().buildRequest().post();
				}
			}
			catch (ClientException ex)
			{
				log.logException(ex);
				succ = false;
			}
			if (!succ)
			{
				try
				{
					client.users(this.fromEmail).messages(newMsg.id).buildRequest().delete();
				}
				catch (ClientException ex)
				{
					log.logException(ex);
				}
			}
			return succ;
		}
		else
		{
			try
			{
				GraphServiceClient<Request> client = createClient();
				if (client == null)
					return false;
				client.users(this.fromEmail)
					.sendMail(UserSendMailParameterSet
						.newBuilder()
						.withMessage(graphMsg)
						.withSaveToSentItems(null)
						.build())
					.buildRequest()
					.post();
				return true;
			}
			catch (ClientException ex)
			{
				log.logException(ex);
				return false;
			}
		}
	}

	@Override
	public boolean sendBatchMail(EmailMessage message, List<String> toList)
	{
		int i = toList.size();
		if (i <= 0)
		{
			return false;
		}
		while (i-- > 0)
		{
			if (!sendMail(message, toList.get(i), null))
				return false;
		}
		return true;
	}

	@Override
	public boolean isServerOnline()
	{
		updateAccessToken();
		return this.accessToken != null;
	}

	@Override
	public boolean validateDestAddr(String addr) {
		return StringUtil.isEmailAddress(addr);
	}

	@Override
	public String sendTestingEmail(String toAddress) {
		EmailMessage message = new SimpleEmailMessage("Email Testing", "This is a test email", false);
		if (sendMail(message, toAddress, null))
		{
			return "Sent";
		}
		else
		{
			return "Failed";
		}
	}
}
