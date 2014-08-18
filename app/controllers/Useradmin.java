/*******************************************************************************
 * Useradmin.java
 * 
 * Controller for admin editing logic
 * 
 ******************************************************************************/

package controllers;

import play.*;
import play.mvc.*;
import play.data.validation.*;
import play.db.jpa.Blob;
import play.db.jpa.Model;

import java.util.*;

import models.*;

//Make sure only admins can log in
@With(Secure.class)
public class Useradmin extends Controller {

    @Before
    
    /**
     * Set the connected user prior to doing anything else so all functions will
     * have it.
     */
    static void setConnectedUser() {
        if(Security.isConnected()) {
            User user = User.find("byEmail", Security.connected()).first();
            renderArgs.put("user", user.firstname);
        }
    }

    /**
     * Admin tools main page. By default, it lists all the current user's posts.
     */
    public static void index() {
        List<Post> posts = Post.find("author.email", Security.connected()).fetch();
        render(posts);
    }

    /**
     * Form for admins to edit other posts.
     * @param id The ID of the post to edit.
     */
    public static void form(Long id) {
        if(id != null) {
            Post post = Post.findById(id);
            render(post);
        }
        render();
    }

    /**
     * Allows admins to edit posts
     * 
     * @param id            The ID of the review, if any
     * @param title         The title of the review
     * @param restaurant    Name of the restaurant
     * @param rating        Rating, from 1 to 5
     * @param street1       Restaurant's address
     * @param street2
     * @param city
     * @param state
     * @param zipcode
     * @param phone         Restaurant's phone number
     * @param website       Restaurant's website
     * @param cuisine       Type of food served
     * @param cost          Average cost for a meal at the restaurant
     * @param content       Content of the review
     * @param tags          Tags associated with the review
     * @param pic           Picture for the review
     */
    public static void save(Long id,
        @Required(message="Title is required.") String title,
        @Required(message="Restaurant is required.") String restaurant,
        int rating,
        String street1,
        String street2,
        @Required(message="City is required.") String city,
        String state,
        String zipcode,
        String phone,
        String website,
        String cuisine,
        String cost,
        @Required(message="Content is required.") String content,
        String tags, Blob pic) {

        Post post;
        if(id == null) {
            // Create pPer Seost
            User author = User.find("byEmail", Security.connected()).first();
            post = new Post(author, title, restaurant, city, content, pic);
        } else {
            // Retrieve post
            post = Post.findById(id);
            post.title = title;
            post.content = content;
						post.restaurant = Restaurant.findOrCreateByName(restaurant, city);
            post.tags.clear();
            post.pic = pic;
        }

        post.rating = rating;

        //Set restaurant data
        post.restaurant.street1 = street1;
        post.restaurant.street2 = street2;
        post.restaurant.state = State.findOrCreateByName(state);
        post.restaurant.zipcode = zipcode;
        post.restaurant.phone = phone;
        post.restaurant.website = website;
        post.restaurant.cuisine = cuisine;
        post.restaurant.cost = cost;
        post.restaurant.save();


        // Set tags list
        for(String tag : tags.split("\\s+")) {
            if(tag.trim().length() > 0) {
                post.tags.add(Tag.findOrCreateByName(tag));
            }
        }
        // Validate
        validation.valid(post);
        if(validation.hasErrors()) {
            render("@form", post);
        }
        // Save
        post.save();
        index();
    }

    /**
     * Capability for admins to delete posts
     * @param id The ID of the post to delete
     */
    public static void deletePost(Long id) {
        Post post = Post.findById(id);
        post.delete();

    }
}
