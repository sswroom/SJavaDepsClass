package org.sswr.util.web.spring;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.GenericFilterBean;

public class RequestLogFilter extends GenericFilterBean
{
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		if (request instanceof HttpServletRequest)
		{
			request = new LogHttpRequest((HttpServletRequest)request);
		}
		if (response instanceof HttpServletResponse)
		{
			response = new LogHttpResponse((HttpServletResponse)response);
		}
		chain.doFilter(request, response);
	}	
}
