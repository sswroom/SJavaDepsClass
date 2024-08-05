package org.sswr.util.web.spring;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class OTPWebAuthenticationDetails extends WebAuthenticationDetails
{
    private String verificationCode;

    public OTPWebAuthenticationDetails(HttpServletRequest request, String otpCodeField)
	{
        super(request);
        this.verificationCode = request.getParameter(otpCodeField);
    }

    public String getVerificationCode()
	{
        return this.verificationCode;
    }
}
