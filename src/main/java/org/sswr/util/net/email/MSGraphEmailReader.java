package org.sswr.util.net.email;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.activation.DataHandler;
import jakarta.mail.Address;
import jakarta.mail.Flags;
import jakarta.mail.Header;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

import org.sswr.util.data.DataTools;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.SharedBool;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;
import org.sswr.util.net.MSGraphUtil;
import org.sswr.util.net.MSGraphUtil.AccessTokenResult;

import com.microsoft.graph.core.tasks.PageIterator;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.AttachmentCollectionResponse;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.FileAttachment;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.MailFolder;
import com.microsoft.graph.models.MailFolderCollectionResponse;
import com.microsoft.graph.models.MessageCollectionResponse;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.graph.users.item.messages.item.move.MovePostRequestBody;
import com.microsoft.kiota.ApiException;

public class MSGraphEmailReader implements EmailReader
{
	private static boolean debug = false;

	public static void setDebug(boolean debug)
	{
		MSGraphEmailReader.debug = debug;
	}

	class GraphMessage extends Message
	{
		com.microsoft.graph.models.Message msg;
		EmailMessage emsg;
		boolean delete;

		@Nullable
		static EmailAddress addrFromRecipient(@Nonnull Recipient rcpt)
		{
			com.microsoft.graph.models.EmailAddress addr = rcpt.getEmailAddress();
			if (addr == null)
				return null;
			String eaddr = addr.getAddress();
			if (eaddr == null)
				return null;
			return new EmailAddress(addr.getName(), eaddr);
		}

		public GraphMessage(@Nonnull com.microsoft.graph.models.Message msg, @Nonnull EmailMessage emsg)
		{
			this.msg = msg;
			this.emsg = emsg;
			this.delete = false;
		}

		@Nonnull
		private static String[] arrayFromString(@Nonnull String s)
		{
			String[] ret = new String[1];
			ret[0] = s;
			return ret;
		}

		private static Address addressFromRecipient(@Nonnull Recipient rcpt)
		{
			com.microsoft.graph.models.EmailAddress addr = rcpt.getEmailAddress();
			if (addr == null)
				return null;
			try
			{
				return new InternetAddress(addr.getAddress(), addr.getName());
			}
			catch (UnsupportedEncodingException ex)
			{
				try
				{
					return new InternetAddress(addr.getAddress());
				}
				catch (AddressException ex2)
				{
					ex.printStackTrace();
					return null;
				}
			}
		}

		@Nonnull
		private static Address[] addressFromRecipients(@Nonnull List<Recipient> rcpts)
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

		@Nullable
		private static String fromRecipient(@Nonnull Recipient rcpt)
		{
			com.microsoft.graph.models.EmailAddress addr = rcpt.getEmailAddress();
			if (addr == null)
				return null;
			return addr.getAddress();
		}
		
		@Nonnull 
		private static String[] fromRecipients(@Nonnull List<Recipient> rcpts)
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
			ItemBody body = this.msg.getBody();
			if (body == null)
				throw new MessagingException("body is null");
			String content = body.getContent();
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
			ItemBody body = this.msg.getBody();
			if (body == null)
				throw new MessagingException("body is null");
			return (body.getContentType() == BodyType.Html)?"text/html":"text/plain";
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
			ItemBody body = this.msg.getBody();
			if (body == null)
				throw new MessagingException("body is null");
			String content = body.getContent();
			if (content == null)
				throw new MessagingException("body content is null");
			if (this.emsg.getAttachmentCount() > 0)
			{
				return EmailUtil.createMultipart(this.emsg.getAttachments(), content, this.getContentType());
			}
			else
				return content;
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
			ItemBody body = this.msg.getBody();
			if (body == null)
				throw new MessagingException("body is null");
			List<Recipient> recipients;
			SMTPMessage message = new SMTPMessage();
			String s;
			OffsetDateTime d;
			Recipient rcpt;
			EmailAddress addr;
			EmailAttachment att;
			if ((s = this.msg.getId()) != null) message.setMessageId(s);
			if ((s = body.getContent()) != null) message.setContent(s, this.getContentType());
			if ((d = this.msg.getSentDateTime()) != null)
				message.setSentDate(d.toZonedDateTime());
			if ((s = this.msg.getSubject()) != null) message.setSubject(s);
			if ((rcpt = this.msg.getFrom()) != null && (addr = addrFromRecipient(rcpt)) != null) message.setFrom(addr);
			int i;
			int j;
			recipients = this.msg.getToRecipients();
			if (recipients != null)
			{
				i = 0;
				j = recipients.size();
				while (i < j)
				{
					if ((rcpt = recipients.get(i)) != null && (addr = addrFromRecipient(rcpt)) != null)
						message.addTo(addr);
					i++;
				}
			}
			recipients = this.msg.getCcRecipients();
			if (recipients != null)
			{
				i = 0;
				j = recipients.size();
				while (i < j)
				{
					if ((rcpt = recipients.get(i)) != null && (addr = addrFromRecipient(rcpt)) != null)
						message.addCc(addr);
					i++;
				}
			}
			recipients = this.msg.getBccRecipients();
			if (recipients != null)
			{
				i = 0;
				j = recipients.size();
				while (i < j)
				{
					if ((rcpt = recipients.get(i)) != null && (addr = addrFromRecipient(rcpt)) != null)
						message.addBcc(addr);
					i++;
				}
			}
			i = 0;
			j = emsg.getAttachmentCount();
			while (i < j)
			{
				if ((att = emsg.getAttachment(i)) != null)
					message.addAttachment(att);
				i++;
			}
			if (!message.writeMessage(os))
			{
				throw new IOException("Error in writing to stream");
			}
		}

