package com.dambrz.projectmanagementsystemapi.security;

import com.dambrz.projectmanagementsystemapi.service.CustomUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.dambrz.projectmanagementsystemapi.exceptions.ExceptionMessageContent.COULD_NOT_SET_USER_AUTHENTICATION_IN_SECURITY_CONTEXT;
import static com.dambrz.projectmanagementsystemapi.security.SecurityConstraints.HEADER_STRING;
import static com.dambrz.projectmanagementsystemapi.security.SecurityConstraints.TOKEN_PREFIX;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JWTProvider jwtProvider;
    private  final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JWTProvider jwtProvider, CustomUserDetailsService userDetailsService) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(httpServletRequest);
            if (StringUtils.hasText(jwt) && jwtProvider.isTokenValid(jwt)) {
                String username = jwtProvider.getUsernameFromToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception exc) {
            logger.error(COULD_NOT_SET_USER_AUTHENTICATION_IN_SECURITY_CONTEXT, exc);
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_STRING);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
