# Xeme

Xeme is a @-mention bot that will send a public message of your choice to each follower of a specified account, adding a hashtag. 

This bot runs under Google App Engine. It can be used to promote a website, send a spam message, etc.

This bot can send between 1 and 6 tweets at a time. 

This bot will run only once a day as per the `cron.xml` file, but you can change this to whatever value you would like.

---
This servlet works by gathering a message and a follower and starting a cron server to blast followers of a follower with a message.

The bot uses a stored ‘cursor’ to maintain its location along the blast path and not spam the same user twice. 

Since the API is now pay-to-play only, I haven't tested the functionality with V2 of the API, although it should work.

Be aware that this bot explicity violates [~~Twitter~~ X's automation rules](https://help.twitter.com/en/rules-and-policies/twitter-automation) 
