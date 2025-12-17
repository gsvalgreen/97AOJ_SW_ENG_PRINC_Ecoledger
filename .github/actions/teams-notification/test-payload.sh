#!/bin/bash

# Test script for MS Teams notification payload validation
# This script validates that the JSON payload structure is correct

echo "Testing MS Teams Notification Payload Structure..."

# Simulate the environment variables
export GITHUB_SERVER_URL="https://github.com"
export GITHUB_REPOSITORY="gsvalgreen/97AOJ_SW_ENG_PRINC_Ecoledger"
export GITHUB_RUN_ID="123456789"
export GITHUB_SHA="abc123def456"
export GITHUB_REF="refs/heads/main"
export GITHUB_WORKFLOW="Test Workflow"
export GITHUB_EVENT_NAME="push"

# Mock git commands for testing
COMMIT_MSG="Test commit message"
COMMIT_AUTHOR="Test Author"
COMMIT_SHA="abc123d"
BRANCH_NAME="main"

# Build URLs
RUN_URL="${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/actions/runs/${GITHUB_RUN_ID}"
COMMIT_URL="${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/commit/${GITHUB_SHA}"
REPO_URL="${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}"

# Test with success status
echo ""
echo "=== Testing SUCCESS status payload ==="
JOB_STATUS="success"
SERVICE_NAME="Test Service"
STATUS_EMOJI="✅"

PAYLOAD=$(cat <<EOF
{
  "type": "message",
  "attachments": [
    {
      "contentType": "application/vnd.microsoft.card.adaptive",
      "content": {
        "type": "AdaptiveCard",
        "body": [
          {
            "type": "Container",
            "style": "emphasis",
            "items": [
              {
                "type": "TextBlock",
                "text": "${STATUS_EMOJI} Build ${JOB_STATUS}",
                "size": "large",
                "weight": "bolder",
                "color": "good"
              }
            ]
          },
          {
            "type": "FactSet",
            "facts": [
              {
                "title": "Service:",
                "value": "${SERVICE_NAME}"
              },
              {
                "title": "Branch:",
                "value": "${BRANCH_NAME}"
              },
              {
                "title": "Commit:",
                "value": "${COMMIT_SHA}"
              },
              {
                "title": "Author:",
                "value": "${COMMIT_AUTHOR}"
              },
              {
                "title": "Workflow:",
                "value": "${GITHUB_WORKFLOW}"
              },
              {
                "title": "Event:",
                "value": "${GITHUB_EVENT_NAME}"
              }
            ]
          },
          {
            "type": "TextBlock",
            "text": "${COMMIT_MSG}",
            "wrap": true,
            "separator": true
          }
        ],
        "actions": [
          {
            "type": "Action.OpenUrl",
            "title": "View Workflow Run",
            "url": "${RUN_URL}"
          },
          {
            "type": "Action.OpenUrl",
            "title": "View Commit",
            "url": "${COMMIT_URL}"
          },
          {
            "type": "Action.OpenUrl",
            "title": "View Repository",
            "url": "${REPO_URL}"
          }
        ],
        "\$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
        "version": "1.4"
      }
    }
  ]
}
EOF
)

# Validate JSON structure
if echo "$PAYLOAD" | python3 -m json.tool > /dev/null 2>&1; then
  echo "✅ JSON payload is valid"
else
  echo "❌ JSON payload is invalid"
  exit 1
fi

# Test with failure status
echo ""
echo "=== Testing FAILURE status payload ==="
JOB_STATUS="failure"
STATUS_EMOJI="❌"

PAYLOAD_FAIL=$(cat <<EOF
{
  "type": "message",
  "attachments": [
    {
      "contentType": "application/vnd.microsoft.card.adaptive",
      "content": {
        "type": "AdaptiveCard",
        "body": [
          {
            "type": "TextBlock",
            "text": "${STATUS_EMOJI} Build ${JOB_STATUS}",
            "size": "large"
          }
        ],
        "\$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
        "version": "1.4"
      }
    }
  ]
}
EOF
)

if echo "$PAYLOAD_FAIL" | python3 -m json.tool > /dev/null 2>&1; then
  echo "✅ Failure status JSON payload is valid"
else
  echo "❌ Failure status JSON payload is invalid"
  exit 1
fi

echo ""
echo "=== All payload validation tests passed! ==="
echo ""
echo "Note: To test with an actual webhook, set the TEAMS_WEBHOOK_URL environment variable"
echo "and run: curl -X POST \"\$TEAMS_WEBHOOK_URL\" -H \"Content-Type: application/json\" -d '\$PAYLOAD'"
