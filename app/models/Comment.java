/******************************************************************************
 * Comment.java
 * 
 * Comment model for users leaving comments.
 * Currently, contains two constructors for users to leave comments as guests or
 * while logged in. Also contains a toShortString() function to display a comment
 * snippet.
 * 
 ******************************************************************************/

package models;
 
import java.util.*;
import javax.persistence.*;
 
import play.db.jpa.*;
import play.data.validation.*;
 
@Entity
public class Comment extends Model {
 
    public String guest_name;   //Used if the author leaves a comment as a guest
    @ManyToOne
    public User author;         //Used if the author leaves a comment while logged in
    
    @Required
    public Date postedAt;       //Date posted
     
    @Lob
    @Required
    @MaxSize(10000)
    public String content;      //Content of the comment
    
    @ManyToOne
    @Required
    public Post post;           //Post the comment is on
    
    /**
     * Constructor to create a new comment when submitted by a guest
     * 
     * @param post      The post the comment is on
     * @param author    The name the commenter entered when leaving the comment
     * @param content   The content of the comment
     */
    public Comment(Post post, String author, String content) {
        this.post = post;
        this.guest_name = author;
        this.content = content;
        this.postedAt = new Date();
    }
    
    /**
     * Constructor to create a new comment when submitted by a logged-in user
     * 
     * @param post      The post the comment is on
     * @param author    The user who left the comment
     * @param content   The content of the comment
     */
    public Comment(Post post, User author, String content) {
        this.post = post;
        this.author = author;
        this.content = content;
        this.postedAt = new Date();
    }

    /**
     * Converts the text of the comment to a length of 53 characters or less.
     * Used for displaying comment snippets, for example, on a list of all posts
     * 
     * @return The first 53 characters of the comment, with an elipses ("...")
     * added to the end if the comment is longer
     */
    public String toString() {
        return content.length() > 50 ? content.substring(0, 50) + "..." : content;
    }
 
}
