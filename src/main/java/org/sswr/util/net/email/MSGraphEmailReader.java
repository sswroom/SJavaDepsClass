package org.sswr.util.net.email;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.annotation.Nonnull;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.sswr.util.data.DataTools;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;
import org.sswr.util.net.AccessTokenProvider;
import org.sswr.util.net.MSGraphUtil;
import org.sswr.util.net.MSGraphUtil.AccessTokenResult;

import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.FileAttachment;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.MailFolder;
import com.microsoft.graph.models.MessageMoveParameterSet;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.requests.AttachmentCollectionPage;
import com.microsoft.graph.requests.AttachmentCollectionRequest;
import com.microsoft.graph.requests.AttachmentCollectionRequestBuilder;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.MailFolderCollectionPage;
import com.microsoft.graph.requests.MailFolderCollectionRequest;
import com.microsoft.graph.requests.MailFolderCollectionRequestBuilder;
import com.microsoft.graph.requests.MessageCollectionPage;

import jakarta.annotation.Nullable;
import okhttp3.Request;

public class MSGraphEmailReader implements EmailReader
{
	class GraphMessage extends Message
	{
		com.microsoft.graph.models.Message msg;
		EmailMessage emsg;
		boolean delete;

		@Nullable
		static EmailAddress addrFromRecipient(@Nonnull Recipient rcpt)
		{
			com.microsoft.graph.models.EmailAddress addr = rcpt.emailAddress;
			if (addr == null)
				return null;
			return new EmailAddress(addr.name, addr.address);
		}

		public GraphMessage(com.microsoft.graph.models.Message msg, EmailMessage emsg)
		{
			this.msg = msg;
			this.emsg = emsg;
			this.delete = false;
		}

		private static String[] arrayFromString(String s)
		{
			if (s == null)
				return null;
			String[] ret = new String[1];
			ret[0] = s;
			return ret;
		}

		private static Address addressFromRecipient(Recipient rcpt)
		{
			com.microsoft.graph.models.EmailAddress addr = rcpt.emailAddress;
			if (addr == null)
				return null;
			try
			{
				return new InternetAddress(addr.address, addr.name);
			}
			catch (UnsupportedEncodingException ex)
			{
				try
				{
					return new InternetAddress(addr.address);
				}
				catch (AddressException ex2)
				{
					ex.printStackTrace();
					return null;
				}
			}
		}

		private static Address[] addressFromRecipients(List<Recipient> rcpts)
		{
			List<Address> list = new ArrayList<Address>();
			Address addr;
			int i = 0;
			int j = rcpts.size();
			while (i < j)
			{
				addr = addressFromRecipient(rcpts.get(i));
				if (addr != null)
					list.add(addr);
				i++;
			}
			j = list.size();
			Address[] ret = new Address[j];
			i = 0;
			while (i < j)
			{
				ret[i] = list.get(i);
				i++;
			}
			return ret;
		}

		private static String fromRecipient(Recipient rcpt)
		{
			com.microsoft.graph.models.EmailAddress addr = rcpt.emailAddress;
			if (addr == null)
				return null;
			return addr.address;
		}
		
		private static String[] fromRecipients(List<Recipient> rcpts)
		{
			List<String> list = new ArrayList<String>();
			int i = 0;
			int j = rcpts.size();
			while (i < j)
			{
				list.add(fromRecipient(rcpts.get(i)));
				i++;
			}
			String[] ret = new String[j];
			i = 0;
			while (i < j)
			{
				ret[i] = list.get(i);
				i++;
			}
			return ret;
		}

		@Override
		public int getSize() throws MessagingException {
			ItemBody body = this.msg.body;
			if (body == null)
				throw new MessagingException("body is null");
			String content = body.content;
			if (content == null)
				throw new MessagingException("content is null");
			return content.length();
		}

		@Override
		public int getLineCount() throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'getLineCount'");
		}

		@Override
		public String getContentType() throws MessagingException {
			ItemBody body = this.msg.body;
			if (body == null)
				throw new MessagingException("body is null");
			return (body.contentType == BodyType.HTML)?"text/html":"text/plain";
		}

