package org.sswr.util.web.spring;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface LoginController
{
	public UserDetails loadUserByUsernameNoPassword(final String username) throws UsernameNotFoundException;
}
