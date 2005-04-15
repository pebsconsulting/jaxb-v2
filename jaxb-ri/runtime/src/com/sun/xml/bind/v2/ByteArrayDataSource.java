package com.sun.xml.bind.v2;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * {@link DataSource} backed by a byte buffer.
 * 
 * @author Kohsuke Kawaguchi
 */
public final class ByteArrayDataSource implements DataSource {

    private final String contentType;
    private final byte[] buf;

    public ByteArrayDataSource(byte[] buf, String contentType) {
        this.buf = buf;
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(buf);
    }

    public String getName() {
        return null;
    }

    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException();
    }
}