		@Override
		public boolean isMimeType(String mimeType) throws MessagingException {
			return this.getContentType().equals(mimeType);
		}

		@Override
		public String getDisposition() throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'getDisposition'");
		}

		@Override
		public void setDisposition(String disposition) throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'setDisposition'");
		}

		@Override
		public String getDescription() throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'getDescription'");
		}

		@Override
		public void setDescription(String description) throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'setDescription'");
		}

		@Override
		public String getFileName() throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'getFileName'");
		}

		@Override
		public void setFileName(String filename) throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'setFileName'");
		}

		@Override
		public InputStream getInputStream() throws IOException, MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'getInputStream'");
		}

		@Override
		public DataHandler getDataHandler() throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'getDataHandler'");
		}

		@Override
		public Object getContent() throws IOException, MessagingException {
			ItemBody body = this.msg.body;
			if (body == null)
				throw new MessagingException("body is null");
			if (this.emsg.getAttachmentCount() > 0)
			{
				return EmailUtil.createMultipart(this.emsg.getAttachments(), body.content, this.getContentType());
			}
			else
				return body.content;
		}

		@Override
		public void setDataHandler(DataHandler dh) throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'setDataHandler'");
		}

		@Override
		public void setContent(Object obj, String type) throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'setContent'");
		}

		@Override
		public void setText(String text) throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'setText'");
		}

		@Override
		public void setContent(Multipart mp) throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'setContent'");
		}

		@Override
		public void writeTo(OutputStream os) throws IOException, MessagingException {
			ItemBody body = this.msg.body;
			if (body == null)
				throw new MessagingException("body is null");
			List<Recipient> recipients;
			SMTPMessage message = new SMTPMessage();
			message.setMessageId(this.msg.id);
			message.setContent(body.content, this.getContentType());
			if (this.msg.sentDateTime != null)
				message.setSentDate(this.msg.sentDateTime.toZonedDateTime());
			message.setSubject(this.msg.subject);
			if (this.msg.from != null)
				message.setFrom(addrFromRecipient(this.msg.from));
			int i;
			int j;
			recipients = this.msg.toRecipients;
			if (recipients != null)
			{
				i = 0;
				j = recipients.size();
				while (i < j)
				{
					message.addTo(addrFromRecipient(recipients.get(i)));
					i++;
				}
			}
			recipients = this.msg.ccRecipients;
			if (recipients != null)
			{
				i = 0;
				j = recipients.size();
				while (i < j)
				{
					message.addCc(addrFromRecipient(recipients.get(i)));
					i++;
				}
			}
			recipients = this.msg.bccRecipients;
			if (recipients != null)
			{
				i = 0;
				j = recipients.size();
				while (i < j)
				{
					message.addBcc(addrFromRecipient(recipients.get(i)));
					i++;
				}
			}
			i = 0;
			j = emsg.getAttachmentCount();
			while (i < j)
			{
				message.addAttachment(emsg.getAttachment(i));
				i++;
			}
			if (!message.writeMessage(os))
			{
				throw new IOException("Error in writing to stream");
			}
		}

		@Override
		public String[] getHeader(String header_name) throws MessagingException {
			switch (header_name.toUpperCase()) {
				case "TO":
					return fromRecipients(this.msg.toRecipients);
				case "CC":
					return fromRecipients(this.msg.ccRecipients);
				case "BCC":
					return fromRecipients(this.msg.bccRecipients);
				case "SUBJECT":
					return arrayFromString(this.msg.subject);
				default:
					return null;
			}
		}

		@Override
		public void setHeader(String header_name, String header_value) throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'setHeader'");
		}

		@Override
		public void addHeader(String header_name, String header_value) throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'addHeader'");
		}

		@Override
		public void removeHeader(String header_name) throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'removeHeader'");
		}

		@Override
		public Enumeration<Header> getAllHeaders() throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'getAllHeaders'");
		}

		@Override
		public Enumeration<Header> getMatchingHeaders(String[] header_names) throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'getMatchingHeaders'");
		}

		@Override
		public Enumeration<Header> getNonMatchingHeaders(String[] header_names) throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'getNonMatchingHeaders'");
		}

		@Override
		public Address[] getFrom() throws MessagingException {
			Address addr[] = new Address[1];
			addr[0] = addressFromRecipient(this.msg.from);
			return addr;
		}

		@Override
		public void setFrom() throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'setFrom'");
		}

		@Override
		public void setFrom(Address address) throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'setFrom'");
		}

		@Override
		public void addFrom(Address[] addresses) throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'addFrom'");
		}

		@Override
		public Address[] getRecipients(RecipientType type) throws MessagingException {
			if (type == RecipientType.TO)
			{
				return addressFromRecipients(this.msg.toRecipients);
			}
			else if (type == RecipientType.CC)
			{
				return addressFromRecipients(this.msg.ccRecipients);
			}
			else if (type == RecipientType.BCC)
			{
				return addressFromRecipients(this.msg.bccRecipients);
			}
			else
			{
				return null;
			}
		}

		@Override
		public void setRecipients(RecipientType type, Address[] addresses) throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'setRecipients'");
		}

		@Override
		public void addRecipients(RecipientType type, Address[] addresses) throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'addRecipients'");
		}

		@Override
		public String getSubject() throws MessagingException {
			return this.msg.subject;
		}

		@Override
		public void setSubject(String subject) throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'setSubject'");
		}

		@Override
		public Date getSentDate() throws MessagingException {
			if (this.msg.sentDateTime != null)
			{
				return DateTimeUtil.toTimestamp(this.msg.sentDateTime.toZonedDateTime());
			}
			return null;
		}

		@Override
		public void setSentDate(Date date) throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'setSentDate'");
		}

		@Override
		public Date getReceivedDate() throws MessagingException {
			if (this.msg.receivedDateTime != null)
			{
				return DateTimeUtil.toTimestamp(this.msg.receivedDateTime.toZonedDateTime());
			}
			return null;
		}

		@Override
		public Flags getFlags() throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'getFlags'");
		}

		@Override
		public void setFlags(Flags flag, boolean set) throws MessagingException {
			if (flag.contains(Flags.Flag.DELETED))
			{
				this.delete = set;
				addChanged(this.msg.id, this);
			}
		}

		@Override
		public Message reply(boolean replyToAll) throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'reply'");
		}

		@Override
		public void saveChanges() throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'saveChanges'");
		}

		public boolean isDeleted()
		{
			return this.delete;
		}

		public String getId()
		{
			return this.msg.id;
		}
	}
	
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
	private String folder;
	private Map<String, GraphMessage> changedMap;
	private boolean archiveOnDelete;

	public MSGraphEmailReader(LogTool log, @Nonnull String clientId, @Nonnull String tenantId, @Nonnull String clientSecret, @Nonnull String fromEmail)
	{
		this.log = log;
		this.clientId = clientId;
		this.tenantId = tenantId;
		this.clientSecret = clientSecret;
		this.fromEmail = fromEmail;
		this.folder = null;
		this.archiveOnDelete = false;
		this.changedMap = new HashMap<String, GraphMessage>();
		updateAccessToken();
	}

	public void setArchiveOnDelete(boolean archiveOnDelete)
	{
		this.archiveOnDelete = archiveOnDelete;
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

    public boolean open()
	{
		return true;
	}

    public boolean openFolder(String folderName)
	{
		GraphServiceClient<Request> client = createClient();
		if (client == null)
			return false;
		List<MailFolder> folderList;
		int i;
		int j;
		MailFolderCollectionPage folders;
		MailFolder folder;
		MailFolderCollectionRequest req = client.users(fromEmail).mailFolders().buildRequest();
		while ((folders = req.get()) != null)
		{
			folderList = folders.getCurrentPage();
			i = 0;
			j = folderList.size();
			while (i < j)
			{
				folder = folderList.get(i);
				if (folder.displayName != null && folder.displayName.equals(folderName))
				{
					this.folder = folder.id;
					return true;
				}
				i++;
			}
			MailFolderCollectionRequestBuilder nextPage = folders.getNextPage();
			if (nextPage == null)
				return false;
			req = nextPage.buildRequest();
		}
		return false;
	}

    public void closeFolder()
	{
		Iterator<GraphMessage> it = changedMap.values().iterator();
		GraphMessage msg;
		while (it.hasNext())
		{
			msg = it.next();
			if (msg.isDeleted())
			{
				if (this.archiveOnDelete)
					this.moveMessageToArchive(msg.getId());
				else
					this.deleteMessage(msg.getId());
			}
		}
		this.changedMap.clear();
		this.folder = null;
	}

    public void close()
	{
	}

 	public Message[] getMessages()
	{
		GraphServiceClient<Request> client = createClient();
		if (client == null)
			return null;
		MessageCollectionPage messages;
		if (this.folder != null)
		{
			messages = client.users(fromEmail).mailFolders(this.folder).messages().buildRequest().get();
		}
		else
		{
			messages = client.users(fromEmail).messages().buildRequest().get();
		}
		if (messages == null)
		{
			return null;
		}
		List<com.microsoft.graph.models.Message> msg = messages.getCurrentPage();
		Message[] msgArr = new Message[msg.size()];
		int i = 0;
		int j = msg.size();
		while (i < j)
		{
			msgArr[i] = new GraphMessage(msg.get(i), this.toEmailMessage(msg.get(i)));
			i++;
		}
		return msgArr;
	}

	public boolean moveMessageToArchive(String id)
	{
		GraphServiceClient<Request> client = createClient();
		if (client == null)
			return false;
		
		com.microsoft.graph.models.Message msg = client.users(fromEmail).messages(id).move(MessageMoveParameterSet.newBuilder().withDestinationId("archive").build()).buildRequest().post();
		if (msg != null)
		{
			return true;
		}
		return false;
	}

	public boolean deleteMessage(String id)
	{
		GraphServiceClient<Request> client = createClient();
		if (client == null)
			return false;
		com.microsoft.graph.models.Message msg = client.users(fromEmail).messages(id).buildRequest().delete();
		if (msg != null)
		{
			return true;
		}
		return false;
	}

	@Nullable
	private EmailMessage toEmailMessage(@Nonnull com.microsoft.graph.models.Message msg)
	{
		ItemBody body = msg.body;
		if (body == null)
			return null;
		String msgId = msg.id;
		if (msgId == null)
			return null;
		SimpleEmailMessage email = new SimpleEmailMessage(msg.subject, body.content, body.contentType == BodyType.HTML);
		if (msg.hasAttachments != null && msg.hasAttachments)
		{
			GraphServiceClient<Request> client = createClient();
			if (client == null)
				return null;
			AttachmentCollectionRequest req = client.users(fromEmail).messages(msgId).attachments().buildRequest();
			AttachmentCollectionPage page;
			int i;
			int j;
			while ((page = req.get()) != null)
			{
				List<Attachment> attList = page.getCurrentPage();
				Attachment att;
				i = 0;
				j = attList.size();
				while (i < j)
				{
					att = attList.get(i);
					if (att instanceof FileAttachment)
					{
						FileAttachment fatt = (FileAttachment)att;
						EmailAttachment eatt = new EmailAttachment();
						eatt.isInline = fatt.isInline != null && fatt.isInline;
						eatt.contentId = fatt.contentId;
						eatt.fileName = fatt.name;
						if (fatt.lastModifiedDateTime != null)
						{
							eatt.modifyTime = fatt.lastModifiedDateTime.toZonedDateTime();
						}
						eatt.contentType = fatt.contentType;
						eatt.content = fatt.contentBytes;
						email.addAttachment(eatt);
					}
					else
					{
						System.out.println(DataTools.toObjectStringWF(att));
					}
					i++;
				}

				AttachmentCollectionRequestBuilder nextPage = page.getNextPage();
				if (nextPage == null)
					break;
				req = nextPage.buildRequest();
			}
		}
		
		return email;
	}

	private void addChanged(String id, GraphMessage msg)
	{
		this.changedMap.put(id, msg);
	}
}
