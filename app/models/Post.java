/******************************************************************************
 * Post.java
 * 
 * Post model used to store posts the user entered on their profile page.
 * 
 * A post is the same thing as a review. There is no distinct Review object. Posts
 * may contain a rating and restaurant, but do not need to. In this way, the user
 * may choose to write something about a specific restaurant they visited, which
 * is the purpose of the site, but they may also just post some generic information
 * to their profile page.
 * 
 * The logic for creating, editing, and deleting posts is kept in the Reviews
 * controller, because the Posts controller is used by the CRUD plugin to enable
 * admins to edit or delete all posts.
 * 
 ******************************************************************************/

package models;

import java.util.*;
import javax.persistence.*;

import play.data.binding.*;
import play.data.validation.*;
import play.db.jpa.Model;
import play.db.jpa.Blob;


@Entity
public class Post extends Model {

    @Required
    public String title;            //Title of the post

    @Required @As("yyyy-MM-dd")
    public Date postedAt;           //Date posted

    @Lob
    @Required
    @MaxSize(15000)
    public String content;          //Content of the post

    public Blob pic;                //Picture for the post, if any

    @Required
    @ManyToOne
    public User author;             //User who authored the post. Unlike comments,
                                    //users must be logged in to post.
    @OneToMany(mappedBy="post", cascade=CascadeType.ALL)
    public List<Comment> comments;  //Comments, if any

    @ManyToOne
    public Restaurant restaurant;   //Restaurant associated with the post, if any

    public int rating;              //Rating, if any

    public int likecount = 0;       //Post likes. Not yet implemented

    @ManyToMany(cascade=CascadeType.PERSIST)
    public Set<Tag> tags;           //Tags for the post

//Constructors

     /**
     * Constructor taking the author, title, and content
     * 
     * @param author    User who wrote the post
     * @param title     Title of the post
     * @param content   Content of the post
     */
    public Post(User author, String title, String content) {
        //All posts must have a list of comments and tags, even if they're empty
        this.comments = new ArrayList<Comment>();
        this.tags = new TreeSet();

        this.author = author;
        this.title = title;
        this.content = content;
        this.likecount = 0;
        this.postedAt = new Date();
    }


    /**
     * Constructor for posts that have a picture but no restaurant
     * 
     * @param author    User who wrote the post
     * @param title     Title of the post
     * @param content   Content of the post
     * @param pic       Picture associated with the post
     */
    public Post(User author, String title, String content, Blob pic) {
        //All posts must have a list of comments, even if they're empty
        this.comments = new ArrayList<Comment>();
        this.tags = new TreeSet();

        this.author = author;
        this.title = title;
        this.content = content;
        this.pic = pic;
        this.likecount = 0;
        this.postedAt = new Date();
    }

//Constructors with restaurant specified

     /**
     * Constructor for posts that specify a restaurant and a city
     * Even if the user doesn't enter a city, the Restaurant.findOrCreateByName
     * function will give the restaurant the user's home city as a default, unless
     * they change it. So there is no constructor for taking only a restaurant but
     * no city.
     * 
     * @param author        User who wrote the post
     * @param title         Title of the post
     * @param content       Content of the post
     * @param restaurant    Restaurant name
     * @param city          City the restaurant is in
     */
    public Post(User author, String title, String content, String restaurant, String city) {
        //All posts must have a list of comments, even if they're empty
        this.comments = new ArrayList<Comment>();
        this.tags = new TreeSet();

        this.author = author;
        this.title = title;
        this.content = content;
        this.likecount = 0;
        this.postedAt = new Date();
        this.restaurant = Restaurant.findOrCreateByName(restaurant, city);
    }

    /**
     * Constructor for posts with a restaurant, city, and picture
     * 
     * @param author        The user who wrote the post
     * @param title         The title of the post
     * @param restaurant    The restaurant's name
     * @param city          The city the restaurant is in
     * @param content       The content of the post
     * @param pic           The picture associated with the post
     */
    public Post(User author, String title, String restaurant, String city, String content, Blob pic) {
        //All posts must have a list of comments, even if they're empty
        this.comments = new ArrayList<Comment>();
        this.tags = new TreeSet();

        this.author = author;
        this.title = title;
        this.content = content;
        this.pic = pic;
        this.likecount = 0;
        this.postedAt = new Date();
        this.restaurant = Restaurant.findOrCreateByName(restaurant, city);
    }

//Other functions

    /**
     * Adds a comment from a guest
     * 
     * @param author    The name the guest entered to leave the comment
     * @param content   The content of the comment
     * @return          A reference to the modified Post object
     */
    public Post addComment(String author, String content) {
        Comment newComment = new Comment(this, author, content);
        this.comments.add(newComment);
        this.save();
        return this;
    }

    /**
     * Adds a comment when the user is signed in
     * 
     * @param author    The user who left the comment
     * @param content   The content of the comment
     * @return          A reference to the modified Post object
     */
    public Post addComment(User author, String content) {
        Comment newComment = new Comment(this, author, content);
        this.comments.add(newComment);
        this.save();
        return this;
    }


    /**
     * Finds the last post entered before this one. These are not grouped by a specific
     * user, but are any post written by any user. This would be used, for example,
     * if the user were browsing all posts on the homepage.
     * 
     * @return The last post entered before this one.
     */
    public Post previous() {
        return Post.find("postedAt < ? order by postedAt desc", postedAt).first();
    }

    /**
     * Finds the next post entered after this one. These are not grouped by a specific
     * user, but are any post written by any user. This would be used, for example,
     * if the user were browsing all posts on the homepage.
     * 
     * @return The next post entered before this one.
     */
    public Post next() {
        return Post.find("postedAt > ? order by postedAt asc", postedAt).first();
    }

    /**
     * Adds a tag to this post.
     * 
     * @param name  The tag to add.
     * @return      A reference to the modified post.
     */
    public Post tagItWith(String name) {
        tags.add(Tag.findOrCreateByName(name));
        return this;
    }

    /**
     * Returns a list of all posts containing a specific tag.
     * 
     * @param tag   The tag to search for
     * @return      A list of posts which contain the tag
     */
    public static List<Post> findTaggedWith(String tag) {
        return Post.find(
            "select distinct p from Post p join p.tags as t where t.name = ?",
            tag
        ).fetch();
    }

    /**
     * Returns a list of all posts containing several specified tags
     * 
     * @param tags  The tags to search for
     * @return      A list of posts that have the tags
     */
    public static List<Post> findTaggedWith(String... tags) {
        return Post.find(
            "select distinct p.id from Post p join p.tags as t where t.name in (:tags) group by p.id having count(t.id) = :size"
        ).bind("tags", tags).bind("size", tags.length).fetch();
    }

    /**
     * Finds all posts associated with a specific restaurant
     * 
     * @param restId    The ID of the restaurant to search for
     * @return          A list of posts for a specific restaurant
     */
    public static List<Post> findPostsByRestaurantId(Long restId){
            return Post.find(
                    "select distinct p from Post p where restaurant_id = ?", restId
            ).fetch();
    }

    /**
     * Returns the string representation of the post, in this case, the title
     * 
     * @return  The post's title
     */
    public String toString() {
        return title;
    }

}
