# MS Teams Integration - CI/CD Notifications

This document describes how to set up MS Teams notifications for CI/CD pipeline builds in the ECO LEDGER project.

## 📋 Overview

All CI/CD workflows in this repository are configured to send build status notifications to a Microsoft Teams channel. The notifications include:

- ✅ Build status (success, failure, or cancelled)
- 📦 Service name being built
- 🌿 Git branch name
- 📝 Commit SHA and message
- 👤 Commit author
- 🔗 Direct links to workflow run, commit, and repository

## 🔧 Setup Instructions

### Step 1: Create an Incoming Webhook in MS Teams

1. Open Microsoft Teams and navigate to the channel where you want to receive build notifications
2. Click on the three dots (`...`) next to the channel name
3. Select **Connectors** or **Workflows** (depending on your Teams version)
4. Search for **Incoming Webhook**
5. Click **Add** or **Configure**
6. Provide a name (e.g., "ECO LEDGER CI/CD Builds")
7. Optionally upload an icon/image
8. Click **Create**
9. **Copy the webhook URL** - you'll need this for the next step

### Step 2: Add the Webhook URL to GitHub Secrets

1. Go to your GitHub repository
2. Navigate to **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Name: `TEAMS_WEBHOOK_URL`
5. Value: Paste the webhook URL you copied from Teams
6. Click **Add secret**

### Step 3: Verify the Integration

Once the secret is added, the next CI/CD pipeline run will automatically send notifications to your Teams channel.

To trigger a test notification:
1. Make a small change to any service (e.g., update a comment)
2. Push to a branch that triggers CI (main, develop, or feature/*)
3. Check your Teams channel for the notification

## 📊 Notification Format

The notifications use Adaptive Cards format and include:

**Header:**
- Status emoji (✅ success, ❌ failure, ⚠️ cancelled)
- Build status text

**Details:**
- Service: Name of the microservice being built
- Branch: Git branch name
- Commit: Short SHA
- Author: Commit author name
- Workflow: GitHub Actions workflow name
- Event: Trigger event (push, pull_request, etc.)

**Commit Message:**
- The full commit message

**Action Buttons:**
- View Workflow Run (direct link to GitHub Actions run)
- View Commit (direct link to the commit)
- View Repository (direct link to the repository)

## 🔍 Affected Workflows

The following CI workflows are configured with Teams notifications:

1. **users-service-ci.yml** - Users Service builds
2. **movimentacao-service-ci.yml** - Movimentacao Service builds
3. **auditoria-service-ci.yml** - Auditoria Service builds
4. **certificacao-service-ci.yml** - Certificacao Service builds
5. **frontend-ci.yml** - Frontend Web builds

## ⚙️ Technical Details

### Composite Action

The Teams notification is implemented as a reusable composite action located at:
```
.github/actions/teams-notification/action.yml
```

This action is called from each workflow with the following inputs:
- `webhook-url`: The MS Teams webhook URL (from secrets)
- `job-status`: The status of the job (success, failure, cancelled)
- `service-name`: The name of the service being built

### Conditional Execution

The notification step only runs if:
1. The step is configured with `if: always()` - so it runs regardless of previous step failures
2. The `TEAMS_WEBHOOK_URL` secret is set - so it doesn't fail if the secret is not configured

This ensures:
- ✅ Notifications are sent even if the build fails
- ✅ The workflow doesn't fail if the webhook is not configured
- ✅ Graceful handling when the secret is missing

### Error Handling

The notification action includes error handling:
- If the HTTP request to Teams fails, it logs a warning but doesn't fail the workflow
- The exit status is always 0 to prevent build failures due to notification issues
- Non-blocking design ensures CI/CD pipeline integrity

## 🎨 Customization

### Changing the Notification Format

To customize the notification format, edit the file:
```
.github/actions/teams-notification/action.yml
```

The payload is formatted as an Adaptive Card. You can:
- Add or remove facts
- Change colors
- Modify the card structure
- Add more action buttons

Reference: [Adaptive Cards Documentation](https://adaptivecards.io/)

### Adding Notifications to New Workflows

To add Teams notifications to a new workflow:

1. Add this step at the end of your job:
```yaml
- name: Notify MS Teams
  if: always() && secrets.TEAMS_WEBHOOK_URL != ''
  uses: ./.github/actions/teams-notification
  with:
    webhook-url: ${{ secrets.TEAMS_WEBHOOK_URL }}
    job-status: ${{ job.status }}
    service-name: 'Your Service Name'
```

2. Replace `'Your Service Name'` with the appropriate service name

## 🔐 Security Considerations

- ⚠️ **Never commit the webhook URL to the repository** - always use GitHub Secrets
- 🔒 The webhook URL should be treated as a secret
- 👥 Limit access to the Teams channel to authorized team members only
- 🔄 If the webhook URL is compromised, regenerate it in Teams and update the GitHub Secret

## 🐛 Troubleshooting

### Notifications not appearing

1. **Check if the secret is set:**
   - Go to GitHub Settings → Secrets and variables → Actions
   - Verify that `TEAMS_WEBHOOK_URL` exists

2. **Verify the webhook URL is valid:**
   - The URL should start with `https://`
   - Test the webhook manually with curl:
   ```bash
   curl -X POST "YOUR_WEBHOOK_URL" \
     -H "Content-Type: application/json" \
     -d '{"text": "Test message"}'
   ```

3. **Check workflow logs:**
   - Open the GitHub Actions run
   - Look for the "Notify MS Teams" step
   - Check for error messages

4. **Verify the webhook is active in Teams:**
   - Go to the Teams channel
   - Check Connectors/Workflows
   - Ensure the webhook is still configured

### Notification sent but not formatted correctly

- Ensure you're using a modern version of MS Teams
- Adaptive Cards require Teams to support Adaptive Card version 1.4
- Check the Teams channel settings to ensure cards are not blocked

## 📚 References

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [MS Teams Incoming Webhooks](https://learn.microsoft.com/en-us/microsoftteams/platform/webhooks-and-connectors/how-to/add-incoming-webhook)
- [Adaptive Cards](https://adaptivecards.io/)
- [Composite Actions](https://docs.github.com/en/actions/creating-actions/creating-a-composite-action)

---

**Last Updated:** 2025-12-17  
**Project:** ECO LEDGER  
**Maintained by:** DevOps Team
