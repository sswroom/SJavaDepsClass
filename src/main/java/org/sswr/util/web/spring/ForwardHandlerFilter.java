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
	private String scheme;
	private String host;

	public ForwardHandlerFilter()
	{
		this.scheme = null;
		this.host = null;
	}

	public ForwardHandlerFilter(String scheme, String host)
	{
		this.scheme = scheme;
		this.host = host;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		if (request instanceof HttpServletRequest)
		{
			ForwardHandlerRequest req = new ForwardHandlerRequest((HttpServletRequest)request);
			if (this.scheme != null)
			{
				req.setScheme(this.scheme);
			}
			if (this.host != null)
			{
				req.setHost(this.host);
			}
			request = req;
		}
		if (response instanceof HttpServletResponse)
		{
			response = new ForwardHandlerResponse((HttpServletRequest)request, (HttpServletResponse)response);
		}
		chain.doFilter(request, response);
	}	
}
