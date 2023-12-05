package org.sswr.util.net;

import java.sql.Timestamp;
import java.util.Map;

import javax.annotation.Nonnull;

import org.sswr.util.data.JSONBase;
import org.sswr.util.data.SharedInt;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;

public class MSGraphUtil
{
    public static class AccessTokenResult
    {
        public String type;
        public Timestamp expiresIn;
        public Timestamp extExpiresIn;
        public String accessToken;
    }

    public static AccessTokenResult getApplicationAccessToken(LogTool log, @Nonnull String tenantId, @Nonnull String clientId, @Nonnull String clientSecret, String scope)
    {
        String url = "https://login.microsoftonline.com/"+tenantId+"/oauth2/v2.0/token";
        SharedInt statusCode = new SharedInt();
        if (scope == null)
        {
            scope = "https://graph.microsoft.com/.default";
        }
        String s = HTTPMyClient.formPostAsString(url, Map.of("client_id", clientId, "scope", scope, "client_secret", clientSecret, "grant_type", "client_credentials"), statusCode, 5000);
        if (s != null && statusCode.value == 200)
        {
            AccessTokenResult token = new AccessTokenResult();
            JSONBase json = JSONBase.parseJSONStr(s);
            token.type = json.getValueString("token_type");
            long t = System.currentTimeMillis();
            token.expiresIn = new Timestamp(t + json.getValueAsInt32("expires_in") * 1000);
            token.extExpiresIn = new Timestamp(t + json.getValueAsInt32("ext_expires_in") * 1000);
            token.accessToken = json.getValueString("access_token");
            return token;
        }
        if (log != null)
        {
            log.logMessage("Error in getting access token: status = "+statusCode.value+", content = "+s, LogLevel.ERROR);
        }
        return null;
    }
}
