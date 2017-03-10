package org.endeavourhealth.hl7receiver.engine;

import ca.uhn.hl7v2.app.Connection;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class HL7Connection {
    private Connection connection;
    private String host;
    private int port;

    public HL7Connection(Connection connection) {
        this.connection = connection;
        this.host = connection.getRemoteAddress().getHostAddress();
        this.port = connection.getRemotePort();
    }

    public HL7Connection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(host)
                .append(port)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HL7Connection))
            return false;

        if (obj == this)
            return true;

        HL7Connection rhs = (HL7Connection)obj;

        return new EqualsBuilder()
                .append(host, rhs.host)
                .append(port, rhs.port)
                .isEquals();
    }
}
