package com.github.makszdanowicz.githubproxy;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RepositoryIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().port(8081))
            .build();

    RestTestClient restTestClient;

    @DynamicPropertySource
    static void configureProperty(DynamicPropertyRegistry registry) {
        registry.add("external.api.url", wireMock::baseUrl);
    }

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void shouldReturnNonForkRepositories() {
        // GIVEN
        String userName = "test-user";
        String validRepoName = "test-app";

        GitHubRepoDto validRepo = new GitHubRepoDto(validRepoName, new GitHubOwnerDto(userName), false);
        GitHubRepoDto forkedRepo = new GitHubRepoDto("forked-app", new GitHubOwnerDto(userName), true);
        List<GitHubRepoDto> githubReposResponse = List.of(validRepo, forkedRepo);
        String reposJson = objectMapper.writeValueAsString(githubReposResponse);
        wireMock.stubFor(get("/users/" + userName + "/repos")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(reposJson)));

        String branchName = "main";
        String lastCommitSha = "abc123sha";
        GitHubBranchDto mainBranch = new GitHubBranchDto(branchName, new GitHubCommitDto(lastCommitSha));
        String branchJson = objectMapper.writeValueAsString(List.of(mainBranch));
        wireMock.stubFor(get("/repos/" + userName + "/" + validRepoName + "/branches")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(branchJson)));

        // WHEN + THEN
        List<RepositoryResponse> repositories = restTestClient.get().uri("/api/repositories/" + userName)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(new ParameterizedTypeReference<List<RepositoryResponse>>() {})
                .returnResult()
                .getResponseBody();
        assertThat(repositories).isNotNull();
        assertThat(repositories).hasSize(1);

        RepositoryResponse response = repositories.getFirst();
        assertThat(response.name()).isEqualTo(validRepoName);
        assertThat(response.ownerLogin()).isEqualTo(userName);
        assertThat(response.branches()).isNotNull();
        assertThat(response.branches()).hasSize(1);
        assertThat(response.branches().getFirst().name()).isEqualTo(branchName);
        assertThat(response.branches().getFirst().lastCommitSha()).isEqualTo(lastCommitSha);
    }

    @Test
    void shouldReturn404WhenUserDoesNotExistOnGitHub() {
        // GIVEN
        String userName = "not-existing-user";

        wireMock.stubFor(get("/users/" + userName + "/repos")
                .willReturn(aResponse().withStatus(404)));

        // WHEN + THEN
        ErrorResponse response = restTestClient.get().uri("/api/repositories/" + userName)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.message()).isEqualTo("User " + userName + " not found");
    }
}
