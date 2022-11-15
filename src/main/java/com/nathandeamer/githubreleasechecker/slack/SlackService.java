package com.nathandeamer.githubreleasechecker.slack;

import com.nathandeamer.githubreleasechecker.github.CompareResult;
import com.slack.api.Slack;
import com.slack.api.model.block.ContextBlock;
import com.slack.api.model.block.DividerBlock;
import com.slack.api.model.block.HeaderBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.webhook.Payload;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SlackService {

    @Value("${slack.webhookUrl}")
    private String webhookUrl;

    @Value("${github.tag}")
    private String tag;

    public void send(List<CompareResult> githubDiffs) throws IOException {
        Slack slack = Slack.getInstance();
        List<LayoutBlock> blocks = new ArrayList<>();
        blocks.add(HeaderBlock.builder().text(PlainTextObject.builder().text("Github Release Checker").build()).build());
        blocks.add(DividerBlock.builder().build());
        blocks.addAll(calculateBlocks(githubDiffs));
        blocks.add(DividerBlock.builder().build());
        blocks.add(ContextBlock.builder()
                .elements(Collections.singletonList(MarkdownTextObject.builder()
                                .text("<https://github.com/nathandeamer/github-release-checker|github-release-checker> by <https://www.linkedin.com/in/nathandeamer/|Nathan Deamer>")
                        .build()))
                .build());

        slack.send(webhookUrl, Payload.builder().blocks(blocks).build());
    }

    private List<SectionBlock> calculateBlocks(List<CompareResult> githubDiffs) {
        return githubDiffs.stream()
                .map(gh -> SectionBlock.builder().text(MarkdownTextObject.builder().text(String.format("<%s|%s> is %s commits behind %s", gh.getLink(), gh.getName(), gh.getBy(), tag)).build()).build())
                .collect(Collectors.toList());
    }

}
