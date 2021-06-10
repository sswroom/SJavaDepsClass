package org.sswr.util.web;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;


public class JWTSessionFilter extends GenericFilterBean
{
	private JWTSessionManager sessMgr;
	public JWTSessionFilter(JWTSessionManager sessMgr)
	{
		super();
		this.sessMgr = sessMgr;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		SecurityContext securityContext = SecurityContextHolder.getContext();
		Authentication authentication = securityContext.getAuthentication();
		if (authentication == null || !authentication.getClass().equals(JWTSessionAuthentication.class))
		{
			if (request instanceof HttpServletRequest)
			{
				HttpServletRequest req = (HttpServletRequest)request;
				JWTSession sess = null;
				String tokenHdr = req.getHeader("X-Token");
				if (tokenHdr != null)
				{
					sess = this.sessMgr.getSession(tokenHdr);
				}
				if (sess == null)
				{
					Map<String, Object> params = HttpUtil.parseParams(req, null);
					Object token = params.get("Token");
					if (token != null)
					{
						sess = this.sessMgr.getSession(token.toString());
					}
				}
				if (sess != null)
				{
					securityContext.setAuthentication(new JWTSessionAuthentication(sess));
				}
			}
		}
		chain.doFilter(request, response);
	}
}
