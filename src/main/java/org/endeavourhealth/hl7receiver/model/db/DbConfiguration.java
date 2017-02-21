package org.endeavourhealth.hl7receiver.model.db;

import java.util.List;

public class DbConfiguration {

    private Integer instanceId;
    private List<DbChannel> dbChannels;
    private DbEds dbEds;
    private List<Integer> dbNotificationAttemptIntervalsSeconds;

    public Integer getInstanceId() {
        return instanceId;
    }

    public DbConfiguration setInstanceId(Integer instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public List<DbChannel> getDbChannels() {
        return dbChannels;
    }

    public DbConfiguration setDbChannels(List<DbChannel> dbChannels) {
        this.dbChannels = dbChannels;
        return this;
    }

    public DbEds getDbEds() {
        return dbEds;
    }

    public DbConfiguration setDbEds(DbEds dbEds) {
        this.dbEds = dbEds;
        return this;
    }

    public List<Integer> getDbNotificationAttemptIntervalsSeconds() {
        return dbNotificationAttemptIntervalsSeconds;
    }

    public DbConfiguration setDbNotificationAttemptIntervalsSeconds(List<Integer> dbNotificationAttemptIntervalsSeconds) {
        this.dbNotificationAttemptIntervalsSeconds = dbNotificationAttemptIntervalsSeconds;
        return this;
    }
}
