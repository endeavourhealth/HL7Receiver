package org.endeavourhealth.hl7receiver;

import com.zaxxer.hikari.HikariDataSource;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.config.ConfigManagerException;
import org.endeavourhealth.common.postgres.logdigest.LogDigestAppender;
import org.endeavourhealth.hl7receiver.model.db.DbChannelOption;
import org.endeavourhealth.hl7receiver.model.db.DbChannelOptionType;
import org.endeavourhealth.hl7receiver.model.db.DbConfiguration;
import org.endeavourhealth.hl7receiver.model.exceptions.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public final class Configuration
{
    // class members //
    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
    private static final String PROGRAM_CONFIG_MANAGER_NAME = "hl7receiver";
    private static final String POSTGRES_URL_CONFIG_MANAGER_KEY = "postgres-url";
    private static final String POSTGRES_USERNAME_CONFIG_MANAGER_KEY = "postgres-username";
    private static final String POSTGRES_PASSWORD_CONFIG_MANAGER_KEY = "postgres-password";

    private static Configuration instance = null;

    public static Configuration getInstance() throws Exception
    {
        if (instance == null)
            instance = new Configuration();

        return instance;
    }

    // instance members //
    private DbConfiguration dbConfiguration;
    private String machineName;
    private String postgresUrl;
    private String postgresUsername;
    private String postgresPassword;
    private DataSource dataSource;

    private Configuration() throws ConfigurationException
    {
        initialiseMachineName();
        initialiseDBConnectionPool();
        initialiseConfigManager();
        addHL7LogAppender();
        loadDbConfiguration();
    }

    private void initialiseMachineName() throws ConfigurationException {
        try {
            machineName = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            throw new ConfigurationException("Error getting machine name");
        }
    }

    private void initialiseConfigManager() throws ConfigurationException {
        try {
            ConfigManager.Initialize(PROGRAM_CONFIG_MANAGER_NAME);

            postgresUrl = ConfigManager.getConfiguration(POSTGRES_URL_CONFIG_MANAGER_KEY);
            postgresUsername = ConfigManager.getConfiguration(POSTGRES_USERNAME_CONFIG_MANAGER_KEY);
            postgresPassword = ConfigManager.getConfiguration(POSTGRES_PASSWORD_CONFIG_MANAGER_KEY);

        } catch (ConfigManagerException e) {
            throw new ConfigurationException("Error loading ConfigManager configuration", e);
        }
    }

    private void addHL7LogAppender() throws ConfigurationException {
        try {
            LogDigestAppender.addLogAppender(new DataLayer(getDatabaseConnection()));
        } catch (Exception e) {
            throw new ConfigurationException("Error adding HL7 log appender", e);
        }
    }

    private void loadDbConfiguration() throws ConfigurationException {
        try {
            DataLayer dataLayer = new DataLayer(getDatabaseConnection());
            this.dbConfiguration = dataLayer.getConfiguration(getMachineName());
        } catch (Exception e) {
            throw new ConfigurationException("Error loading DB configuration, see inner exception", e);
        }
    }

    public DataSource getDatabaseConnection() throws SQLException {
        return this.dataSource;
    }

    public String getMachineName()
    {
        return machineName;
    }

    public DbConfiguration getDbConfiguration() {
        return dbConfiguration;
    }

    public int getInstanceId() {
        return this.dbConfiguration.getInstanceId();
    }

    public DbChannelOption getChannelOption(int channelId, DbChannelOptionType dbChannelOptionType) {
        if (this.dbConfiguration == null)
            return null;

        if (this.dbConfiguration.getDbChannelOptions() == null)
            return null;

        List<DbChannelOption> dbChannelOptions = this.dbConfiguration
                .getDbChannelOptions()
                .stream()
                .filter(t -> t.getChannelId() == channelId && t.getChannelOptionType().equals(dbChannelOptionType))
                .collect(Collectors.toList());

        if (dbChannelOptions.size() == 0)
            return null;

        return dbChannelOptions.get(0);
    }

    private synchronized void initialiseDBConnectionPool() throws ConfigurationException {
        try {
            if (this.dataSource == null) {

                HikariDataSource hikariDataSource = new HikariDataSource();
                hikariDataSource.setJdbcUrl(postgresUrl);
                hikariDataSource.setUsername(postgresUsername);
                hikariDataSource.setPassword(postgresPassword);
                hikariDataSource.setMaximumPoolSize(15);
                hikariDataSource.setMinimumIdle(2);
                hikariDataSource.setIdleTimeout(60000);
                hikariDataSource.setPoolName("HL7ReceiverDBConnectionPool");

                this.dataSource = hikariDataSource;
            }
        } catch (Exception e) {
            throw new ConfigurationException("Error creating Hikari connection pool", e);
        }
    }
}