/******************************************************************************
 * Application.java
 * 
 * This file contains the main application logic used by Fotogrub, including the
 * logic to get the current logged in user, set the logged in user, get default
 * values needed by all templates, and list posts with specific criteria.
 * 
 ******************************************************************************/

package controllers;

import play.*;
import play.mvc.*;
import play.data.validation.*;
import play.libs.*;
import play.cache.*;

import java.util.*;
import models.*;

//Main class for this web application
public class Application extends Controller {

  @Before
  /**
   * Used for logging users in.
   * 
   */
  static void setConnectedUser() {
    if(Security.isConnected()) {
      User user = User.find("byEmail", Security.connected()).first();
      renderArgs.put("loggedInAs", user);
    }
  }

  
  /**
   * Default variables to be set for all templates
   * 
   */
  static void addDefaults() {
        renderArgs.put("blogTitle", Play.configuration.getProperty("blog.title"));
        renderArgs.put("blogBaseline", Play.configuration.getProperty("blog.baseline"));
				
        if(Security.isConnected()) {
            User user = User.find("byEmail", Security.connected()).first();
            renderArgs.put("user", user);
        }
    }

  /**
   * Index page for our website
   * 
   */
  public static void index() {
        //Connection conn = DB.getConnection();
        //conn.createStatement().execute("select * from products");

      //Get a list of all restaurants for the sidebar
      List<Restaurant> restaurants = Restaurant.find("order by rating desc").from(1).fetch(10);

        Post frontPost = Post.find("order by postedAt desc").first();
        render(restaurants, frontPost);
    }

  /**
   * Shows a specific post (restaurant review)
   * @param id The ID of the post
   */
  public static void show(Long id) {
        Post post = Post.findById(id);
        String randomID = Codec.UUID();
        render(post, randomID);
    }

  /**
   * Adds to a post's like count. Should be called when the Like button is clicked.
   * @param id The ID of the post
   */
  public static void addLikeCount(Long id){
    Post post = Post.findById(id);
    post.likecount = post.likecount++;
    post.save();
}


  /**
   * Should be called after the user clicks the Submit button on a form to enter a new comment
   * 
   * @param postId  The post the user is commenting on
   * @param userId  The user who is writing the comment
   * @param author  The name the user entered to comment, if they aren't logged in
   * @param content The comment
   * @param code    Captcha, only required if the user isn't logged in
   * @param randomID Random ID generated for the captcha
   */
  public static void postComment(
        Long postId,
        Long userId,
        String author,
        @Required(message="A message is required") String content,
        String code,
        String randomID)
        {
            String author_name = author;

            if(userId == null) {			//If the user isn't logged in, they need to enter the captcha
                validation.required(author).message("Please enter your name.");
                validation.required(code).message("You must login or enter the code to post a comment.");
                validation.required(randomID);
                validation.equals(code, Cache.get(randomID)).message("Invalid code. Please type it again");
            }


            Post post = Post.findById(postId);
            if(validation.hasErrors()) {
                render("Application/show.html", post, randomID);
            }

            //Call two different overloaded functions, depending on whether the user is posting as a guest or a logged in user.
            if(userId == null)
                post.addComment(author, content);

            else {
                    User user = User.findById(userId);
                    post.addComment(user, content);
                    author_name = user.firstname;
            }

            flash.success("Thanks for posting, %s.", author_name);
            Cache.delete(randomID);
            show(postId);
    }

   /**
   * Function called by the template to generate a random capctha image
   * @param id The captcha's random ID
   */
    public static void captcha(String id) {
        Images.Captcha captcha = Images.captcha();
        String code = captcha.getText("#E4EAFD");
        Cache.set(id, code, "30mn");    //Capctha will expire after 30 minutes.
        renderBinary(captcha);
    }

    /**
     * Lists all posts with a specific tag
     * @param tag The tag to search for
     */
    public static void listTagged(String tag) {
        List<Post> posts = Post.findTaggedWith(tag);
        render(tag, posts);
    }

    /**
     * Function called by the templates to render the image associated with a specific post
     * @param id The post ID
     */
    public static void postPic(Long id) {
	Post post = Post.findById(id);

        //If it's not found, render the appropriate "Not found" message
        notFoundIfNull(post);
	response.setContentTypeIfNotSet(post.pic.type());
	
        //Otherwise, render the image
        if(post.pic.get() != null){
		renderBinary(post.pic.get());
	}
	
    }

    /**
     * Returns whether the user is currently logged in.
     * Works the same as Security.isConnected(), but is implemented separately just to give clients easier accessibility to this function.
     * @return 
     */
    public static boolean isLoggedIn() {
	return Security.isConnected();
    }

    /**
     * Returns the current user who is logged in.
     * @return The User object representing the logged in user. Returns null if not logged in.
     */
    public static User getUser() {
	if(Security.isConnected()) {
            User user = User.find("byEmail", Security.connected()).first();
            return user;
	}

	else return null;   //Return null if no one is logged in.
    }
}
