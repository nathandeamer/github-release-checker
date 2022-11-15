package com.nathandeamer.githubreleasechecker.github;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.kohsuke.github.GHCompare;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GithubService {

    private final String organisation;
    private final String user;
    private final String tag;
    private final GitHub gitHub;

    public GithubService(@Value("${github.organisation}") String organisation,
                         @Value("${github.user}") String user,
                         @Value("${github.tag}") String tag,
                         @Value("${github.token}") String token) throws IOException {
        this.organisation = organisation;
        this.user = user;
        this.tag = tag;
        this.gitHub = new GitHubBuilder().withOAuthToken(token).build();
    }

    public List<CompareResult> calculateGitDiffs() throws IOException {
        Map<String, GHRepository> allRepos;
        if (Strings.isNotEmpty(organisation)) {
            allRepos = gitHub.getOrganization(organisation).getRepositories();
        } else {
            allRepos = gitHub.getUser(user).getRepositories();
        }
        return calculateGitDiffs(allRepos);
    }

    private List<CompareResult> calculateGitDiffs(Map<String, GHRepository> allRepos) {
        return allRepos.values().stream()
                .map(this::toGithubCompareResult)
                .filter(Objects::nonNull)
                .filter(r -> r.getBy() > 0)
                .sorted(Comparator.comparingInt(CompareResult::getBy).reversed())
                .collect(Collectors.toList());
    }

    private CompareResult toGithubCompareResult(GHRepository ghRepository) {
        try {
            GHCompare ghCompare = ghRepository.getCompare(ghRepository.getDefaultBranch(), tag);
            return CompareResult.builder()
                    .name(ghRepository.getName())
                    .by(ghCompare.getBehindBy())
                    .link(ghCompare.getHtmlUrl().toString())
                    .build();
        } catch (GHFileNotFoundException notFoundException) {
            return null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
