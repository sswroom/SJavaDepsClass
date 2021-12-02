package org.sswr.util.web.spring;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.GenericFilterBean;

public class ForwardHandlerFilter extends GenericFilterBean
{
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		if (request instanceof HttpServletRequest)
		{
			request = new ForwardHandlerRequest((HttpServletRequest)request);
		}
		if (response instanceof HttpServletResponse)
		{
			response = new ForwardHandlerResponse((HttpServletRequest)request, (HttpServletResponse)response);
		}
		chain.doFilter(request, response);
	}	
}
