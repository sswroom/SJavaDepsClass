package org.sswr.util.web.spring;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class OTPWebAuthenticationDetails extends WebAuthenticationDetails
{
    private String verificationCode;

    public OTPWebAuthenticationDetails(@Nonnull HttpServletRequest request, @Nonnull String otpCodeField)
	{
        super(request);
        this.verificationCode = request.getParameter(otpCodeField);
    }

    @Nullable
    public String getVerificationCode()
	{
        return this.verificationCode;
    }
}
