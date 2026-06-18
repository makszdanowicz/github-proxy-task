package com.github.makszdanowicz.githubproxy;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
class GitHubClient {

    private final RestClient restClient;

    GitHubClient(RestClient restClient) {
        this.restClient = restClient;
    }

    List<GitHubRepoDto> getUserRepositories(String userName) {
        return restClient.get()
                .uri("/users/{username}/repos", userName)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> {
                    if (response.getStatusCode().value() == 404) {
                        throw new UserNotFoundException("User " + userName + " not found");
                    }
                    throw new RuntimeException("API Error: " + response.getStatusCode());
                }))
                .body(new ParameterizedTypeReference<List<GitHubRepoDto>>() {});
    }

    List<GitHubBranchDto> getRepositoryBranches(String owner, String repo) {
        return restClient.get()
                .uri("/repos/{owner}/{repo}/branches", owner, repo)
                .retrieve()
                .body(new ParameterizedTypeReference<List<GitHubBranchDto>>() {});
    }

}
