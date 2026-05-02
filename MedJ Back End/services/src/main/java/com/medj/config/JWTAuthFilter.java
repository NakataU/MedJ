//package com.medj.config;
//
//import com.medj.entities.Role;
//import com.medj.entities.UserNameBlackList;
//import io.jsonwebtoken.JwtException;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.modelmapper.internal.bytebuddy.build.Plugin;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//public class JWTAuthFilter extends OncePerRequestFilter {
//
//    private final JWTTokenProvider jwtTokenProvider;
//    private final UserNameBlackList userNameBlackList;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        String token = getJWTFromRequest(request);
//
//        if(token != null && jwtTokenProvider.validateToken(token)){
//            String username = jwtTokenProvider.getUsernameFromJWT(token);
//            Role role = jwtTokenProvider.getRoleFromJWT(token);
//
//            if(username == null || role == null){
//                throw new JwtException("Missing required JWT claims");
//            }
//
//            if(userNameBlackList.isBlacklisted(username)){
//                throw new JwtException("Black listed username");
//            }
//
//            List<GrantedAuthority> grantedAuthorityList = List.of(new SimpleGrantedAuthority(role.addPrefix(role)));
//
//            Authentication authentication = new UsernamePasswordAuthenticationToken(username,null,grantedAuthorityList);
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        }
//
//        filterChain.doFilter(request,response);
//    }
//
//    private String getJWTFromRequest(HttpServletRequest request){
//        String bearerToken = request.getHeader("Authorization");
//
//        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
//            return bearerToken.substring("Bearer ".length());
//        }
//
//        return null;
//    }
//}
