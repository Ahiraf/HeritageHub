package com.HeritageHub.security;

import com.HeritageHub.repository.AdminRepository;
import com.HeritageHub.repository.ConsumerRepository;
import com.HeritageHub.repository.SellerRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "X-API-KEY";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final AdminRepository adminRepository;
    private final SellerRepository sellerRepository;
    private final ConsumerRepository consumerRepository;

    public ApiKeyAuthFilter(AdminRepository adminRepository,
                            SellerRepository sellerRepository,
                            ConsumerRepository consumerRepository) {
        this.adminRepository = adminRepository;
        this.sellerRepository = sellerRepository;
        this.consumerRepository = consumerRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return PATH_MATCHER.match("/auth/**", path)
                || PATH_MATCHER.match("/css/**", path)
                || PATH_MATCHER.match("/js/**", path)
                || PATH_MATCHER.match("/images/**", path)
                || PATH_MATCHER.match("/*.html", path)
                || PATH_MATCHER.match("/", path)
                || PATH_MATCHER.match("/error", path)
                || PATH_MATCHER.match("/h2-console/**", path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String apiKey = request.getHeader(HEADER_NAME);
        if (!StringUtils.hasText(apiKey)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing API key");
            return;
        }

        var admin = adminRepository.findByApiKey(apiKey);
        if (admin.isPresent()) {
            authenticate("admin:" + admin.get().getId(), apiKey, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
            filterChain.doFilter(request, response);
            return;
        }

        var seller = sellerRepository.findByApiKey(apiKey);
        if (seller.isPresent()) {
            authenticate("seller:" + seller.get().getSellerNid(), apiKey, List.of(new SimpleGrantedAuthority("ROLE_SELLER")));
            filterChain.doFilter(request, response);
            return;
        }

        var consumer = consumerRepository.findByApiKey(apiKey);
        if (consumer.isPresent()) {
            authenticate("consumer:" + consumer.get().getConsumerNid(), apiKey, List.of(new SimpleGrantedAuthority("ROLE_CONSUMER")));
            filterChain.doFilter(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API key");
    }

    private void authenticate(String principal, String apiKey, List<GrantedAuthority> authorities) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, apiKey, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
