/*
 * Copyright (c) 2017-2026 Brett
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 */

package io.github.brettvac.xeme;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.IDs;
import twitter4j.Trends;

import twitter4j.TwitterV2;
import twitter4j.TwitterV2ExKt;
import twitter4j.CreateTweetResponse;

public class XemeCronServlet extends HttpServlet 
   {

   private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
   private static Entity cursorEntity, oauthEntity, spamEntity;
   public static long cursor;
   
   public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException 
      {
      Twitter bacon;
      TwitterV2 xeme;
      IDs ids;
      StringBuilder tweet = new StringBuilder();
      Random r = new Random();
      
      PrintWriter out = response.getWriter();
      response.setContentType("text/plain; charset=UTF-8");
      
      try 
         {    
         // Read OAuth values from App Engine environment variables
         String oAuthConsumerKey = System.getenv("TWITTER4J_OAUTH_CONSUMER_KEY");
         String oAuthConsumerSecret = System.getenv("TWITTER4J_OAUTH_CONSUMER_SECRET");
         String oAuthAccessToken = System.getenv("TWITTER4J_OAUTH_ACCESS_TOKEN");
         String oAuthAccessTokenSecret = System.getenv("TWITTER4J_OAUTH_ACCESS_TOKEN_SECRET");
         
         //Get Spam values from Datastore
         spamEntity = datastore.get(KeyFactory.createKey("spamEntity", "SP"));    
         String host = spamEntity.getProperty("host").toString();
               
         String message = spamEntity.getProperty("message").toString();

         int frequency = Integer.parseInt(spamEntity.getProperty("frequency").toString());

        //Set up the Twitter object
         ConfigurationBuilder twitterConfigBuilder = new ConfigurationBuilder();     
         twitterConfigBuilder.setDebugEnabled(false);
         
         twitterConfigBuilder.setOAuthConsumerKey(oAuthConsumerKey);
         twitterConfigBuilder.setOAuthConsumerSecret(oAuthConsumerSecret);
         twitterConfigBuilder.setOAuthAccessToken(oAuthAccessToken);
         twitterConfigBuilder.setOAuthAccessTokenSecret(oAuthAccessTokenSecret);
         
         // Twitter object for API version 1.1
         bacon = new TwitterFactory(twitterConfigBuilder.build()).getInstance();
         // Twitter object for API version 2
         xeme = TwitterV2ExKt.getV2(bacon);      
         
         //Retrieve stored cursor to track who we have messaged
         try
            {
            cursorEntity = datastore.get(KeyFactory.createKey("cursorEntity", "CE"));
            cursor = Long.parseLong(cursorEntity.getProperty("cursor").toString());
            }
         catch (EntityNotFoundException e) 
            {
            // Make new cursorEntity if we haven't spammed anyone yet
            out.println("Creating new cursorEntity for Datastore...");
            cursorEntity = new Entity("cursorEntity", "CE");
            cursorEntity.setProperty("cursor", "-1");
            datastore.put(cursorEntity);
            cursor = -1L; //Set the cursor at -1 for this session
            }
         
         if(cursor == 0)
            {
            out.println("All followers have been notified.");
            return;
            }
         
         //Get the followers that we want to spam
         ids = bacon.getFollowersIDs(host, cursor, frequency);
       
         //Post the message along with a trend
         for (long id : ids.getIDs()) 
            {
            tweet.append("@");
            tweet.append(bacon.showUser(id).getScreenName());
            tweet.append(" ").append(message);
            
            //Get a trend using a WOEID from the list (change this list to match language of your tweet)
            int[] woeids = new int[] { 3444, 580778, 615702, 610264, 23424819 };
            Trends t = bacon.getPlaceTrends(woeids[r.nextInt(woeids.length)]);
            tweet.append(" ").append(t.getTrends()[r.nextInt(t.getTrends().length)].getName());
            
            /* Tweets are maximum 280 characters, so trim our sentence appropriately */
            if(tweet.length() > 280) 
               tweet.setLength(280);
            
            // Posting using version 1.1 is deprecated
            /* bacon.updateStatus(tweet.toString()); */
            
            // Version 2 of the API is pay to play only
            CreateTweetResponse tweetResponse = xeme.createTweet(
                    null,   // directMessageDeepLink
                    null,   // forSuperFollowersOnly
                    null,   // placeId
                    null,   // mediaIds
                    null,   // taggedUserIds
                    null,   // pollDurationMinutes
                    null,   // pollOptions
                    null,   // quoteTweetId
                    null,   // excludeReplyUserIds
                    null,   // inReplyToTweetId
                    null,   // replySettings
                    tweet.toString());
            
            out.println("Tweet posted successfully: " + tweet.toString());
            tweet = new StringBuilder(); // Clear out old tweet value
            }
        
         //Retrieve the cursor value for the next time!
         cursor = ids.getNextCursor();
         //Store the value of the cursor
         cursorEntity.setProperty("cursor", cursor);
         datastore.put(cursorEntity);     
         }
      
       catch(TwitterException e)
         {
         out.println("Problem with Twitter!");
         e.printStackTrace(response.getWriter());
         }
      catch (EntityNotFoundException e) 
         {
         out.println("Error finding Datastore Entities!");
         e.printStackTrace(response.getWriter()); 
         }
      catch(Exception e)
         {
        out.println("Error!!");
        e.printStackTrace(response.getWriter());
         }
      finally 
         {
         out.close();  // Always close the output writer
         }
      
      }
   
   // Redirect POST request to GET request.
   @Override
   public void doPost(HttpServletRequest request, HttpServletResponse response)
               throws IOException, ServletException 
      {
      doGet(request, response);
      }   
   }