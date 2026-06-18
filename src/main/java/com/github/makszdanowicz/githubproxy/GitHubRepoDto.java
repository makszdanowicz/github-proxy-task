package com.github.makszdanowicz.githubproxy;

public record GitHubRepoDto(String name, GitHubOwnerDto owner, boolean fork) {
}
