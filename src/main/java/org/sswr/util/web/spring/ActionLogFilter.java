package org.sswr.util.web.spring;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.sswr.util.basic.ThreadVar;

@Component
public class ActionLogFilter extends GenericFilterBean
{
	public ActionLogFilter()
	{
		super();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
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
