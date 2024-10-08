package org.sswr.util.net;

import java.sql.Timestamp;
import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.JSONBase;
import org.sswr.util.data.SharedInt;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;

import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.kiota.authentication.BaseBearerTokenAuthenticationProvider;

public class MSGraphUtil
{
    public static class AccessTokenResult
    {
        public String type;
        public Timestamp expiresIn;
        public Timestamp extExpiresIn;
        public String accessToken;
    }

    @Nonnull
    public static GraphServiceClient createClient(@Nonnull AccessTokenResult accessToken)
    {
		BaseBearerTokenAuthenticationProvider authProvider = new BaseBearerTokenAuthenticationProvider(new MSGraphAccessTokenProvider(accessToken.accessToken));
		return new GraphServiceClient(authProvider);
    }

    @Nullable
    public static AccessTokenResult getApplicationAccessToken(@Nullable LogTool log, @Nonnull String tenantId, @Nonnull String clientId, @Nonnull String clientSecret, @Nullable String scope)
    {
        String url = "https://login.microsoftonline.com/"+tenantId+"/oauth2/v2.0/token";
        SharedInt statusCode = new SharedInt();
        if (scope == null)
        {
            scope = "https://graph.microsoft.com/.default";
        }
        String s = HTTPOSClient.formPostAsString(url, Map.of("client_id", clientId, "scope", scope, "client_secret", clientSecret, "grant_type", "client_credentials"), statusCode, 5000);
        if (s != null && statusCode.value == 200)
        {
            AccessTokenResult token = new AccessTokenResult();
            JSONBase json = JSONBase.parseJSONStr(s);
			if (json != null)
			{
				token.type = json.getValueString("token_type");
				Timestamp t = DateTimeUtil.timestampNow();
				token.expiresIn = Timestamp.from(t.toInstant().plusSeconds(json.getValueAsInt32("expires_in")));
				token.extExpiresIn = Timestamp.from(t.toInstant().plusSeconds(json.getValueAsInt32("ext_expires_in")));
				token.accessToken = json.getValueString("access_token");
				return token;
			}
			if (log != null)
			{
				log.logMessage("Error in parsing access token: status = "+statusCode.value+", content = "+s, LogLevel.ERROR);
			}
			return null;
		}
        if (log != null)
        {
            log.logMessage("Error in getting access token: status = "+statusCode.value+", content = "+s, LogLevel.ERROR);
        }
        return null;
    }
}
