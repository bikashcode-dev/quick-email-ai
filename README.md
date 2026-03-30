# QuickMail Assistant

QuickMail Assistant is a Chrome extension that helps generate AI-powered email replies and draft new emails directly inside Gmail.

## Features

- Generate contextual email replies inside Gmail
- Draft brand-new emails from simple instructions
- Supports natural prompts in English, Hindi, and Hinglish
- Works with a deployed backend API

## Project Structure

- `manifest.json` - Chrome extension manifest
- `background.js` - Sends requests to the backend API
- `content.js` - Injects the AI Reply button and reads Gmail context
- `content.css` - Styles the button and notices
- `icon/` - Extension icons

## How It Works

1. Open Gmail
2. Click the `AI Reply` button in a compose or reply box
3. The extension reads the current email context or your typed instruction
4. It sends the request to the backend API
5. The generated email is inserted back into the compose box

## Backend Requirement

This extension requires a running backend API.

Current configured API:

`https://quick-email-ai-production.up.railway.app/api/email/generate`

## Installation

### Option 1: Load Unpacked in Chrome

1. Open Chrome and go to `chrome://extensions`
2. Enable `Developer mode`
3. Click `Load unpacked`
4. Select this project folder

### Option 2: Use With Deployed Backend

This extension is configured to work with the deployed backend API:

`https://quick-email-ai-production.up.railway.app/api/email/generate`

After loading the extension in Chrome:

1. Open Gmail
2. Open a reply box or compose a new email
3. Click `AI Reply`
4. Review the generated draft before sending

## Privacy and Data Use

QuickMail Assistant is designed to work only when the user actively requests help.

- The extension reads Gmail content only when the user clicks the `AI Reply` button
- It does not automatically generate replies in the background
- It does not automatically send emails
- It only sends the required email context or user instruction to the backend to generate a draft
- Generated content is inserted into the compose box as a draft for user review

The backend may use third-party AI providers to generate responses. Users should ensure that the deployed backend and connected AI services are configured responsibly and securely.

## Notes

- The extension is designed specifically for Gmail
- A stable backend is required for proper response generation
- If the backend is unavailable, generation requests will fail
- Users should always review generated content before sending
- For public release, a proper privacy policy page is recommended alongside the extension listing
