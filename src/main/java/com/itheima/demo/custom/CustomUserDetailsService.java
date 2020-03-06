package com.itheima.demo.custom;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.itheima.demo.entity.UserAccount;
import com.itheima.demo.service.UserAccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class CustomUserDetailsService implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {

	private final UserAccountService userAccountService;

	@Override
	public UserDetails loadUserDetails(CasAssertionAuthenticationToken token) throws UsernameNotFoundException {
		String username = token.getName();
		log.info("当前的用户名是："+username);
		List<GrantedAuthority> authorities=new ArrayList<>();
		UserAccount user = userAccountService.findByUsername(username);
		if (user==null){
			throw new UsernameNotFoundException(username+"不存在");
		}
		if("13906160328".equalsIgnoreCase(username)) {
			authorities.add(new SimpleGrantedAuthority("TEST"));
			authorities.add(new SimpleGrantedAuthority("ADMIN"));
		} else {
			authorities.add(new SimpleGrantedAuthority("TEST"));
		}
		user.setGrantedAuthorities(authorities);
		return user;
	}



}