		@Override
		public String[] getHeader(String header_name) throws MessagingException {
			String s;
			List<Recipient> rcpts;
			switch (header_name.toUpperCase()) {
				case "TO":
					if ((rcpts = this.msg.getToRecipients())!= null)
						return fromRecipients(rcpts);
					return null;
				case "CC":
					if ((rcpts = this.msg.getCcRecipients())!= null)
						return fromRecipients(rcpts);
					return null;
				case "BCC":
					if ((rcpts = this.msg.getBccRecipients())!= null)
						return fromRecipients(rcpts);
					return null;
				case "SUBJECT":
					if ((s = this.msg.getSubject()) != null)
						return arrayFromString(s);
					return null;
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
			Recipient rcpt = this.msg.getFrom();
			if (rcpt == null)
			{
				return new Address[0];
			}
			Address addr[] = new Address[1];
			addr[0] = addressFromRecipient(rcpt);
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
			List<Recipient> rcpts;
			if (type == RecipientType.TO)
			{
				if ((rcpts = this.msg.getToRecipients()) != null)
					return addressFromRecipients(rcpts);
				return null;
			}
			else if (type == RecipientType.CC)
			{
				if ((rcpts = this.msg.getCcRecipients()) != null)
					return addressFromRecipients(rcpts);
				return null;
			}
			else if (type == RecipientType.BCC)
			{
				if ((rcpts = this.msg.getBccRecipients()) != null)
					return addressFromRecipients(rcpts);
				return null;
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
			return this.msg.getSubject();
		}

		@Override
		public void setSubject(String subject) throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'setSubject'");
		}

		@Override
		public Date getSentDate() throws MessagingException {
			OffsetDateTime dt;
			if ((dt = this.msg.getSentDateTime()) != null)
			{
				return DateTimeUtil.toTimestamp(dt.toZonedDateTime());
			}
			return null;
		}

		@Override
		public void setSentDate(Date date) throws MessagingException {
			throw new UnsupportedOperationException("Unimplemented method 'setSentDate'");
		}

		@Override
		public Date getReceivedDate() throws MessagingException {
			OffsetDateTime dt;
			if ((dt = this.msg.getReceivedDateTime()) != null)
			{
				return DateTimeUtil.toTimestamp(dt.toZonedDateTime());
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
				addChanged(this.msg.getId(), this);
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
			return this.msg.getId();
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

	public MSGraphEmailReader(@Nonnull LogTool log, @Nonnull String clientId, @Nonnull String tenantId, @Nonnull String clientSecret, @Nonnull String fromEmail)
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

	private GraphServiceClient createClient()
	{
		updateAccessToken();
		if (accessToken == null)
		{
			return null;
		}
		return MSGraphUtil.createClient(accessToken);
	}

    public boolean open()
	{
		return true;
	}

    public boolean openFolder(@Nonnull String folderName)
	{
		GraphServiceClient client = createClient();
		if (client == null)
			return false;
		SharedBool found = new SharedBool();
		found.value = false;
		MailFolderCollectionResponse folders = client.users().byUserId(fromEmail).mailFolders().get();
		if (folders == null)
		{
			this.log.logMessage("Error in getting user mail folder", LogLevel.ERROR);
			return false;
		}
		try
		{
			PageIterator<MailFolder, MailFolderCollectionResponse> pageIterator = new PageIterator.Builder<MailFolder, MailFolderCollectionResponse>()
			.client(client)
			.collectionPage(folders)
			.collectionPageFactory(MailFolderCollectionResponse::createFromDiscriminatorValue)
			.processPageItemCallback(folder -> {
				String dispName;
				if ((dispName = folder.getDisplayName()) != null && dispName.equals(folderName))
				{
					this.folder = folder.getId();
					found.value = true;
					return false;
				}
				return true;
			}).build();
			pageIterator.iterate();
			return found.value;
		}
		catch (ApiException|ReflectiveOperationException ex)
		{
			this.log.logException(ex);
			return false;
		}
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
		GraphServiceClient client = createClient();
		if (client == null)
		{
			if (debug) this.log.logMessage("getMessages: createClient = null", LogLevel.ERROR);
			return null;
		}
		MessageCollectionResponse messages;
		if (this.folder != null)
		{
			messages = client.users().byUserId(fromEmail).mailFolders().byMailFolderId(this.folder).messages().get();
		}
		else
		{
			messages = client.users().byUserId(fromEmail).messages().get();
		}
		if (messages == null)
		{
			if (debug) this.log.logMessage("getMessages: messages = null, folder = " + this.folder, LogLevel.ERROR);
			return null;
		}
		List<com.microsoft.graph.models.Message> msg = messages.getValue();
		if (msg == null)
		{
			if (debug) this.log.logMessage("getMessages: messages.value = null, folder = " + this.folder, LogLevel.ERROR);
			return null;
		}
		Message[] msgArr = new Message[msg.size()];
		int i = 0;
		int j = msg.size();
		while (i < j)
		{
			EmailMessage emsg = this.toEmailMessage(msg.get(i));
			if (emsg != null)
			{
				msgArr[i] = new GraphMessage(msg.get(i), emsg);
			}
			i++;
		}
		if (msgArr.length == 0)
		{
			if (debug) this.log.logMessage("getMessages: msgArr is empty, msg = " + DataTools.toObjectStringWF(msg), LogLevel.ERROR);
		}
		return msgArr;
	}

	public boolean moveMessageToArchive(String id)
	{
		GraphServiceClient client = createClient();
		if (client == null)
			return false;
		
		MovePostRequestBody body = new MovePostRequestBody();
		body.setDestinationId("archive");
		com.microsoft.graph.models.Message msg = client.users().byUserId(fromEmail).messages().byMessageId(id).move().post(body);
		if (msg != null)
		{
			return true;
		}
		return false;
	}

	public boolean deleteMessage(String id)
	{
		GraphServiceClient client = createClient();
		if (client == null)
			return false;
		client.users().byUserId(fromEmail).messages().byMessageId(id).delete();
		return true;
	}

	@Nullable
	private EmailMessage toEmailMessage(@Nonnull com.microsoft.graph.models.Message msg)
	{
		ItemBody body = msg.getBody();
		if (body == null)
			return null;
		String msgId = msg.getId();
		if (msgId == null)
			return null;
		String subject = msg.getSubject();
		String content = body.getContent();
		Boolean b;
		if (subject == null || content == null)
			return null;
		SimpleEmailMessage email = new SimpleEmailMessage(subject, content, body.getContentType() == BodyType.Html);
		if ((b = msg.getHasAttachments()) != null && b.booleanValue())
		{
			GraphServiceClient client = createClient();
			if (client == null)
				return null;
			AttachmentCollectionResponse page = client.users().byUserId(fromEmail).messages().byMessageId(msgId).attachments().get();
			if (page == null)
				return null;
			try
			{
				PageIterator<Attachment, AttachmentCollectionResponse> pageIterator = new PageIterator.Builder<Attachment, AttachmentCollectionResponse>()
				.client(client)
				.collectionPage(page)
				.collectionPageFactory(AttachmentCollectionResponse::createFromDiscriminatorValue)
				.processPageItemCallback(att -> {
					if (att instanceof FileAttachment)
					{
						Boolean bo;
						OffsetDateTime dt;
						FileAttachment fatt = (FileAttachment)att;
						EmailAttachment eatt = new EmailAttachment();
						eatt.isInline = (bo = fatt.getIsInline()) != null && bo.booleanValue();
						eatt.contentId = fatt.getContentId();
						eatt.fileName = fatt.getName();
						if ((dt = fatt.getLastModifiedDateTime()) != null)
						{
							eatt.modifyTime = dt.toZonedDateTime();
						}
						eatt.contentType = fatt.getContentType();
						eatt.content = fatt.getContentBytes();
						email.addAttachment(eatt);
					}
					else
					{
						System.out.println(DataTools.toObjectStringWF(att));
					}
					return true;
				}).build();
				pageIterator.iterate();
				return email;
			}
			catch (ApiException|ReflectiveOperationException ex)
			{
				this.log.logException(ex);
				return null;
			}
			}
		
		return email;
	}

	private void addChanged(String id, GraphMessage msg)
	{
		this.changedMap.put(id, msg);
	}
}
