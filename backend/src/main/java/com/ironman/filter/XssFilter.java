package com.ironman.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(new XssRequestWrapper((HttpServletRequest) request), response);
    }

    private static class XssRequestWrapper extends HttpServletRequestWrapper {

        public XssRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String[] getParameterValues(String parameter) {
            String[] values = super.getParameterValues(parameter);
            if (values == null) {
                return null;
            }

            int count = values.length;
            String[] encodedValues = new String[count];
            for (int i = 0; i < count; i++) {
                encodedValues[i] = cleanXSS(values[i]);
            }

            return encodedValues;
        }

        @Override
        public String getParameter(String parameter) {
            String value = super.getParameter(parameter);
            return cleanXSS(value);
        }

        @Override
        public String getHeader(String name) {
            String value = super.getHeader(name);
            return cleanXSS(value);
        }

        private String cleanXSS(String value) {
            if (value == null) {
                return null;
            }

            // Use Apache Commons Text for HTML escaping
            value = StringEscapeUtils.escapeHtml4(value);

            // Remove script tags
            value = value.replaceAll("<script>(.*?)</script>", "");
            value = value.replaceAll("</script>", "");
            value = value.replaceAll("<script(.*?)>", "");
            value = value.replaceAll("eval\\((.*?)\\)", "");
            value = value.replaceAll("expression\\((.*?)\\)", "");
            value = value.replaceAll("javascript:", "");
            value = value.replaceAll("vbscript:", "");
            value = value.replaceAll("onload(.*?)=", "");

            return value;
        }
    }
}