package com.example.taskmanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rewrite /api/v1/... → /api/... TRƯỚC khi tới Security / RateLimit / handler mapping.
 * Nhờ đó client mới có thể gọi version-explicit (/api/v1/tasks) mà không phải nhân
 * đôi mọi security rule / handler. App cũ vẫn gọi /api/... và đi thẳng (= v1 mặc định).
 *
 * v2+ KHÔNG được rewrite — chúng là endpoint mới có hành vi khác (vd validate
 * password mạnh hơn ở /api/v2/auth/register).
 *
 * Chạy với precedence cao hơn {@link RateLimitFilter} để RateLimit & Security đều
 * thấy URI đã được normalize về dạng v1 mặc định.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiVersionAliasFilter extends OncePerRequestFilter {

    private static final String V1_PREFIX = "/api/v1/";
    private static final String CANONICAL_PREFIX = "/api/";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri != null && uri.startsWith(V1_PREFIX)) {
            String rewritten = CANONICAL_PREFIX + uri.substring(V1_PREFIX.length());
            chain.doFilter(new RewrittenUriRequest(request, rewritten), response);
            return;
        }
        chain.doFilter(request, response);
    }

    /** Wrapper trả về URI/servletPath đã rewrite. Giữ nguyên query string. */
    private static final class RewrittenUriRequest extends HttpServletRequestWrapper {
        private final String rewrittenUri;

        RewrittenUriRequest(HttpServletRequest request, String rewrittenUri) {
            super(request);
            this.rewrittenUri = rewrittenUri;
        }

        @Override
        public String getRequestURI() {
            return rewrittenUri;
        }

        @Override
        public String getServletPath() {
            return rewrittenUri;
        }

        @Override
        public StringBuffer getRequestURL() {
            HttpServletRequest original = (HttpServletRequest) getRequest();
            StringBuffer url = new StringBuffer();
            url.append(original.getScheme()).append("://").append(original.getServerName());
            int port = original.getServerPort();
            if (port > 0 && port != 80 && port != 443) {
                url.append(':').append(port);
            }
            url.append(rewrittenUri);
            return url;
        }
    }
}
