/*******************************************************************************
 * Reviews.java
 * 
 * Contains controller logic for displaying reviews, getting the review of the
 * day, and leaving comments.
 * 
 * Currently, the logic for displaying a review is contained here, but the logic
 * for writing a new one or editing an old one is in Userprofile.java, since
 * users can only do this from their Userprofile page.
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
import play.db.jpa.Blob;

public class Reviews extends Controller {

    @Before

    /**
     * Loads standard information that all reviews need to have.
     */
    static void addDefaults() {
        renderArgs.put("blogTitle", Play.configuration.getProperty("blog.title"));
        renderArgs.put("blogBaseline", Play.configuration.getProperty("blog.baseline"));
    }

    /**
     * Index page for all reviews. Will list the most recent posts.
     */
    public static void index() {
      Post frontPost = Post.find("order by postedAt desc").first();
      List<Post> olderPosts = Post.find("order by postedAt desc").from(1).fetch(10);
      render(frontPost, olderPosts);
    }


    /**
     * Gets the featured review of the day.
     */
    public static Post getROTD() {
        Calendar cal = Calendar.getInstance();

        //Get the 7 best reviews and use modulo to change between them each day of the week
        try{
            List<Post> topReviews = Post.find("select p from Post p order by p.rating desc").fetch(7);
            Post reviewOfTheDay = topReviews.get(cal.get(Calendar.DAY_OF_WEEK) % topReviews.size()); //Use top reviews.size instead of 7 because there may be less than 7 reviews found
            return reviewOfTheDay;
        }
        catch(ArithmeticException e){ // 0 reviews returned
            return null;
        }
    }

    /**
     * Displays a particular review
     * @param id The review to display
     */
    public static void show(Long id) {
        Post post = Post.findById(id);
        String randomID = Codec.UUID(); //Used to generate a captcha, in case the user wants to leave a comment
        render(post, randomID);
    }

    /**
     * Called when a user tries to submit a new comment
     * 
     * @param postId    The review the comment is being left on
     * @param author    The name the user left the comment by, if they're not logged in
     * @param content   The comment
     * @param code      Captcha code
     * @param randomID  Captcha code's randomID
     */
    public static void postComment(
        Long postId,
        @Required(message="Author is required") String author,
        @Required(message="A message is required") String content,
        @Required(message="Please type the code") String code,
        String randomID)
    {
        Post post = Post.findById(postId);

        //Validate the code
        validation.equals(code, Cache.get(randomID)).message("Invalid code. Please type it again");

        if(validation.hasErrors()) {
            render("Reviews/show.html", post, randomID);
        }
        
        //else
        post.addComment(author, content);
        flash.success("Thanks for posting %s", author);

        Cache.delete(randomID); //Free the randomID from the cache
        show(postId);
    }

    /**
     * Called by templates to display the captcha image
     * @param id The captcha's random ID
     */
    public static void captcha(String id) {
        Images.Captcha captcha = Images.captcha();
        String code = captcha.getText("#E4EAFD");
        Cache.set(id, code, "30mn");
        renderBinary(captcha);
    }

    /**
     * Called by templates to display the image associated with a particular review.
     * @param id 
     */
    public static void postPic(Long id) {
	Post post = Post.findById(id);
	notFoundIfNull(post);
	response.setContentTypeIfNotSet(post.pic.type());

	if(post.pic.get() != null){
		renderBinary(post.pic.get());
	}
    }

    /**
     * Lists all reviews with a specific tag
     * @param tag The tag to search for
     */
    public static void listTagged(String tag) {
        List<Post> posts = Post.findTaggedWith(tag);
        render(tag, posts);
    }
}
