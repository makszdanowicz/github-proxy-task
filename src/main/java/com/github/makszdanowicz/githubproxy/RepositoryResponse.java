package com.github.makszdanowicz.githubproxy;

import java.util.List;

public record RepositoryResponse(String name, String ownerLogin, List<BranchResponse> branches) {
}
