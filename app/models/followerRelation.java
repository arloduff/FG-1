/*******************************************************************************
 * followerRelation.java
 * 
 * Model for representing one user being another one's follower.
 * 
 * This is implemented as its own model rather than a list of User objects in the
 * User object because at the time of writing, Play! does not support all the
 * features needed to search the database to see if a specific user is on another
 * user's Following list.
 * 
 ******************************************************************************/

package models;

import java.util.*;
import javax.persistence.*;

import play.db.jpa.*;
import play.db.jpa.Model;
import models.*;

@Entity
public class followerRelation extends Model {

    @ManyToOne
    public User follower;
    @ManyToOne
    public User followee;

    /**
     * Constructor to make one user follow another based on user IDs
     * 
     * @param followerId    ID of the person who will begin following another
     * @param followeeId    ID of the person who will be followed
     */
    public followerRelation(Long followerId, Long followeeId) {
        this.follower = User.findById(followerId);
        this.followee = User.findById(followeeId);
    }

    /**
     * Constructor to make one user follow another based on User objects
     * 
     * @param follower  The user who will begin following another
     * @param followee  The person who will be followed
     */
    public followerRelation(User follower, User followee) {
        this.follower = follower;
        this.followee = followee;
    }

}
