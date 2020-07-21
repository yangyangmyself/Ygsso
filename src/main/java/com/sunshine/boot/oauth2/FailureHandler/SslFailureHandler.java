package com.sunshine.boot.oauth2.FailureHandler;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

public class SslFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String return_uri = request.getParameter("post_logout_redirect_uri");
        if (StringUtils.isBlank(return_uri)) {
            return_uri = getLocalServerUrl(request) + "/login/pkilogin.html";
        }
        String error = "PKI登录失败！";
        error = URLEncoder.encode(error,"UTF-8");
        response.sendRedirect(return_uri + "?error=" + error);
    }

    protected String getLocalServerUrl(HttpServletRequest request){
        String contextPath = request.getContextPath();
        String requestURI = request.getRequestURI();
        String localServerUrl = request.getRequestURL().toString();
        int indexPos = localServerUrl.indexOf(requestURI);
        if (indexPos > 0){
            localServerUrl = localServerUrl.substring(0, (indexPos + contextPath.length()));
        }
        return localServerUrl;
    }
}
