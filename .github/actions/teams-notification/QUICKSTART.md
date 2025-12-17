# MS Teams Integration - Quick Reference

## 🚀 Quick Setup (2 Steps)

### Step 1: Get Webhook URL from Teams
```
Teams Channel → ⋯ → Connectors → Incoming Webhook → Create
```
Copy the generated webhook URL.

### Step 2: Add to GitHub Secrets
```
GitHub Repo → Settings → Secrets and variables → Actions → New secret
Name: TEAMS_WEBHOOK_URL
Value: [your webhook URL]
```

## 📊 What Gets Notified

| Trigger | Notification Sent |
|---------|------------------|
| Build Success ✅ | Yes |
| Build Failure ❌ | Yes |
| Build Cancelled ⚠️ | Yes |
| Push to main/develop | Yes |
| Push to feature/* | Yes |
| Pull Request | Yes |

## 🎯 Affected Services

All 5 services have Teams notifications:
- ✅ Users Service
- ✅ Movimentacao Service
- ✅ Auditoria Service
- ✅ Certificacao Service
- ✅ Frontend Web

## 📱 Notification Content

Each notification includes:
- Build status (success/failure/cancelled)
- Service name
- Git branch
- Commit SHA & message
- Author name
- Clickable links to workflow run, commit, and repo

## 🔧 Troubleshooting

| Issue | Solution |
|-------|----------|
| No notifications appearing | Check if `TEAMS_WEBHOOK_URL` secret is set |
| Webhook expired | Regenerate in Teams and update secret |
| Wrong channel | Verify webhook was created in correct channel |

## 📖 Documentation

- **Full Setup Guide**: [README-teams-integration.md](../../README-teams-integration.md)
- **Examples**: [EXAMPLE.md](./EXAMPLE.md)
- **Test Script**: [test-payload.sh](./test-payload.sh)

## 🔒 Security Notes

- ⚠️ Never commit webhook URLs to repository
- 🔐 Always use GitHub Secrets
- 👥 Limit Teams channel access to authorized team members

## ⚡ Testing

To test the integration:
```bash
# Make any change and push
git commit -am "Test Teams notification" 
git push

# Check Teams channel for notification within 1-2 minutes
```

---

**Need help?** Check the full documentation at [README-teams-integration.md](../../README-teams-integration.md)
