.PHONY: build test verify format format-check clean \
        java-build java-test java-verify java-format java-format-check java-clean \
        docs-dev docs-build docs-lint docs-format docs-format-check docs-types

# ---------------------------------------------------------------------------
# Combined
# ---------------------------------------------------------------------------

build: java-build docs-build

test: java-test

verify: java-verify docs-lint docs-format-check docs-types

format: java-format docs-format

format-check: java-format-check docs-format-check

clean: java-clean

# ---------------------------------------------------------------------------
# Java
# ---------------------------------------------------------------------------

java-build:
	mvn --batch-mode compile

java-test:
	mvn --batch-mode test

java-verify:
	mvn --batch-mode verify

java-format:
	mvn spotless:apply

java-format-check:
	mvn spotless:check

java-clean:
	mvn clean

# ---------------------------------------------------------------------------
# Docs
# ---------------------------------------------------------------------------

docs-dev:
	cd docs && pnpm dev

docs-build:
	cd docs && pnpm build

docs-lint:
	cd docs && pnpm lint

docs-format:
	cd docs && pnpm format

docs-format-check:
	cd docs && pnpm format:check

docs-types:
	cd docs && pnpm types:check
