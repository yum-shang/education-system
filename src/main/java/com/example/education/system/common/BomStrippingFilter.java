package com.example.education.system.common;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * 剥离请求体开头的 UTF-8 BOM，防止 Jackson 解析 JSON 时报错。
 */
@Component
public class BomStrippingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String contentType = httpRequest.getContentType();

        if (contentType != null && contentType.contains("application/json")) {
            chain.doFilter(new BomStrippingRequestWrapper(httpRequest), response);
        } else {
            chain.doFilter(request, response);
        }
    }
}

class BomStrippingRequestWrapper extends HttpServletRequestWrapper {

    private byte[] body;

    public BomStrippingRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        InputStream is = request.getInputStream();
        PushbackInputStream pis = new PushbackInputStream(is, 3);
        byte[] bom = new byte[3];
        int read = pis.read(bom);
        if (read == 3 && bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) {
            // BOM 已跳过
        } else if (read > 0) {
            pis.unread(bom, 0, read);
        }
        this.body = pis.readAllBytes();
    }

    @Override
    public ServletInputStream getInputStream() {
        return new DelegatingServletInputStream(new ByteArrayInputStream(body));
    }
}

class DelegatingServletInputStream extends ServletInputStream {

    private final InputStream delegate;

    public DelegatingServletInputStream(InputStream delegate) {
        this.delegate = delegate;
    }

    @Override
    public int read() throws IOException {
        return delegate.read();
    }

    @Override
    public boolean isFinished() {
        try {
            return delegate.available() == 0;
        } catch (IOException e) {
            return true;
        }
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener listener) {
    }
}
