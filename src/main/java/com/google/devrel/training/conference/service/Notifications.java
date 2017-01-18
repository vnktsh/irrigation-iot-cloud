package com.google.devrel.training.conference.service;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.TwilioRestResponse;

import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;
import com.twilio.sdk.resource.instance.Account;

import com.twilio.sdk.verbs.TwiMLException;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class Notifications {

  public static final String ACCOUNT_SID = "AC6d43ef940717329cddd0e8c3f7ac3e35";
  public static final String AUTH_TOKEN = "f3beb7a4d6118aad319279f73e2d6353";
  private static final TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);

  public Notifications() {
  }

  public static void sendNotif(String toNumber, String msg) throws TwilioRestException, TwiMLException {
      try {
          System.out.println("inside sendnotif");
          Account mainAccount = client.getAccount();
          System.out.println(mainAccount);

          MessageFactory messageFactory = mainAccount.getMessageFactory();
          System.out.println(messageFactory);
          List<NameValuePair> messageParams = new ArrayList<NameValuePair>();
          messageParams.add(new BasicNameValuePair("To", toNumber));
          messageParams.add(new BasicNameValuePair("From", "+12016763550"));
          messageParams.add(new BasicNameValuePair("Body", msg));
          Message message = messageFactory.create(messageParams);
          System.out.println(message.getSid());
      } catch (Exception e) { throw new RuntimeException("Couldn't send SMS", e); }
  }
 }
