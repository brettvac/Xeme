/*
 * Copyright (c) 2017-2026 Brett
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 */

package io.github.brettvac.xeme;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class XemeServlet extends HttpServlet 
   {

   private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
   private static Entity oauthEntity,spamEntity;
   
   public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException 
      {     
      PrintWriter out = response.getWriter();
      
      response.setContentType("text/html; charset=UTF-8");
      
      out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
      out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\" data-theme=\"light\">");
      out.println("<head>");
      out.println("  <title>Xeme Status</title>");
      out.println("  <link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/@picocss/pico@2.1.1/css/pico.min.css\" />");
      out.println("</head>");
      out.println("<body>");
      out.println("  <main class=\"container\" style=\"max-width: 800px;\">");
      
      try 
         {                    
         //Get Spamming values from the form and save them to Datastore
         spamEntity = new Entity("spamEntity", "SP");
          
         String host = request.getParameter("host");
         spamEntity.setProperty("host", host);
            
         String message = request.getParameter("message");
         spamEntity.setProperty("message", message);

         String frequency = request.getParameter("frequency");
         spamEntity.setProperty("frequency", frequency);

         datastore.put(spamEntity);
            
         out.println("<h1>Xeme Configuration Status</h1>");
   
         out.println("<p>Parameters set and saved to Datastore:</p>");
         out.println("<ul>");
         out.println("<li>Host: " + host + "</li>");
         out.println("<li>Message: " + message + "</li>");
         out.println("<li>Frequency: " + frequency + "</li>");
         out.println("</ul>");
         
         // Validate that values exist in the datastore entity before displaying the cron server link
         if (spamEntity.getProperty("host") != null && spamEntity.getProperty("message") != null && spamEntity.getProperty("frequency") != null)   
              {
              out.println("<p>Xeme cron job is <a href=\"/cron/XemeCronServlet\">running here</a>.</p>");
              }
         
         // Back to index button
         out.println("<form action=\"index.html\" method=\"get\">");
         out.println("<input type=\"submit\" value=\"Back to Index\" />");
         out.println("</form>");
         }
      
      catch(DatastoreFailureException e)
         {
        out.println("Problem with DataStore!");
        out.println("<br />"); 
        e.printStackTrace(response.getWriter());
         }
      
       catch(Exception e)
         {
        out.println("Error!!");
        out.println("<br />"); 
        e.printStackTrace(response.getWriter());
         }
      finally 
         {
         out.println("  </main>");
         out.println("</body>");
         out.println("</html>");
         out.close();  // Always close the output writer
         }
      }
   
   // Redirect POST request to GET request.
   public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException 
      {
      doGet(request, response);
      }   
   } 