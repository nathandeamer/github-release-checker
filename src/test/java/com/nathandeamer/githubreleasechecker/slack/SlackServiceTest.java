package com.nathandeamer.githubreleasechecker.slack;

import com.nathandeamer.githubreleasechecker.github.CompareResult;
import com.slack.api.Slack;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.webhook.Payload;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SlackServiceTest {

    private static final String SLACK_WEBHOOK_URL = "https://hooks.slack.com/services/NotTheRealWebhookUrl";
    private static final String PROD_TAG = "prod";
    private static final String GITHUB_HTML_URL = "https://github.com/nathandeamer/github-release-checker";
    private static final String REPO_NAME = "repo1";
    private static final int BEHIND_BY = 1;

    @Test
    public void shouldSendSlackNotification() throws Exception {
        ArgumentCaptor<Payload> slackPayloadCaptor = ArgumentCaptor.forClass(Payload.class);

        Slack slack = mock(Slack.class);
        SlackService slackService = new SlackService(SLACK_WEBHOOK_URL,PROD_TAG, slack);

        slackService.send(Collections.singletonList(CompareResult.builder()
                        .name(REPO_NAME)
                        .link(GITHUB_HTML_URL)
                        .by(BEHIND_BY)
                .build()));

        verify(slack).send(eq(SLACK_WEBHOOK_URL), slackPayloadCaptor.capture());

        Payload slackPayload = slackPayloadCaptor.getValue();

        assertThat(slackPayload.getBlocks()).hasSize(5);
        assertThat(slackPayload.getBlocks()).contains(
                SectionBlock.builder().text(
                        MarkdownTextObject.builder()
                                .text(String.format(String.format("<%s|%s> is %s commits behind %s", GITHUB_HTML_URL, REPO_NAME, BEHIND_BY, PROD_TAG)))
                                .build())
                        .build());
    }

}