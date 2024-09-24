package org.sswr.util.net;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

import com.microsoft.graph.authentication.BaseAuthenticationProvider;

import javax.annotation.Nonnull;

public class AccessTokenProvider extends BaseAuthenticationProvider {
    private String accessToken;
    public AccessTokenProvider(@Nonnull String accessToken) {
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
    public @Nonnull CompletableFuture<String> getAuthorizationTokenAsync(@Nonnull final URL requestUrl){
        return CompletableFuture.completedFuture(accessToken);
    }
}
