package org.endeavourhealth.hl7receiver.monitoring;

import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.model.db.DbChannelOption;
import org.endeavourhealth.hl7receiver.model.db.DbChannelOptionType;
import org.slf4j.LoggerFactory;

public class SlackNotifier {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SlackNotifier.class);

    private String slackUrl;

    public SlackNotifier(Configuration configuration, int channelId) {
        Validate.notNull(configuration, "configuration");

        this.slackUrl = configuration.getChannelOptionValue(channelId, DbChannelOptionType.SLACK_URL);
    }

    public void postMessage(String message, String attachment) {
        try {
            if (StringUtils.isEmpty(slackUrl))
                return;

            String logMessage = "Posting message to slack: '" + message + "'";

            if (StringUtils.isNotEmpty(attachment))
                logMessage += ", with attachment: '" + attachment + "'";

            LOG.info(logMessage);

            SlackMessage slackMessage = new SlackMessage(message);

            if (StringUtils.isNotEmpty(attachment)) {
                SlackAttachment slackAttachment = new SlackAttachment()
                        .setFallback("Attachment cannot be displayed")
                        .setText("```" + attachment + "```")
                        .addMarkdownAttribute("text");

                slackMessage.addAttachments(slackAttachment);
            }

            SlackApi slackApi = new SlackApi(slackUrl);
            slackApi.call(slackMessage);

        } catch (Exception e) {
            LOG.warn("Error posting message to slack", e);
        }
    }

    public void postMessage(String slackMessage) {
        postMessage(slackMessage, null);
    }
}
