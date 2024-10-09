package org.sswr.util.net;

import java.net.URI;
import java.util.Map;

import com.microsoft.kiota.authentication.AccessTokenProvider;
import com.microsoft.kiota.authentication.AllowedHostsValidator;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class MSGraphAccessTokenProvider implements AccessTokenProvider {
    private String accessToken;
    public MSGraphAccessTokenProvider(@Nonnull String accessToken) {
        this.accessToken = accessToken;
    }

    private final AllowedHostsValidator validator = new AllowedHostsValidator("graph.microsoft.com");
    @Override
	@Nonnull
    public AllowedHostsValidator getAllowedHostsValidator() {
        return validator;
    }

	@Override
	@Nonnull
	public String getAuthorizationToken(@Nonnull URI arg0, @Nullable Map<String, Object> arg1) {
		return this.accessToken;
	}
}
