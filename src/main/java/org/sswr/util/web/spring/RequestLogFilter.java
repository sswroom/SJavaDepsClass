package org.sswr.util.web.spring;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
