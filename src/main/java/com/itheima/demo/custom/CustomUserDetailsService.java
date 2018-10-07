package com.itheima.demo.custom;


import java.util.HashSet;
import java.util.Set;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CustomUserDetailsService implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {
	@Override
	public UserDetails loadUserDetails(CasAssertionAuthenticationToken token) throws UsernameNotFoundException {
		String username = token.getName();
		System.out.println("当前的用户名是："+username);
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		if("zhangsan".equalsIgnoreCase(username)) {
			authorities.add(new SimpleGrantedAuthority("TEST"));
			authorities.add(new SimpleGrantedAuthority("ADMIN"));
		} else {
			authorities.add(new SimpleGrantedAuthority("TEST"));
		}
		User user = new User(username, "", authorities);
		return user;
	}

	
	
}
