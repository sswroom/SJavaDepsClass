package org.sswr.util.web.spring;

import java.io.IOException;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import org.sswr.util.basic.ThreadVar;

public class ActionLogFilter extends GenericFilterBean
{
	public ActionLogFilter()
	{
		super();
	}

	@Override
	public void doFilter(@Nonnull ServletRequest request, @Nonnull ServletResponse response, @Nonnull FilterChain chain) throws IOException, ServletException
	{
		ThreadVar.set("User", null);;
		try
		{
			SecurityContext securityContext = SecurityContextHolder.getContext();
			Authentication authentication = securityContext.getAuthentication();
			
			if (authentication != null)
			{
				String username = authentication.getName();
				ThreadVar.set("User", username);
			}
		
			chain.doFilter(request, response);
			ThreadVar.set("User", null);;
		}
		catch (IOException ex)
		{
			ThreadVar.set("User", null);;
			throw ex;
		}
		catch (ServletException ex)
		{
			ThreadVar.set("User", null);;
			throw ex;
		}
	}	
}
