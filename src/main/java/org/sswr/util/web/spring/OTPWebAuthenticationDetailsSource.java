package org.sswr.util.web.spring;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class OTPWebAuthenticationDetailsSource implements AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails>
{
	private String otpCodeField;

	public OTPWebAuthenticationDetailsSource(@Nonnull String otpCodeField)
	{
		this.otpCodeField = otpCodeField;
	}

    @Override
	@Nonnull
    public WebAuthenticationDetails buildDetails(HttpServletRequest context)
	{
        return new OTPWebAuthenticationDetails(context, otpCodeField);
    }
}
