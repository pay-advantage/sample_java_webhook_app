# Java Demo Webhook Consumer

This project demonstrates how build an endpoint that consumes Pay Advantage webhooks.

To use this example

1. Build and publish the application. This must be accessible to external servers.
2. Create a Webhook Endpoint by posting to https://api.test.payadvantate.com.au/webhook_endpoints
3. Enter your webhook secret in src.main.java.au.com.payadvantage.javawebhooksdemo.WebhooksController.java.
4. Build and publish the application.
5. Wait for the arming process. *Note: Recreating the webhook endpoint will trigger a new arming event.*
6. Once armed, webhook events will be shown in the console.
