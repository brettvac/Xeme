# Xeme

Xeme is a @-mention bot that will send a public message of your choice to each follower of a specified account, adding a hashtag.

This bot works by gathering a message and a follower and starting a cron server to blast followers of a follower with a message.

Xeme can send between 1 and 6 tweets at a time and can be used to promote a website, send a spam message to annoy users, etc.

This bot runs under Google App Engine, which you can deploy easily by running `mvn compile package appengine:deploy`. 

The Xeme cron servlet will run only once a day as per the `cron.xml` file, but you can change this to whatever value you like (requires `cron.yaml` to work).

## Functionality

The Xeme cron service uses a stored ‘cursor’ to maintain its location along the blast path and not spam the same user twice. 

Since the API is now pay-to-play only, I haven't tested the functionality of this bot with V2 of the API, although it should work.

---

Be aware that Xeme explicity violates [~~Twitter~~ X's automation rules](https://help.twitter.com/en/rules-and-policies/twitter-automation).
