package com.google.devrel.training.conference.domain;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.devrel.training.conference.service.WeaveService;
import com.google.devrel.training.conference.service.Notifications;
import com.google.devrel.training.conference.domain.Conference;

import static com.google.devrel.training.conference.service.OfyService.factory;
import static com.google.devrel.training.conference.service.OfyService.ofy;
import com.googlecode.objectify.condition.IfNotDefault;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.cmd.Query;


import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.verbs.TwiMLException;

import java.util.*;

//class WeaveDaemon extends TimerTask {

//@SuppressWarnings("serial")
public class WeaveDaemon extends HttpServlet {
    //public Conference field_ref;
    //public String phn;
    private static Queue queue;
    //public WeaveDaemon(Conference ref, String phn) {
    //    this.field_ref = ref;
    //    this.phn = "+1" + phn;
    //}

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //public void run() {
            try {
                queue = QueueFactory.getDefaultQueue();

                String param_key = req.getParameter("key");
                String phn = req.getParameter("phn");

                System.out.println("WeaveDaemon:" + param_key + " " + phn);

                Key<Conference> conferenceKey = Key.create(param_key);
                Conference conference = ofy().load().key(conferenceKey).now();

                //String weave_sensor_data = new WeaveService().run();
                String weave_sensor_data = "30";
                FarmAnalytics should_send_notif = FarmAnalytics.computeSense(conference,weave_sensor_data);
                if(true) {

                        String msg = should_send_notif.str;
                        //String msg = "Moisture level: " + weave_sensor_data + ", Farm needs water!";
                        Notifications.sendNotif(phn,msg);
                }

                queue.add(TaskOptions.Builder.withCountdownMillis((long)30000)
                    .url("/weave_daemon")
                    .param("key", param_key)
                    .param("phn", phn)
                    );
            } catch (Exception e) {
                throw new RuntimeException("Couldn't send SMS", e);
            }
        //}
    }
}
