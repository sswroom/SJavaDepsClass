package org.sswr.util.net.email;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.sswr.util.data.StringUtil;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;
import org.sswr.util.net.HTTPClient;
import org.sswr.util.net.MSGraphUtil;
import org.sswr.util.net.RequestMethod;
import org.sswr.util.net.MSGraphUtil.AccessTokenResult;

import com.microsoft.graph.models.AttachmentItem;
import com.microsoft.graph.models.AttachmentType;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.InternetMessageHeader;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.models.UploadSession;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.graph.users.item.messages.item.attachments.createuploadsession.CreateUploadSessionPostRequestBody;
import com.microsoft.graph.users.item.sendmail.SendMailPostRequestBody;

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

	private GraphServiceClient createClient()
	{
		updateAccessToken();
		if (accessToken == null)
		{
			return null;
		}
		return MSGraphUtil.createClient(accessToken);
	}

	@Override
	public boolean sendMail(@Nonnull EmailMessage message, @Nullable String toList, @Nullable String ccList) {
		Message graphMsg = new Message();
		graphMsg.setSubject(message.getSubject());
		ItemBody body = new ItemBody();
		if (message.isContentHTML())
			body.setContentType(BodyType.Html);
		else
			body.setContentType(BodyType.Text);
		body.setContent(message.getContent());
		graphMsg.setBody(body);
		int i;
		int j;
		if (toList != null)
		{
			List<Recipient> toRecipientsList = new ArrayList<Recipient>();
			String []toArr = toList.split(",");
			i = 0;
			j = toArr.length;
			while (i < j)
			{
				Recipient toRecipients = new Recipient();
				com.microsoft.graph.models.EmailAddress emailAddress = new com.microsoft.graph.models.EmailAddress();
				emailAddress.setAddress(toArr[i]);
				toRecipients.setEmailAddress(emailAddress);
				toRecipientsList.add(toRecipients);
				i++;
			}
			graphMsg.setToRecipients(toRecipientsList);
		}
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
				emailAddress.setAddress(ccArr[i]);
				ccRecipients.setEmailAddress(emailAddress);
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
				header.setName(message.getCustomHeaderName(i));
				header.setValue(message.getCustomHeaderValue(i));
				headers.add(header);
				i++;
			}
			graphMsg.setInternetMessageHeaders(headers);
		}

		if (message.getAttachmentCount() > 0)
		{
			Message newMsg = null;
			GraphServiceClient client = createClient();
			if (client == null)
				return false;
			newMsg = client.users().byUserId(this.fromEmail)
				.messages()
				.post(graphMsg);
			if (newMsg == null)
			{
				this.log.logMessage("Error in getting user info", LogLevel.ERROR);
				return false;
			}
			String newMsgId = newMsg.getId();
			if (newMsgId == null)
			{
				this.log.logMessage("Message id not found", LogLevel.ERROR);
				return false;
			}
			boolean succ = true;
			EmailAttachment att;
			i = 0;
			j = message.getAttachmentCount();
			while (i < j)
			{
				att = message.getAttachment(i);
				if (att != null)//att.content.length > 1048576 * 3)
				{
					AttachmentItem item = new AttachmentItem();
					item.setAttachmentType(AttachmentType.File);
					item.setContentId(att.contentId);
					item.setContentType(att.contentType);
					item.setIsInline(att.isInline);
					item.setName(att.fileName);
					item.setSize((long)att.content.length);
					CreateUploadSessionPostRequestBody itemBody = new CreateUploadSessionPostRequestBody();
					itemBody.setAttachmentItem(item);
					UploadSession sess = client.users().byUserId(this.fromEmail).messages().byMessageId(newMsgId).attachments()
						.createUploadSession().post(itemBody);
					if (sess == null)
					{
						this.log.logMessage("MSGraphEmailControl: Error in creating upload session", LogLevel.ERROR);
						return false;
					}
					String uploadUrl = sess.getUploadUrl();
					if (uploadUrl == null)
					{
						this.log.logMessage("MSGraphEmailControl: Upload session url not found", LogLevel.ERROR);
						return false;
					}
					int currOfst = 0;
					int endOfst;
					while (currOfst < att.content.length)
					{
						endOfst = currOfst + this.attSplitSize;
						if (endOfst > att.content.length)
							endOfst = att.content.length;
						HTTPClient cli = HTTPClient.createConnect(null, null, uploadUrl, RequestMethod.HTTP_PUT, false);
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
					client.users(this.fromEmail).messages(newMsgId).attachments().buildRequest().post(fileAtt);
				}*/
				i++;
			}
			if (succ)
			{
				client.users().byUserId(this.fromEmail).messages().byMessageId(newMsgId).send().post();
			}
			if (!succ)
			{
				client.users().byUserId(this.fromEmail).messages().byMessageId(newMsgId).delete();
			}
			return succ;
		}
		else
		{
			GraphServiceClient client = createClient();
			if (client == null)
				return false;
			SendMailPostRequestBody mailBody = new SendMailPostRequestBody();
			mailBody.setMessage(graphMsg);
			mailBody.setSaveToSentItems(null);
			client
				.users().byUserId(this.fromEmail)
				.sendMail().post(mailBody);
			return true;
		}
	}

	@Override
	public boolean sendBatchMail(@Nonnull EmailMessage message, @Nonnull List<String> toList)
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
	public boolean validateDestAddr(@Nonnull String addr) {
		return StringUtil.isEmailAddress(addr);
	}

	@Override
	@Nonnull 
	public String sendTestingEmail(@Nonnull String toAddress) {
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
