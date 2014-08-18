/*******************************************************************************
 * RestaurantProfile.java
 * 
 * Contains controller logic for creating, editing, viewing and saving restaurant
 * profiles.
 * 
 ******************************************************************************/

package controllers;

import play.*;
import play.mvc.*;
import play.data.validation.*;
import play.libs.*;
import play.cache.*;
import play.db.jpa.Blob;

import java.util.*;
import models.*;

public class RestaurantProfile extends Controller {

    @Before

    /**
     * Loads standard information that all restaurant profile pages need.
     */
    static void addDefaults() {
        renderArgs.put("blogTitle", Play.configuration.getProperty("blog.title"));
        renderArgs.put("blogBaseline", Play.configuration.getProperty("blog.baseline"));

        //Add user information, if it's needed.
        if(Security.isConnected()) {
		User user = User.find("byEmail", Security.connected()).first();
		renderArgs.put("user", user);
	}
    }

    /**
     * Main page for viewing restaurant profiles
     * @param id 
     */
    public static void index(Long id) {
        //Get restaurant & review information
        Restaurant restaurant = Restaurant.findById(id);
        List<Post> restaurantReviews = Post.findPostsByRestaurantId(id);
        String randomID = Codec.UUID();

        //Get the review of the day
        Calendar cal = Calendar.getInstance();
        List<Post> topReviews = Post.find("select p from Post p where p.restaurant = ? order by p.rating desc", restaurant).fetch(7);

        //Get the 7 best reviews for this restaurant and use modulo to change between them each day of the week
        try{
            Post reviewOfTheDay = topReviews.get(cal.get(Calendar.DAY_OF_WEEK) % topReviews.size()); //Use top reviews.size instead of 7 because there may be less than 7 reviews found
            render(restaurant, restaurantReviews, reviewOfTheDay, randomID);
        }
        catch(ArithmeticException e){ // 0 reviews returned
            render(restaurant, restaurantReviews);
        }


    }

    /**
     * Page to be called for editing restaurant info
     * @param id 
     */
    public static void editform(Long id) {
	if(!Security.isConnected())
		Useradmin.index();		//This forces the user to log in to edit the restaurant profile

        //else

        //If the user is just trying to create a restaurant, let them
        if(id == null)
		render();

	//else

	User user;            
	Restaurant restaurant = Restaurant.findById(id);

        //Check whether the user has permission to edit this restaurant
        user = User.find("byEmail", Security.connected()).first();	//We already made sure they're connected, so this will not return null

        //If restaurants have owners, their profile can only be edited by the owner. Otherwise, anyone can edit them, like a wiki.
        if(restaurant.owner == null || restaurant.owner == user || user.isAdmin)
                render(restaurant);
        else
                UserProfile.index(user.id);	//Send them back to their profile page if they don't have 
                                                //permission to edit the restaurant
    }

    /**
     * Saves the restaurant info.
     * Edit forms should be submitted to this function.
     * 
     * @param id        The restaurant's ID
     * @param name      The name of the restaurant, submitted through the form
     * @param street1   Restaurant's address
     * @param street2
     * @param city
     * @param state
     * @param zipcode
     * @param phone     Restaurant's phone number
     * @param website
     * @param cuisine   Type of food served
     * @param cost      Average price for a meal
     * @param content   Description of the restaurant
     * @param pic       Picture of a meal or of the restaurant
     */
    public static void save(Long id, @Required String name, String street1, String street2, @Required(message="Please enter a city for this restaurant.") String city, String state, String zipcode, String phone, @URL String website, String cuisine, String cost, String content, Blob pic) {
        Restaurant restaurant;

	//Create a new restaurant if we weren't editing an existing one
	if(id == null) {
		restaurant = new Restaurant(name, street1, street2, city, state, zipcode, phone, website, cuisine, cost, content, pic);
	}
	//Otherwise, update the restaurant that already exists
	else {
		restaurant = Restaurant.findById(id);
		restaurant.name = name;
		restaurant.street1 = street1;
		restaurant.street2 = street2;
		restaurant.city = city;
		restaurant.state = State.findOrCreateByName(state);
		restaurant.zipcode = zipcode;
		restaurant.phone = phone;
		restaurant.website = website;
		restaurant.cuisine = cuisine;
		restaurant.cost = cost;
		restaurant.aboutUs = content;
		restaurant.profilepic = pic;
	}
        // Validate
        validation.valid(restaurant);
        if(validation.hasErrors()) {
            render("@editform", restaurant);
        }

	//If everything went well, save and display the restaurant's profile page
	restaurant.save();
	if(id == null) {
		id = restaurant.id;
	}
	index(id);
    }

    /**
     * Lists all restaurants with a given tag.
     * @param tag The tag to search for
     */
    public static void listTagged(String tag) {
        List<Post> posts = Post.findTaggedWith(tag);
        render(tag, posts);
    }

    /**
     * Called by templates to deliver the profile picture associated with a restaurant
     * @param id The restaurant's ID
     */
    public static void profilePic(Long id) {
	Restaurant restaurant = Restaurant.findById(id);
	notFoundIfNull(restaurant);
	response.setContentTypeIfNotSet(restaurant.profilepic.type());
	if(restaurant.profilepic.get() != null){
		renderBinary(restaurant.profilepic.get());
	}
   }
}
