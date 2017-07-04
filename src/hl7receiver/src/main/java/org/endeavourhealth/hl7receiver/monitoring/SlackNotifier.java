package org.endeavourhealth.hl7receiver.monitoring;

import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7receiver.Configuration;
import org.slf4j.LoggerFactory;

public class SlackNotifier {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SlackNotifier.class);

    private Configuration configuration;
    private String slackUrl;

    public SlackNotifier(Configuration configuration) {
        Validate.notNull(configuration, "configuration");

        this.configuration = configuration;
    }

    private void postMessage(String slackMessage) {
        try {
            if (StringUtils.isEmpty(slackUrl))
                return;

            LOG.info("Posting message to slack: '" + slackMessage + "'");

            SlackApi slackApi = new SlackApi(slackUrl);
            slackApi.call(new SlackMessage(slackMessage));

        } catch (Exception e) {
            LOG.warn("Error posting message to slack", e);
        }
    }
}
