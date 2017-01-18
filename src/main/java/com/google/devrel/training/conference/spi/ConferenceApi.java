package com.google.devrel.training.conference.spi;

import static com.google.devrel.training.conference.service.OfyService.factory;
import static com.google.devrel.training.conference.service.OfyService.ofy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.google.devrel.training.conference.Constants;
import com.google.devrel.training.conference.domain.Conference;
import com.google.devrel.training.conference.domain.Profile;
import com.google.devrel.training.conference.form.ConferenceForm;
import com.google.devrel.training.conference.form.ProfileForm;
import com.google.devrel.training.conference.form.ProfileForm.TeeShirtSize;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.cmd.Query;
/**
 * Defines conference APIs.
 */
@Api(name = "conference", version = "v1", scopes = { Constants.EMAIL_SCOPE }, clientIds = {
        Constants.WEB_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID }, description = "API for the Conference Central Backend application.")
public class ConferenceApi {

    private static String extractDefaultDisplayNameFromEmail(String email) {
        return email == null ? null : email.substring(0, email.indexOf("@"));
    }

    /**
     * Creates or updates a Profile object associated with the given user
     * object.
     */

    // Declare this method as a method available externally through Endpoints
    @ApiMethod(name = "saveProfile", path = "profile", httpMethod = HttpMethod.POST)
    // The request that invokes this method should provide data that
    // conforms to the fields defined in ProfileForm

    public Profile saveProfile(final User user,ProfileForm profileform) throws UnauthorizedException {

        String userId = null;
        String mainEmail = null;

        // If the user is not logged in, throw an UnauthorizedException
        if(user==null){
        	throw new UnauthorizedException("Authentication Required");
        }

        // Get the userId and mainEmail
        mainEmail=user.getEmail();
        userId=user.getUserId();

        //Get display name, state (denoted by tee shirt size), city and zipcode

        String displayName=profileform.getDisplayName();
        System.out.println("Value is "+displayName);
        TeeShirtSize teeShirtSize=profileform.getTeeShirtSize();
        String city=profileform.getCity();
        int zipcode=profileform.getZipCode();

        Profile profile = (Profile) ofy().load().key(Key.create(Profile.class,userId)).now();

        if (profile==null){

        	// If the displayName is null, set it to default value based on the user's email
            // by calling extractDefaultDisplayNameFromEmail(...)
            if (displayName==null){
            	displayName=extractDefaultDisplayNameFromEmail(user.getEmail());
            }

            if (teeShirtSize==null){
            	teeShirtSize=TeeShirtSize.Alabama;
            }

            if (city==null){
            	city="Not Available";
            }

            if (zipcode==0){
            	zipcode=32608;
            }

            // Create a new Profile entity from the
            // userId, displayName, mainEmail and State (teeShirtSize)
            profile = new Profile(userId, displayName, mainEmail, teeShirtSize,city,zipcode);
        }
        else
        {
        	profile.update(displayName,teeShirtSize,city,zipcode);
        }

        // Save the Profile entity in the data-store
        ofy().save().entity(profile).now();

        // Return the profile
        return profile;
    }

    /**
     * Returns a Profile object associated with the given user object. The cloud
     * end points system automatically inject the User object.
     */
    @ApiMethod(name = "getProfile", path = "profile", httpMethod = HttpMethod.GET)
    public Profile getProfile(final User user) throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }


        // load the Profile Entity
        String userId = user.getUserId();
        Key key = Key.create(Profile.class,userId);
        Profile profile = (Profile) ofy().load().key(key).now();
        return profile;
    }
    // This method creates a new field profile for each user. SO basically each user
    // can have one or more farm profiles associated with his id.
    // METHOD FOR CREATING FARM PROFILE

    @ApiMethod(name = "createConference", path = "conference", httpMethod = HttpMethod.POST)
    public Conference createConference(final User user, final ConferenceForm conferenceForm)
        throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        // Get the userId of the logged in User
        String userId = user.getUserId();

        // Get the key for the User's Profile so that each field can be mapped to the user.
        Key<Profile> profileKey = Key.create(Profile.class, userId);

        // Allocate a key for the Field -- Here App Engine allocate the ID
        // We also to include the parent Profile in the allocated ID
        final Key<Conference> conferenceKey = factory().allocateId(profileKey, Conference.class);

        // Get the Conference Id from the Key
        final long conferenceId = conferenceKey.getId();

        // Get the existing Profile entity for the current user if there is one
        // Otherwise create a new Profile entity with default values
        //This is because while storing it in data store we have to use both parent entity as well as child
        //entity
        Profile profile = getProfileFromUser(user);

        // Create a new Field Entity, specifying the user's Profile entity
        // as the parent of the field
        Conference conference = new Conference(conferenceId, userId, conferenceForm);
        //conference.actualSensorData = conference.getActualSensorData();


        // Save Conference and Profile Entities
         ofy().save().entities(conference, profile).now();
         conference.startMonitoring();

         return conference;
         }

    /**
     * Gets the Profile entity for the current user
     */
    private static Profile getProfileFromUser(User user) {
        // First fetch the user's Profile from the data-store.
        Profile profile = ofy().load().key(
                Key.create(Profile.class, user.getUserId())).now();
        if (profile == null) {
            // Create a new Profile if it doesn't exist.
            // Use default displayName and teeShirtSize
            String email = user.getEmail();
            profile = new Profile(user.getUserId(),
                    extractDefaultDisplayNameFromEmail(email), email, TeeShirtSize.Alabama,"Not Available",32608);
        }
        return profile;
    }

    /**
     * Method to query all the fields.
     */

    @ApiMethod(
    		name="queryConferences",
    		path="queryConferences",
    		httpMethod=HttpMethod.POST
    		)

    public List <Conference> queryConferences(){
    	//Returns list of all fields.
    	Query<Conference> query= ofy().load().type(Conference.class).order("name");
    	return query.list();
    }

    /**
     * Method to query the fields that are owned by the user.
     */

    @ApiMethod(
    		name="getConferencesCreated",
    		path="getConferencesCreated",
    		httpMethod=HttpMethod.POST
    		)

    public List <Conference> getConferencesCreated(final User user)throws UnauthorizedException{
    	//Returns list of all fields that are created by the user.
		if(user==null){
			throw new UnauthorizedException("Authorization Required : Please Sign in");
		}

		String userId=user.getUserId();
		Key userKey=Key.create(Profile.class,userId);
    	return ofy().load().type(Conference.class).ancestor(userKey).order("name").list();
    }

    /**
     * Method to get details about a particular conference.
     */
    @ApiMethod(
            name = "getConference",
            path = "conference/{websafeConferenceKey}",
            httpMethod = HttpMethod.GET
    )
    public Conference getConference(
            @Named("websafeConferenceKey") final String websafeConferenceKey)
            throws NotFoundException {
        Key<Conference> conferenceKey = Key.create(websafeConferenceKey);
        Conference conference = ofy().load().key(conferenceKey).now();
        //System.out.println(conference.getActualSensorData());
        if (conference == null) {
            throw new NotFoundException("No Conference found with key: " + websafeConferenceKey);
        }
        return conference;
    }

}
