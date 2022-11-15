package com.nathandeamer.githubreleasechecker;

import com.nathandeamer.githubreleasechecker.github.CompareResult;
import com.nathandeamer.githubreleasechecker.github.GithubService;
import com.nathandeamer.githubreleasechecker.slack.SlackService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.util.List;

@SpringBootApplication
@RequiredArgsConstructor
public class Application {

	private final GithubService githubService;
	private final SlackService slackService;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) throws IOException {
		List<CompareResult> githubDiffs = githubService.calculateGitDiffs();
		if (!githubDiffs.isEmpty()) {
			slackService.send(githubDiffs);
		}
	}
}
