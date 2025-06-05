Jetzt funktioniert es
Funktioniert es jetzt
ich versuche noch einmal einen pull request

This is a Kotlin Multiplatform project targeting Web, Desktop, Server.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/server` is for the Ktor server application.

* `/shared` is for the code that will be shared between all targets in the project.
  The most important subfolder is `commonMain`. If preferred, you can add code to the platform-specific folders here too.

## Email Configuration

The application uses email to send notifications for form submissions. The email configuration can be set up in several ways:

1. **Environment Variables**: The application reads email configuration from environment variables.
2. **.env File**: If environment variables are not set, the application looks for a `.env` file.
3. **Default Values**: If neither environment variables nor a `.env` file is found, default values are used.

### GitHub Actions Secrets

For deployment with GitHub Actions, the email configuration is stored in GitHub repository secrets. The following secrets need to be set up in your GitHub repository:

- `SMTP_HOST`: The SMTP server host (e.g., smtp.gmail.com)
- `SMTP_PORT`: The SMTP server port (e.g., 587)
- `SMTP_USER`: The SMTP username (usually your email address)
- `SMTP_PASSWORD`: The SMTP password or app password
- `RECIPIENT_EMAIL`: The email address that will receive form submissions
- `SMTP_SENDER_EMAIL`: The email address that will appear as the sender (usually the same as SMTP_USER)

These secrets are automatically passed to the Docker container during deployment via the GitHub Actions workflow.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [GitHub](https://github.com/JetBrains/compose-multiplatform/issues).

You can open the web application by running the `:composeApp:wasmJsBrowserDevelopmentRun` Gradle task.
