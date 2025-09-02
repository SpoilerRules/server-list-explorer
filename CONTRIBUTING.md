# Contributing

We're excited you're interested in contributing to this project! To ensure a smooth and effective collaboration, please
follow these guidelines. Your adherence helps us maintain a high-quality, stable codebase for everyone.

## Pull Requests

Before starting any work, please create an issue to discuss the feature or bug you want to address. This allows
maintainers and the community to provide feedback, prevent wasted effort, and ensure your contribution aligns with the
project's goals.

All pull requests must be associated with an existing issue. This helps us track and manage changes effectively.

### General Guidelines

* **Create an Issue First**: Always create an issue and get preliminary approval from a maintainer before you start
  writing code.
* **Branching**: Work on your changes in a dedicated branch. Name your branch descriptively using the issue number and a
  short name, for example: `fix/123-crash-on-startup` or `feat/456-add-user-profile`.
* **Descriptive Pull Request Title**: Your PR title should be concise and descriptive. It should start with a prefix
  like `feat:`, `fix:`, `docs:`, or `refactor:`.
* **Link to Issue**: In the description of your pull request, link to the issue it resolves. You can use keywords like
  `Closes #123` or `Fixes #123`.
* **Detailed Description**: Provide a clear and detailed description of the changes in your pull request. Explain the
  problem you're solving, the approach you took, and any potential side effects. Include screenshots or GIFs for UI
  changes.
* **One Concern per Pull Request**: Keep your pull requests focused. Address a single feature, bug, or refactoring in
  each PR. This makes reviews easier and faster.
* **Keep Pull Requests Updated**: If your pull request is open for a while, it may fall behind the `main` branch.
  Regularly rebase your branch on the latest `main` branch to resolve merge conflicts and ensure your changes are based
  on the most recent code.
* **Request a Review**: Once you believe your pull request is ready, request a review from a maintainer. Be responsive
  to feedback and address comments promptly.

## Guidelines for Pull Requests

To ensure your contributions are high-quality and easy to merge, please follow these guidelines when preparing your pull
requests.

1. **Follow Code Style**: Format your Kotlin code according to the **JetBrains coding conventions**. After making your
   changes, run the `ktlintFormat` Gradle command (e.g., `./gradlew ktlintFormat`) to automatically apply the correct
   formatting.
2. **Verify Optimizations**: Test your code to ensure it works correctly when minified by ProGuard. Run the
   `./gradlew packageReleaseUberJarForCurrentOS` command and verify that your changes are functioning as expected in the
   resulting optimized JAR file. This helps catch potential issues with obfuscation (not enabled for now) and shrinking.
3. **Idiomatic Kotlin**: Use **expression returns** whenever possible to write concise and readable functions. For
   example, prefer `fun sum(a: Int, b: Int) = a + b` over a function with a body and a separate `return` statement.
4. **Testing for Unstable APIs**: If your pull request involves interactions with external, volatile APIs (e.g., HTTP
   requests, database operations, or third-party libraries), you **must** include **unit tests**. These tests are
   crucial for guarding against future regressions when things outside our control change. For internal logic, tests are
   highly encouraged but not strictly mandatory.
5. **Documentation**: If your pull request introduces a new feature or changes existing behavior, update the relevant
   documentation. This includes **KDoc** for public APIs and any related Markdown files.

## Coding style

### Higher order functions

1. Avoid chain-call porn (overusing chained high-order functions)
    - Good:
   ```kotlin
   val eligibleUsers = users
    .filter { it.isActive }
    .map { it.toResponseDto() }
    .takeIf { it.isNotEmpty() } ?: emptyList()

   val eligibleUserIds = eligibleUsers.map { it.id }

   val availableItems = items.filter { it.isInStock && it.ownerId in eligibleUserIds }

   val discountedItems = availableItems.map { applyDiscount(it) }

   return discountedItems.sortedBy { it.price }
   ```
    - Bad:
    ```kotlin
    return items
        .filter { it.isInStock && users.any { user -> user.isActive && user.id == it.ownerId } }
        .map { applyDiscount(it) }
        .sortedBy { it.price }
        .takeIf { it.isNotEmpty() }
        ?.map { it.toDto() }
        ?: emptyList()
   ```

### Formatting

1. Break a single line that contains a text longer than to multiple lines

    - When a single line of text exceeds a reasonable length (100 characters), it's better to split it into multiple
      lines using string concatenation or raw strings.
    - Good:
   ```kotlin
   val message = "Dear customer, your order has been received and is currently being processed. " +
    "You will receive an update once it has been shipped. Thank you for shopping with us!"

   val description = """
    |This is a detailed explanation of the process.
    |Please make sure to follow each step carefully,
    |as mistakes could lead to data loss.
   """.trimMargin()
   ```
    - Bad:
   ```kotlin
   val message = "Dear customer, your order has been received and is currently being processed. You will receive an update once it has been shipped. Thank you for shopping with us!"

   val description = "This is a detailed explanation of the process. Please make sure to follow each step carefully, as mistakes could lead to data loss."
   ```
2. Do not follow IntelliJ's or Android Studio's suggestion to wrap unary operators in parentheses
    - Good:
   ```kotlin
   val offset = -1.dp
   val negativeIndex = -list.size
   ```
    - Bad:
   ```kotlin
   val offset = (-1).dp
   val negativeIndex = (-list.size)
   ```
3. If `t` or `stringResource` can't be used inline because the current scope isn’t composable, create a variable instead
    - Contain the full, descriptive name of the string resource (no vague abbreviations).
    - Be converted to snake_case in the resource name but camelCase in the variable.
    - End with the word 'Text' so nobody mistakes it for anything else.
    - Good:
   ```kotlin
   val pleaseDoNotPushTheBigRedButtonText = t(Res.string.please_do_not_push_the_big_red_button)

   LaunchedEffect(nuclearReactor) {
       Text(pleaseDoNotPushTheBigRedButtonText)
   }
   ```
    - Bad:
   ```kotlin
   // What even is this? Mystery meat variable names
   val txt = t(Res.string.txt)
   LaunchedEffect(nuclearReactor) {
       // Will this save the world or order a pizza? Nobody knows
       Text(txt)
   }
   ```

### Proguard Files

1. If a ProGuard rules file is sourced from an online resource, clearly indicate its origin by placing a comment at the
   very top of the file in the following format:
   ```
   # --- Source: {resource link}
   ```
   For example, in proguard\kotlinx-serialization.pro:
   ```
   # --- Source: https://github.com/Kotlin/kotlinx.serialization/blob/master/rules/common.pro
   ```

2. If you modify any rules from the original source, add another comment immediately after the source link describing
   the nature of the modification

   Example:
   ```
   # --- Source: https://github.com/Kotlin/kotlinx.serialization/blob/master/rules/common.pro
   # Modified: Excluded rules for legacy API, see commit <hash>
   ```

3. Always keep one library or concern per ProGuard file. Name each file descriptively and store it under the proguard\
   directory at the project root



