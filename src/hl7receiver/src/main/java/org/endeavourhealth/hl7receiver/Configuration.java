package org.endeavourhealth.hl7receiver;

import com.fasterxml.jackson.databind.JsonNode;
import com.kstruct.gethostname4j.Hostname;
import com.zaxxer.hikari.HikariDataSource;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.config.ConfigManagerException;
import org.endeavourhealth.common.postgres.logdigest.LogDigestAsyncAppender;
import org.endeavourhealth.hl7receiver.model.db.DbChannelOption;
import org.endeavourhealth.hl7receiver.model.db.DbChannelOptionType;
import org.endeavourhealth.hl7receiver.model.db.DbConfiguration;
import org.endeavourhealth.hl7receiver.model.exceptions.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public final class Configuration {
    // class members //
    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

    private static Configuration instance = null;

    public static Configuration getInstance() throws Exception {
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

    private Configuration() throws ConfigurationException {
        initialiseMachineName();
        initialiseConfigManager();
        initialiseDBConnectionPool();
        addHL7LogAppender();
        loadDbConfiguration();
    }

    private void initialiseMachineName() throws ConfigurationException {
        try {
            machineName = Hostname.getHostname();
        } catch (Exception e) {
            throw new ConfigurationException("Error getting machine name");
        }
    }

    private void initialiseConfigManager() throws ConfigurationException {
        try {
            ConfigManager.Initialize("hl7receiver");

            //new settings are stored in a single JSON structure, but old settings are stored in three separate records
            JsonNode json = ConfigManager.getConfigurationAsJson("database");
            if (json == null) {
                postgresUrl = ConfigManager.getConfiguration("postgres-url");
                postgresUsername = ConfigManager.getConfiguration("postgres-username");
                postgresPassword = ConfigManager.getConfiguration("postgres-password");

            } else {
                postgresUrl = json.get("url").asText();
                postgresUsername = json.get("username").asText();
                postgresPassword = json.get("password").asText();
            }

        } catch (Exception e) {
            throw new ConfigurationException("Error loading ConfigManager configuration", e);
        }
    }


    private void addHL7LogAppender() throws ConfigurationException {
        try {
            LogDigestAsyncAppender.addLogAppender(new DataLayer(getDatabaseConnection()));
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

    public String getChannelOptionValue(int channelId, DbChannelOptionType dbChannelOptionType) {
        DbChannelOption dbChannelOption = getChannelOption(channelId, dbChannelOptionType);

        if (dbChannelOption == null)
            return null;

        return dbChannelOption.getChannelOptionValue();
    }

    private synchronized void initialiseDBConnectionPool() throws ConfigurationException {
        try {
            if (this.dataSource == null) {

                HikariDataSource hikariDataSource = new HikariDataSource();
                hikariDataSource.setJdbcUrl(postgresUrl);
                hikariDataSource.setUsername(postgresUsername);
                hikariDataSource.setPassword(postgresPassword);
                hikariDataSource.setDriverClassName("org.postgresql.Driver");
                hikariDataSource.setMaximumPoolSize(15);
                hikariDataSource.setMinimumIdle(2);
                hikariDataSource.setIdleTimeout(60000);
                hikariDataSource.setConnectionTimeout(5000L);
                hikariDataSource.setPoolName("HL7ReceiverDBConnectionPool");

                this.dataSource = hikariDataSource;
            }
        } catch (Exception e) {
            throw new ConfigurationException("Error creating Hikari connection pool", e);
        }
    }
}