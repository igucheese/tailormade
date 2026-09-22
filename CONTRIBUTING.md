# Contributing to Tailormade

When contributing to this repository, please first discuss the change you wish to make via an Issue before making any changes. 

Please note that we have a Code of Conduct. Please follow it in all your interactions with the project.

---

## Development Workflow

1. **Branch Structure**
    * `master`: The stable, production-ready source code.
    * `develop`: The branch for development verification and testing new feature implementations.
    * `release/vX.X.X`: The main branch targeted for the next release. Working branches should be created from here.
2. **Getting Started**
    * Before starting any work, please create an Issue (or check existing ones) on [GitHub Issues](https://github.com/igucheese/tailormade/issues) to discuss your proposal or bug report.
    * Create your working branch off of the latest `release/vX.X.X` branch.
    * Please refer to the branch naming rules below.
3. **Pull Requests**
    * Once your changes are complete, create a Pull Request targeting the `develop` branch.
    * Assign `@igucheese` as the reviewer.
    * Merging will be handled exclusively by `@igucheese` after the review is complete.

---

## Branch Naming Rules

Please follow the naming conventions below, including the corresponding Issue number:

- **Feature**: `feature/#<issue-number>-<short-description>` (e.g., `feature/#1-add-hat-pattern`)
- **Bug Fix**: `fix/#<issue-number>-<short-description>` (e.g., `fix/#45-bleach-dupe-glitch`)
- **Documentation**: `docs/#<issue-number>-<short-description>` (e.g., `docs/#8-update-readme`)
- **Other**: Please follow the feature naming convention.

---

## Commit Rules

* Write clear and concise commit messages in **English** (e.g., `Add Hat Pattern recipe`, `Fix null pointer in TailoringBenchBlock`).
* Additional description in the commit body is generally not required, as context should be tracked within the corresponding Issue or PR. Add details only when necessary.

---

## Pull Request Guidelines

* **Title**: Clear and concise title in English (preferred) or Japanese.
* **Description**: Please follow the provided Pull Request template.
* **Target Branch**: **`develop`**
* **Reviewer**: Assign `@igucheese`.

---

## Questions & Suggestions

If you have any questions, ideas, feature requests, or bug reports, feel free to open an issue on [GitHub Issues](https://github.com/igucheese/tailormade/issues)!
