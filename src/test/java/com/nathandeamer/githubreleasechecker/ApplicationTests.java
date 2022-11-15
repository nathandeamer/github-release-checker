package com.nathandeamer.githubreleasechecker;

import com.nathandeamer.githubreleasechecker.github.CompareResult;
import com.nathandeamer.githubreleasechecker.github.GithubService;
import com.nathandeamer.githubreleasechecker.slack.SlackService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class ApplicationTests {

    @MockBean
    private GithubService githubService;

    @MockBean
    private SlackService slackService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Test
    void testOnApplicationEvent() throws Exception {
        List<CompareResult> compareResults = Collections.singletonList(CompareResult.builder().build());
        when(githubService.calculateGitDiffs()).thenReturn(compareResults);
        publisher.publishEvent(mock(ContextRefreshedEvent.class));
        verify(githubService, times(2)).calculateGitDiffs(); // First time is part of starting the app.
        verify(slackService).send(compareResults);
    }

}
