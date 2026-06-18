package com.github.makszdanowicz.githubproxy;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class RepositoryService {

    private final GitHubClient gitHubClient;

    RepositoryService(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    List<RepositoryResponse> getNonForkRepositories(String userName) {
        List<GitHubRepoDto> repositories = gitHubClient.getUserRepositories(userName);
        return repositories.stream()
                .filter(repo -> !repo.fork())
                .map(repo -> {
                    List<GitHubBranchDto> branchesDto = gitHubClient.getRepositoryBranches(userName, repo.name());
                    List<BranchResponse> branches = branchesDto.stream()
                            .map(branchDto -> new BranchResponse(branchDto.name(), branchDto.commit().sha()))
                            .toList();
                    return new RepositoryResponse(repo.name(), repo.owner().login(), branches);
                })
                .toList();
    }

}
