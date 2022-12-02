### Hexlet tests and linter status:
[![Actions Status](https://github.com/DireElf/java-project-73/workflows/hexlet-check/badge.svg)](https://github.com/DireElf/java-project-73/actions)
[![Tests](https://github.com/DireElf/java-project-73/actions/workflows/build.yml/badge.svg)](https://github.com/DireElf/java-project-73/actions/workflows/build.yml)
[![Maintainability](https://api.codeclimate.com/v1/badges/d766a928abb1889d3c9a/maintainability)](https://codeclimate.com/github/DireElf/java-project-73/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/d766a928abb1889d3c9a/test_coverage)](https://codeclimate.com/github/DireElf/java-project-73/test_coverage)

[Production](https://java-project-73-production-3677.up.railway.app/)

[API docs (Swagger)](https://java-project-73-production-3677.up.railway.app/swagger-ui.html)

The app allows you to create and manage tasks by assigning statuses, assigning executors, and setting labels. Authentication with tokens (Bearer) and password encryption are supported.

Created using Spring Boot (web, data, security, validation), Liquibase and Hibernate. CodeClimate and Rollbar are connected for code quality and error analysis. H2 database is used for development, PostgreSQL for production.

[List of control commands](https://github.com/DireElf/java-project-73/blob/main/Makefile) (can be used with or without Make utility)
