package org.endeavourhealth.hl7receiver.hl7;

import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionListener;
import ca.uhn.hl7v2.protocol.MetadataKeys;
import org.endeavourhealth.common.postgres.PgStoredProcException;
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.DataLayer;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class HL7ConnectionManager implements ConnectionListener {
    private static final Logger LOG = LoggerFactory.getLogger(HL7ConnectionManager.class);

    private Configuration configuration;
    private DbChannel dbChannel;
    private DataLayer dataLayer;
    private ConcurrentHashMap<HL7Connection, Integer> connectionIds = new ConcurrentHashMap<>();

    private HL7ConnectionManager() {
    }

    public HL7ConnectionManager(Configuration configuration, DbChannel dbChannel) throws SQLException {
        this.configuration = configuration;
        this.dbChannel = dbChannel;
        this.dataLayer = new DataLayer(configuration.getDatabaseConnection());
    }

    public void closeConnections() {
        for (HL7Connection connection : connectionIds.keySet())
            connection.getConnection().close();
    }

    public Integer getConnectionId(String ipAddress, int port) {
        return connectionIds.get(new HL7Connection(ipAddress, port));
    }

    public static String getRemoteHost(Map<String, Object> map) {
        return (String)map.get(MetadataKeys.IN_SENDING_IP);
    }

    public static Integer getRemotePort(Map<String, Object> map) {
        return (Integer)map.get(MetadataKeys.IN_SENDING_PORT);
    }

    public synchronized Integer getConnectionId(Map<String, Object> map) {
        if (map == null)
            return null;

        String remoteHost = getRemoteHost(map);
        Integer remotePort = getRemotePort(map);

        return getConnectionId(remoteHost, remotePort);
    }

    public synchronized void connectionReceived(Connection connection) {
        String remoteHost = connection.getRemoteAddress().getHostAddress();
        Integer remotePort = connection.getRemotePort();

        LOG.info("Connection opened on channel " + dbChannel.getChannelName() + " from remote host " + remoteHost + " using remote port " + remotePort.toString());

        Integer connectionId = null;

        try {
            connectionId = dataLayer.openConnection(configuration.getDbConfiguration().getInstanceId(), dbChannel.getChannelId(), dbChannel.getPortNumber(), remoteHost, remotePort);

        } catch (PgStoredProcException e) {
            LOG.error("Could not write new connection to database for channel {}. Closing inbound connection", new Object[] { dbChannel.getChannelName(), e });
            connection.close();
        }

        if (connectionId != null)
            this.connectionIds.put(new HL7Connection(connection), connectionId);
    }

    public synchronized void connectionDiscarded(Connection connection) {

        String remoteHost = connection.getRemoteAddress().getHostAddress();
        Integer remotePort = connection.getRemotePort();

        LOG.info("Connection closed on channel {} from remote host {} using remote port {}", new Object[] { dbChannel.getChannelName(), remoteHost, remotePort.toString()});

        Integer connectionId = this.connectionIds.remove(new HL7Connection(connection));

        try {
            dataLayer.closeConnection(connectionId);
        } catch (Exception e) {
            LOG.error("Could not close connection on channel {}", new Object[] { dbChannel.getChannelName(), e });
        }
    }
}
