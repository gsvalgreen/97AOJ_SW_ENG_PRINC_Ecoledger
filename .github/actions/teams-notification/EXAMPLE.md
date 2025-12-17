# MS Teams Notification - Example Output

This document shows what the notification will look like in MS Teams.

## Example Notification (Success)

```
╔═══════════════════════════════════════════════════════════╗
║                                                           ║
║  ✅ Build success                                         ║
║                                                           ║
╠═══════════════════════════════════════════════════════════╣
║                                                           ║
║  Service:        Users Service                            ║
║  Branch:         feature/add-new-endpoint                 ║
║  Commit:         a1b2c3d                                  ║
║  Author:         John Doe                                 ║
║  Workflow:       Users Service CI                         ║
║  Event:          push                                     ║
║                                                           ║
╠═══════════════════════════════════════════════════════════╣
║                                                           ║
║  Add new user registration endpoint with validation       ║
║                                                           ║
╠═══════════════════════════════════════════════════════════╣
║                                                           ║
║  [ View Workflow Run ]  [ View Commit ]  [ View Repository ] ║
║                                                           ║
╚═══════════════════════════════════════════════════════════╝
```

## Example Notification (Failure)

```
╔═══════════════════════════════════════════════════════════╗
║                                                           ║
║  ❌ Build failure                                         ║
║                                                           ║
╠═══════════════════════════════════════════════════════════╣
║                                                           ║
║  Service:        Movimentacao Service                     ║
║  Branch:         main                                     ║
║  Commit:         x9y8z7w                                  ║
║  Author:         Jane Smith                               ║
║  Workflow:       Movimentacao Service CI                  ║
║  Event:          pull_request                             ║
║                                                           ║
╠═══════════════════════════════════════════════════════════╣
║                                                           ║
║  Fix null pointer exception in transaction processor      ║
║                                                           ║
╠═══════════════════════════════════════════════════════════╣
║                                                           ║
║  [ View Workflow Run ]  [ View Commit ]  [ View Repository ] ║
║                                                           ║
╚═══════════════════════════════════════════════════════════╝
```

## Color Coding

- **Green (Success)**: ✅ Builds that pass all tests
- **Red (Failure)**: ❌ Builds that fail tests or have errors
- **Yellow (Cancelled)**: ⚠️ Builds that were manually cancelled

## Interactive Elements

All buttons in the notification are clickable and will:
- **View Workflow Run**: Open the GitHub Actions run details page
- **View Commit**: Open the specific commit on GitHub
- **View Repository**: Open the repository homepage

## Technical Implementation

The notifications use Microsoft Teams **Adaptive Cards** format (version 1.4), which provides:
- Rich formatting and layout
- Color-coded status indicators
- Interactive action buttons
- Responsive design (works on desktop and mobile Teams apps)
- Accessibility support

## Notification Triggers

Notifications are sent:
- ✅ After every successful build
- ❌ After every failed build  
- ⚠️ After every cancelled build
- 📅 On all branches: main, develop, feature/*
- 🔔 For both push events and pull requests

## Privacy & Security

- Webhook URL is stored securely in GitHub Secrets
- No sensitive data (credentials, API keys, etc.) is included in notifications
- Only public repository information is shared
- Notifications can only be sent to the configured Teams channel
