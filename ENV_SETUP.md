# Environment Variables Setup Guide

## Overview

This application requires several environment variables to be configured for proper operation. These variables store sensitive credentials and should **NEVER** be committed to version control.

## Quick Start

1. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` and replace placeholder values with your actual credentials.

3. Never commit the `.env` file.

## Required Environment Variables

### Email Configuration (Gmail)

- **MAIL_USERNAME**: Your Gmail address (e.g., `your-email@gmail.com`)
- **MAIL_PASSWORD**: Gmail App Password (not your regular password)
  - Generate at: https://myaccount.google.com/apppasswords
  - Requires 2FA to be enabled on your Google account

### Google OAuth2 Configuration

- **GOOGLE_CLIENT_ID**: OAuth2 Client ID from Google Cloud Console
- **GOOGLE_CLIENT_SECRET**: OAuth2 Client Secret from Google Cloud Console
  - Create credentials at: https://console.cloud.google.com/apis/credentials

### OpenAI Configuration

- **OPENAI_API_KEY**: Your OpenAI API key
  - Get your API key at: https://platform.openai.com/api-keys

### Database Configuration (for Docker Compose)

- **DB_URL**: Database connection URL (e.g., `jdbc:mysql://mysql:3306/mealplanner`)
- **DB_USERNAME**: Database username
- **DB_PASSWORD**: Database password
- **DB_NAME**: Database name
- **SPRING_PROFILES_ACTIVE**: Spring profile (e.g., `dev`, `prod`)

## Local Development

For local development using the embedded H2 database, you only need:

- MAIL_USERNAME
- MAIL_PASSWORD
- GOOGLE_CLIENT_ID
- GOOGLE_CLIENT_SECRET
- OPENAI_API_KEY

Set these as environment variables in your IDE or export them in your shell:

```bash
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
export GOOGLE_CLIENT_ID=your-client-id
export GOOGLE_CLIENT_SECRET=your-client-secret
export OPENAI_API_KEY=your-api-key
```

## Docker Compose

When using Docker Compose, all variables in `.env` will be automatically loaded. Ensure all variables in `.env.example` are configured in your `.env` file.

## CI/CD

For GitHub Actions or other CI/CD platforms:

1. Add secrets in your repository settings
2. Reference them in your workflow files
3. Never hardcode credentials in workflow files

## Security Best Practices

- ✅ Use `.env` for local development (already in `.gitignore`)
- ✅ Use CI/CD secrets for deployment pipelines
- ✅ Rotate credentials regularly
- ✅ Use least-privilege access for API keys
- ✅ Monitor API usage for anomalies
- ❌ Never commit `.env` to version control
- ❌ Never hardcode credentials in code
- ❌ Never share credentials via insecure channels

## Troubleshooting

### Application fails to start with "Could not resolve placeholder"

This means an environment variable is not set. Check that:
1. Your `.env` file exists
2. All required variables are defined in `.env`
3. No typos in variable names

### Gmail authentication fails

1. Verify you're using an App Password, not your regular password
2. Ensure 2FA is enabled on your Google account
3. Check that the email address is correct

### OAuth2 fails

1. Verify your Client ID and Secret are correct
2. Ensure redirect URIs are configured in Google Cloud Console
3. Check that OAuth2 consent screen is configured

## Support

For issues related to:
- Gmail App Passwords: https://support.google.com/accounts/answer/185833
- Google OAuth2: https://developers.google.com/identity/protocols/oauth2
- OpenAI API: https://platform.openai.com/docs
