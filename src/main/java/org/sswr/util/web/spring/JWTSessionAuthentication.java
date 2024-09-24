package org.sswr.util.web.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.sswr.util.data.DataTools;
import org.sswr.util.web.JWTSession;

import jakarta.annotation.Nonnull;

public class JWTSessionAuthentication implements Authentication
{
	private JWTSession sess;
	private List<SimpleGrantedAuthority> authorities;

	public JWTSessionAuthentication(@Nonnull JWTSession sess)
	{
		this.sess = sess;
		List<String> roles = this.sess.getRoleList();
		int i = 0;
		int j = roles.size();
		this.authorities = new ArrayList<SimpleGrantedAuthority>();
		while (i < j)
		{
			this.authorities.add(new SimpleGrantedAuthority(roles.get(i)));
			i++;
		}
	}

	@Override
	public String getName() {
		return this.sess.getUserName();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getDetails() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return this.sess;
	}


	@Override
	public boolean isAuthenticated() {
		return true;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
	}

	@Override
	public String toString()
	{
		return DataTools.toObjectString(this.sess);
	}
}
