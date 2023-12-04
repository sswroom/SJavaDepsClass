package org.sswr.util.net;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

import com.microsoft.graph.authentication.BaseAuthenticationProvider;

public class AccessTokenProvider extends BaseAuthenticationProvider {
    private String accessToken;
    public AccessTokenProvider(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * This implementation of the IAuthenticationProvider helps injects the Graph access
     * token into the headers of the request that GraphServiceClient makes.
     *
     * @param requestUrl the outgoing request URL
     * @return a future with the token
     */
    @Override
    public CompletableFuture<String> getAuthorizationTokenAsync(@Nonnull final URL requestUrl){
        return CompletableFuture.completedFuture(accessToken);
    }
}
