package com.example.lly.module.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@Slf4j
@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private UserDetailsService userDetailsService;


    //Authentication的type为UsernamePasswordAuthenticationToken
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String jwtToken = request.getHeader(JwtTokenUtil.TOKEN_HEADER);
        if(StringUtils.isNotEmpty(jwtToken)) {
            String username = JwtTokenUtil.getUsernameFromToken(jwtToken);
            if(username != null && SecurityContextHolder.getContext().getAuthentication() != null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                //验证账号是否一直和token是否过期
                if(JwtTokenUtil.validateToken(jwtToken, userDetails)) {
                    System.out.println("这个页面进行验证了！");
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    Authentication authentication = getAuthentication(jwtToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("header " + authentication.getPrincipal());
                    System.out.println("header " + authentication.getAuthorities());
                }
            }
        }
        chain.doFilter(request, response);
    }


    private UsernamePasswordAuthenticationToken getAuthentication(String tokenHeader) {
        String token = tokenHeader.replace(JwtTokenUtil.TOKEN_PREFIX, "");
        String username = JwtTokenUtil.getUsernameFromToken(token);
        Collection<? extends GrantedAuthority> roles = JwtTokenUtil.getUserRoleFromToken(token);
        if(username != null) {
            return new UsernamePasswordAuthenticationToken(username, null, roles);
        }
        return null;
    }

}
