package org.sswr.util.net.email;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.AuthenticationProvider;
import org.sswr.util.data.StringUtil;
import org.sswr.util.io.LogTool;
import org.sswr.util.net.AccessTokenProvider;
import org.sswr.util.net.MSGraphUtil;
import org.sswr.util.net.MSGraphUtil.AccessTokenResult;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.AuthorizationCodeCredential;
import com.azure.identity.AuthorizationCodeCredentialBuilder;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.models.UserSendMailParameterSet;
import com.microsoft.graph.requests.GraphServiceClient;

import okhttp3.Request;

public class MSGraphEmailControl implements EmailControl
{
    private String clientId;
    private String tenantId;
    private String clientSecret;
    private String redirUrl;
    private AccessTokenResult accessToken;
    private LogTool log;

    public MSGraphEmailControl(LogTool log, String clientId, String tenantId, String clientSecret, String redirUrl)
    {
        this.log = log;
        this.clientId = clientId;
        this.tenantId = tenantId;
        this.redirUrl = redirUrl;
        this.clientSecret = clientSecret;
        updateAccessToken();
    }

    private void updateAccessToken()
    {
        if (this.accessToken != null && this.accessToken.expiresIn.getTime() < System.currentTimeMillis())
        {
            return;
        }
        this.accessToken = MSGraphUtil.getApplicationAccessToken(this.log, this.tenantId, this.clientId, this.clientSecret);
    }

    private GraphServiceClient<Request> createClient()
    {
        updateAccessToken();
        if (accessToken == null)
        {
            return null;
        }
/*        final AuthorizationCodeCredential credential = new AuthorizationCodeCredentialBuilder()
			.clientId(clientId).tenantId(tenantId).clientSecret(clientSecret).authorizationCode(accessToken.accessToken).redirectUrl(redirUrl).build();*/

//		System.out.println("code = "+authorizationCode);
/* 		final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(
			List.of("api://"+clientId+"/.default"), credential);
        TokenCredential credential2 = TokenCredential.*/

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

/*        LinkedList<InternetMessageHeader> internetMessageHeadersList = new LinkedList<InternetMessageHeader>();
        InternetMessageHeader internetMessageHeaders = new InternetMessageHeader();
        internetMessageHeaders.name = "x-custom-header-group-name";
        internetMessageHeaders.value = "Nevada";
        internetMessageHeadersList.add(internetMessageHeaders);
        InternetMessageHeader internetMessageHeaders1 = new InternetMessageHeader();
        internetMessageHeaders1.name = "x-custom-header-group-id";
        internetMessageHeaders1.value = "NV001";
        internetMessageHeadersList.add(internetMessageHeaders1);
        message.internetMessageHeaders = internetMessageHeadersList;*/

        try
        {
            GraphServiceClient<Request> client = createClient();
            if (client == null)
                return false;
            client.me()
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
