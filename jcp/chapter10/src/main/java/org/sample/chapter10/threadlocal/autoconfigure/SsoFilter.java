package org.sample.chapter10.threadlocal.autoconfigure;


import lombok.extern.slf4j.Slf4j;
import org.sample.chapter10.threadlocal.LoginContext;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

@Slf4j
public class SsoFilter implements Filter {

    private final static String X_SSO_USER_NAME = "X-SSO-USER-NAME";
    private final static String X_SSO_FULL_NAME = "X-SSO-FULL-NAME";

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getServletPath();
        if (path.endsWith(".js") || path.endsWith(".css") || path.endsWith(".png")) {
            chain.doFilter(request, response);
        } else {
            String userName = req.getHeader(X_SSO_USER_NAME);
            String fullName = req.getHeader(X_SSO_FULL_NAME);

            if (StringUtils.hasLength(userName)) {
                LoginContext loginContext = new LoginContext(userName, URLDecoder.decode(fullName,"utf-8"));
                LoginContext.setLoginContext(loginContext);
                try {
                    chain.doFilter(request, response);
                } finally {
                    LoginContext.setLoginContext(null);
                }
            } else {
                // 重定向到登录页面
                HttpServletResponse resp = (HttpServletResponse) response;
                resp.sendRedirect("/login");
            }
        }
    }
}
