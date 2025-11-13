# Security Notice

## ⚠️ CRITICAL: Exposed Secrets Detected

**Date Identified:** November 13, 2025

### Exposed Credentials

The following sensitive credentials were found committed to the repository in `src/main/resources/application.properties`:

1. **Gmail App Password** - Exposed email credentials
2. **Google OAuth2 Client Secret** - OAuth2 authentication credentials
3. **OpenAI API Key** - Full API key with project identifier

### Immediate Actions Required

**If you are the repository owner, take these steps IMMEDIATELY:**

1. **Revoke all exposed credentials:**
   - [ ] Revoke Gmail app password at: https://myaccount.google.com/apppasswords
   - [ ] Regenerate Google OAuth2 client secret in Google Cloud Console
   - [ ] Revoke and regenerate OpenAI API key at: https://platform.openai.com/api-keys

2. **Generate new credentials:**
   - Create new Gmail app password
   - Create new Google OAuth2 client secret
   - Create new OpenAI API key

3. **Update environment variables:**
   - Copy `.env.example` to `.env`
   - Add your new credentials to `.env` file
   - Never commit `.env` file

4. **Review and clean git history:**
   - Consider using `git-filter-repo` or BFG Repo-Cleaner to remove sensitive data from git history
   - See: https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository

5. **Monitor for unauthorized access:**
   - Check OpenAI API usage logs for unauthorized requests
   - Check Google Cloud Console for OAuth2 usage
   - Review Gmail account activity

### Changes Made

This security fix includes:

1. ✅ Removed all hardcoded secrets from `application.properties`
2. ✅ Updated configuration to use environment variables
3. ✅ Created `application.properties.example` as a template
4. ✅ Created `.env.example` with documentation
5. ✅ Updated `compose.yaml` to include all required environment variables
6. ✅ Verified `.gitignore` excludes `.env` files

### Setup Instructions

For developers setting up this project:

1. Copy the environment template:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` and add your credentials:
   ```bash
   # Update with your actual values
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-app-password
   GOOGLE_CLIENT_ID=your-client-id
   GOOGLE_CLIENT_SECRET=your-client-secret
   OPENAI_API_KEY=your-api-key
   ```

3. Never commit the `.env` file to version control

### Security Best Practices

- ✅ Use environment variables for all sensitive configuration
- ✅ Use `.env.example` for documentation, never `.env`
- ✅ Add `.env` to `.gitignore`
- ✅ Use GitHub Secrets for CI/CD pipelines
- ✅ Regularly rotate API keys and credentials
- ✅ Use least-privilege access principles
- ✅ Monitor API usage for anomalies

### Reporting Security Issues

If you discover a security vulnerability, please email the repository owner directly. Do not open a public issue.

### References

- [GitHub: Removing sensitive data](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository)
- [OWASP: Credential Management](https://cheatsheetseries.owasp.org/cheatsheets/Credential_Stuffing_Prevention_Cheat_Sheet.html)
- [Spring Boot: Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
