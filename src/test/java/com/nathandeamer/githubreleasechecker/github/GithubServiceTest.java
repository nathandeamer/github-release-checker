package com.nathandeamer.githubreleasechecker.github;

import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHCompare;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GithubServiceTest {

    private static final String DEFAULT_BRANCH = "main";
    private static final String PROD_TAG = "prod";
    private static final String GITHUB_USER = "nathandeamer";
    private static final String GITHUB_ORGANISATION = "nathandeamerOrg";
    private static final String GITHUB_HTML_URL = "https://github.com/nathandeamer/github-release-checker";

    private final GitHub github = mock(GitHub.class);

    @Test
    public void shouldCalculateGitDiffsSortedForUser() throws Exception {
        GithubService underTest = new GithubService(null, GITHUB_USER, PROD_TAG, github);

        GHUser ghUser = mock(GHUser.class);
        when(github.getUser(GITHUB_USER)).thenReturn(ghUser);

        GHRepository repo1 = mock(GHRepository.class);
        when(repo1.getName()).thenReturn("repo1");
        when(repo1.getDefaultBranch()).thenReturn(DEFAULT_BRANCH);

        GHRepository repo2 = mock(GHRepository.class);
        when(repo2.getName()).thenReturn("repo2");
        when(repo2.getDefaultBranch()).thenReturn(DEFAULT_BRANCH);

        Map<String, GHRepository> reposForUser = Map.of(
                repo1.getName(), repo1,
                repo2.getName(), repo2);

        when(ghUser.getRepositories()).thenReturn(reposForUser);

        GHCompare ghCompare1 = mock(GHCompare.class);
        GHCompare ghCompare2 = mock(GHCompare.class);

        when(repo1.getCompare(DEFAULT_BRANCH, PROD_TAG)).thenReturn(ghCompare1);
        when(repo2.getCompare(DEFAULT_BRANCH, PROD_TAG)).thenReturn(ghCompare2);

        when(ghCompare1.getBehindBy()).thenReturn(0);
        when(ghCompare2.getBehindBy()).thenReturn(1);
        when(ghCompare1.getHtmlUrl()).thenReturn(new URL(GITHUB_HTML_URL));
        when(ghCompare2.getHtmlUrl()).thenReturn(new URL(GITHUB_HTML_URL));

        List<CompareResult> result = underTest.calculateGitDiffs();

        assertThat(result).containsExactlyInAnyOrder(CompareResult.builder()
                        .name("repo2")
                        .by(1)
                        .link(GITHUB_HTML_URL)
                .build());
    }

    @Test
    public void shouldCalculateGitDiffsSortedForOrganisation() throws Exception {
        GithubService underTest = new GithubService(GITHUB_ORGANISATION, null, PROD_TAG, github);

        GHOrganization ghOrganization = mock(GHOrganization.class);
        when(github.getOrganization(GITHUB_ORGANISATION)).thenReturn(ghOrganization);

        GHRepository repo1 = mock(GHRepository.class);
        when(repo1.getName()).thenReturn("repo1");
        when(repo1.getDefaultBranch()).thenReturn(DEFAULT_BRANCH);

        GHRepository repo2 = mock(GHRepository.class);
        when(repo2.getName()).thenReturn("repo2");
        when(repo2.getDefaultBranch()).thenReturn(DEFAULT_BRANCH);

        Map<String, GHRepository> reposForOrg = Map.of(
                repo1.getName(), repo1,
                repo2.getName(), repo2);

        when(ghOrganization.getRepositories()).thenReturn(reposForOrg);

        GHCompare ghCompare1 = mock(GHCompare.class);
        GHCompare ghCompare2 = mock(GHCompare.class);

        when(repo1.getCompare(DEFAULT_BRANCH, PROD_TAG)).thenReturn(ghCompare1);
        when(repo2.getCompare(DEFAULT_BRANCH, PROD_TAG)).thenReturn(ghCompare2);

        when(ghCompare1.getBehindBy()).thenReturn(0);
        when(ghCompare2.getBehindBy()).thenReturn(1);
        when(ghCompare1.getHtmlUrl()).thenReturn(new URL(GITHUB_HTML_URL));
        when(ghCompare2.getHtmlUrl()).thenReturn(new URL(GITHUB_HTML_URL));

        List<CompareResult> result = underTest.calculateGitDiffs();

        assertThat(result).containsExactlyInAnyOrder(CompareResult.builder()
                .name("repo2")
                .by(1)
                .link(GITHUB_HTML_URL)
                .build());
    }


    @Test
    public void shouldIgnoreNotFoundWhenTagDoesntExist() throws Exception {
        GithubService underTest = new GithubService(GITHUB_ORGANISATION, null, PROD_TAG, github);

        GHOrganization ghOrganization = mock(GHOrganization.class);
        when(github.getOrganization(GITHUB_ORGANISATION)).thenReturn(ghOrganization);

        GHRepository repo1 = mock(GHRepository.class);
        when(repo1.getName()).thenReturn("repo1");
        when(repo1.getDefaultBranch()).thenReturn(DEFAULT_BRANCH);

        GHRepository repo2 = mock(GHRepository.class);
        when(repo2.getName()).thenReturn("repo2");
        when(repo2.getDefaultBranch()).thenReturn(DEFAULT_BRANCH);

        Map<String, GHRepository> reposForOrg = Map.of(
                repo1.getName(), repo1,
                repo2.getName(), repo2);

        when(ghOrganization.getRepositories()).thenReturn(reposForOrg);

        GHCompare ghCompare2 = mock(GHCompare.class);

        doThrow(new GHFileNotFoundException()).when(repo1).getCompare(DEFAULT_BRANCH, PROD_TAG);
        when(repo2.getCompare(DEFAULT_BRANCH, PROD_TAG)).thenReturn(ghCompare2);

        when(ghCompare2.getBehindBy()).thenReturn(1);
        when(ghCompare2.getHtmlUrl()).thenReturn(new URL(GITHUB_HTML_URL));

        List<CompareResult> result = underTest.calculateGitDiffs();

        assertThat(result).containsExactlyInAnyOrder(CompareResult.builder()
                .name("repo2")
                .by(1)
                .link(GITHUB_HTML_URL)
                .build());
    }
}