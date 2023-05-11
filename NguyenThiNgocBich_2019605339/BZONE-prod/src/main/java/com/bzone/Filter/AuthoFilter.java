package com.bzone.Filter;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
@Service
@Transactional
public class AuthoFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String url = req.getRequestURI();
        String role = auth.getAuthorities().toString();
        if(role.contains("ROLE_ANONYMOUS") || url.contains("login") || url.contains("signup") || url.contains("/css") || url.contains("/JS") || url.contains("/image")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        if (role.contains("Admin")) {
            if (url.contains("admin")) {
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                req.getRequestDispatcher("/admin").forward(servletRequest, servletResponse);
            }
        } else if (role.contains("Customer")) {
            if (!url.contains("admin") && !url.contains("manager") && !url.contains("shipper") && !url.contains("seller")) {
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                req.getRequestDispatcher("/").forward(servletRequest, servletResponse);
            }
        } else if (role.contains("Manager")) {
            if (url.contains("manager")) {
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                req.getRequestDispatcher("/manager").forward(servletRequest, servletResponse);
            }
        } else if (role.contains("Seller")) {
            if (url.contains("seller")) {
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                req.getRequestDispatcher("/seller").forward(servletRequest, servletResponse);
            }
        } else if (role.contains("Shipper")) {
            if (url.contains("shipper")) {
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                req.getRequestDispatcher("/shipper").forward(servletRequest, servletResponse);
            }
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
}
