# GitHub Proxy Application

This is a recruitment task application that acts as a proxy for the GitHub API. It retrieves non-fork repositories for a specific user along with their branches and latest commit SHAs.

## Requirements
* **Java 25**
* **Spring Boot 4.1.0**

## Notes
* **Architecture:** kept strictly simple (`Controller` -> `Service` -> `Client`), with a flat, single-package structure.
* **Access Modifiers:** since all classes reside in a single package, `package-private` access was leveraged extensively to ensure proper encapsulation (no unnecessary `public` modifiers).
* using `RestClient` for synchronous HTTP calls

## Backing API Reference
This application utilizes the official [GitHub REST API v3](https://developer.github.com/v3) as its backing data source. The implementation is based on the following specific API endpoints and versions:
* **List repositories for a user:** [GitHub Repos API Documentation (v2026-03-10)](https://docs.github.com/en/rest/repos/repos?apiVersion=2026-03-10#list-repositories-for-a-user) – used to fetch the user's repositories and filter out forks.
* **List branches:** [GitHub Branches API Documentation (v2026-03-10)](https://docs.github.com/en/rest/branches/branches?apiVersion=2026-03-10) – used to resolve branch names and the latest commit SHAs for each non-fork repository.

## Endpoints

### Get User Repositories
`GET /api/repositories/{username}`

**Success Response (200 OK):**
```json
[
  {
    "repositoryName": "example-repo",
    "ownerLogin": "username",
    "branches": [
      {
        "name": "main",
        "lastCommitSha": "abc123..."
      }
    ]
  }
]
```

### Error Response (404 Not Found)
```json
{
  "status": 404,
  "message": "User nonexistinguser not found on GitHub"
}
```

## How to run locally

### 1. Clone the repository
First, clone the project to your local machine and navigate into the directory:
```
git clone https://github.com/makszdanowicz/github-proxy-task.git
cd github-proxy-task
```


### 2. Run the application
To start the application locally, run the following Gradle wrapper command:
```
./gradlew bootRun
```
The application will start and listen on `http://localhost:8080`.

### Run the tests
To execute the integration tests (which use WireMock to emulate the GitHub API), run:
```
./gradlew test
```

## Useful Links & References
The following external resources and documentation were discovered and utilized during the research and implementation of this task:

- [WireMock JUnit 5 Extension Guide](https://wiremock.org/docs/junit-jupiter/) – official documentation for setting up the WireMock extension in JUnit 5 environments.
- [Spring Framework Testing Reference: RestTestClient](https://docs.spring.io/spring-framework/reference/testing/resttestclient.html) – official documentation on leveraging unified fluent testing APIs for REST endpoints.
- [StackOverflow: WireMock Issue When Upgrading to Spring Boot 3](https://stackoverflow.com/questions/74673966/wiremock-issue-when-upgrading-to-spring-boot-3) – reference solution used to resolve dependency and configuration conflicts between WireMock and modern Spring Boot starters, applicable for Spring Boot 4.1.0 as well.