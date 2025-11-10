# Google OAuth2 Setup Guide

## Step-by-Step Instructions

### 1. Go to Google Cloud Console
Visit: https://console.cloud.google.com/

### 2. Create a New Project (or use existing)
- Click on the project dropdown at the top
- Click "New Project"
- Name it: "Weekly Meal Planner"
- Click "Create"

### 3. Enable Google+ API
- In the left sidebar, go to "APIs & Services" > "Library"
- Search for "Google+ API"
- Click on it and click "Enable"

### 4. Configure OAuth Consent Screen
- Go to "APIs & Services" > "OAuth consent screen"
- Choose "External" (unless you have a Google Workspace)
- Click "Create"
- Fill in:
  - App name: Weekly Meal Planner
  - User support email: your-email@gmail.com
  - Developer contact: your-email@gmail.com
- Click "Save and Continue"
- On Scopes page, click "Save and Continue"
- On Test users page, add your email, click "Save and Continue"
- Click "Back to Dashboard"

### 5. Create OAuth2 Credentials
- Go to "APIs & Services" > "Credentials"
- Click "+ CREATE CREDENTIALS" > "OAuth client ID"
- Choose "Web application"
- Name: "Weekly Meal Planner Web Client"
- Add Authorized redirect URIs:
  - http://localhost:8080/login/oauth2/code/google
  - http://localhost:8080/login/oauth2/code/google/
- Click "Create"

### 6. Copy Your Credentials
- You'll see a popup with:
  - Client ID (looks like: 123456789-abc123.apps.googleusercontent.com)
  - Client Secret (looks like: GOCSPX-abc123def456)
- **Keep this window open or download the JSON**

### 7. Add to .env File
Add these lines to your `.env` file:

```bash
GOOGLE_CLIENT_ID=your-client-id-from-step-6
GOOGLE_CLIENT_SECRET=your-client-secret-from-step-6
```

### 8. Restart Your Application
```bash
./mvnw spring-boot:run
```

### 9. Test the Login
- Open: http://localhost:8080
- Click "Login with Google"
- You should be redirected to Google login
- After login, you'll be redirected back to your app

## Troubleshooting

### Error: "redirect_uri_mismatch"
- Make sure you added exactly: `http://localhost:8080/login/oauth2/code/google`
- Check for trailing slashes
- Wait a few minutes for Google to propagate changes

### Error: "Access blocked: This app's request is invalid"
- Make sure you completed the OAuth consent screen configuration
- Add yourself as a test user

### App shows "Error 401: invalid_client"
- Double-check your CLIENT_ID and CLIENT_SECRET in .env
- Make sure there are no extra spaces
- Restart the application

## Quick Reference

Your .env file should look like:
```bash
GOOGLE_CLIENT_ID=123456789-abc123def456.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-abc123def456ghi789
OPENAI-KEY=sk-proj-your-key-here
OPENAI-API=https://api.openai.com/v1/chat/completions
OPENAI-MODEL=gpt-4o
```

## For Production
When deploying to production:
1. Update authorized redirect URIs with your production URL
2. Publish your OAuth consent screen (move from Testing to Production)
3. Use environment variables instead of .env file
