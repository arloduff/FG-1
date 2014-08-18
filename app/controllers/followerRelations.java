/*******************************************************************************
 * followerRelations.java
 * 
 * Controller for the followerRelation model.
 *
 * Handles logic for the social networking aspect of the website, e.g. adding or
 * removing followers, listing all users being followed by a specific user, listing
 * all the user's followers, etc.
 * 
 * This controller does not correspond to any view. Its purpose is to be called by
 * other controllers to retrieve information about followers/followees.
 * 
 ******************************************************************************/

package controllers;

import play.*;
import play.mvc.*;
import play.utils.*;

import java.util.*;
import javax.persistence.*;

import play.data.validation.*;
import play.db.jpa.Model;
import models.*;

public class followerRelations extends Controller {





//Social networking functions to add or remove users




    /**
     * Causes one user to follow another.
     * Should be called when the user clicks Follow on another person's profile
     * Todo: Add block functionality so that users cannot follow others who have blocked them
     * @param follower  The current user
     * @param followee  The person they want to follow
     * @return 
     */
    public static boolean addFollower(User follower, User followee) {
       //If the user isn't logged in, they shouldn't be able to add followers
       if(!Security.isConnected()) {
            return false;
        }

       //Get the user who's logged in
       User loggedInAs = User.find("byEmail", Security.connected()).first();

        //User can't add a follower unless he's the user following, or an admin
        if(loggedInAs != follower && !loggedInAs.isAdmin) {
            return false;
        }

        //User can't follow himself
        if(follower == followee)
                return false;

        followerRelation friendship = followerRelation.find("select f from followerRelation f where f.follower = ?1 and f.followee = ?2", follower, followee).first();

        //If there's already a  friendship to begin with, we can't add it again.
        if(friendship != null) {
            return false;
        }


        //If all checks are passed, add the follower relation
        friendship = new followerRelation(follower, followee);
        friendship.save();

        return true;
    }

    /**
     * Calls the other addFollower function to cause one user to follow another
     * @param followerId    The ID of the follower user
     * @param followeeId    The ID of the user being followed
     * @return 
     */
    public static boolean addFollower(Long followerId, Long followeeId) {
        User follower = User.findById(followerId);
        User followee = User.findById(followeeId);

        return(addFollower(follower, followee));
    }

    /**
     * Causes a follower to no longer follow another user
     * Should be called when the user clicks the Unfollow button
     * @param follower  The user currently following the other user
     * @param followee  The user being followed
     * @return 
     */
    public static boolean removeFollower(User follower, User followee) {
      //If the user isn't logged in, they shouldn't be able to add followers
      if(!Security.isConnected()) {
            return false;
        }

        User loggedInAs = User.find("byEmail", Security.connected()).first();

        //User can't add a follower unless he's the user following, or an admin
        if(loggedInAs != follower && !loggedInAs.isAdmin) {
            return false;
        }

        followerRelation friendship = followerRelation.find("select f from followerRelation f where f. follower = ?1 and f.followee = ?2", follower, followee).first();

        //If there was no friendship to begin with, an error occurred.
        if(friendship == null) {
            return false;
        }

        //Otherwise, delete it.
        friendship.delete();
        return true;
    }

    /**
     * Calls the other removeFollower function to cause a user to no longer follow another
     * @param followerId    The ID of the person currently following another
     * @param followeeId    The ID of the person being followed
     * @return 
     */
    public static boolean removeFollower(Long followerId, Long followeeId) {
        User follower = User.findById(followerId);
        User followee = User.findById(followeeId);

        return(removeFollower(follower, followee));
    }




//Boolean functions to check if a user is following or being followed by another user




    /**
     * Checks whether one user is currently following another.
     * @param follower  The person supposedly following another
     * @param followee  The person being followed
     * @return true if followee is currently being followed by follower, false otherwise.
     */
    public static boolean isFollowing(User follower, User followee) {
        followerRelation friendship = followerRelation.find("select f from followerRelation f where f.follower = ?1 and f.followee = ?2", follower, followee).first();

        return(friendship != null);
    }

    /**
     * Calls the other isFollowing function to determine whether one user is currently following another.
     * @param followerId    The ID of the person supposedly following another
     * @param followeeId    The ID of the person being followed
     * @return true if followee is currently being followed by follower, false otherwise.
     */
    public static boolean isFollowing(Long followerId, Long followeeId) {
        User follower = User.findById(followerId);
        User followee = User.findById(followeeId);

        return(isFollowing(follower, followee));
    }

    /**
     * Checks whether a user is being followed by another
     * @param followed      The user supposedly being followed
     * @param followedBy    The user supposedly following
     * @return true if followed is being followed by followedBy, false otherwise
     */
    public static boolean isFollowedBy(User followed, User followedBy) {
        followerRelation friendship = followerRelation.find("select f from followerRelation f where f.follower = ?1 and f.followee = ?2", followedBy, followed).first();

        return(friendship != null);
    }

    /**
     * 
     * Calls the other isFollowedBy function to check whether a user is being followed by another
     * @param followed      The ID of the user supposedly being followed
     * @param followedBy    The ID of the user supposedly following
     * @return true if followed is being followed by followedBy, false otherwise
     */
    public static boolean isFollowedBy(Long followedId, Long followedById) {
        User followed = User.findById(followedId);
        User followedBy = User.findById(followedById);
        return(isFollowedBy(followed, followedBy));

    }




//Functions to list a user's followers/Users a user is following



    /**
     * Returns the list of users a user is following.
     * @param follower  The user whose follower list will be returned
     * @param amount    The maximum amount of users to return
     * @return List of users being followed by follower
     */
    public static List<User> listFollowing(User follower, int amount) {
        List<User> followingList = followerRelation.find("select f.followee from followerRelation f where f.follower = ?", follower).fetch(amount);

        return followingList;
    }

    /**
     * Returns a user's entire following list
     * @param follower  The user whose list to return
     * @return  The list of users being followed by follower
     */
    public static List<User> listFollowing(User follower) {
        List<User> followingList = followerRelation.find("select f.followee from followerRelation f where f.follower = ?", follower).fetch();

        return followingList;
    }

    /**
     * Calls the other listFollowing function tp return a user's entire following list
     * @param follower  The ID of the user whose list to return
     * @return  The list of users being followed by follower
     */
    public static List<User> listFollowing(Long followerId, int amount) {
        User follower = User.findById(followerId);

        return(listFollowing(follower, amount));
    }

    /**
     * Returns a list of users following a specific user.
     * @param followee  The user who is being followed
     * @param amount    A maximum number of users to return
     * @return A list of users following the followee
     */
    public static List<User> listFollowers(User followee, int amount) {
            List<User> followersList = followerRelation.find("select f.follower from followerRelation f where f.followee = ?", followee).fetch(amount);

            return followersList;
    }

    /**
     * Returns a list of all people following a specific user.
     * @param followee  The user who is being followed
     * @return  A list of users following the followee
     */
    public static List<User> listFollowers(User followee) {
        List<User> followersList = followerRelation.find("select f.follower from followerRelation f where f.followee = ?", followee).fetch();

        return followersList;
    }

    /**
     * Returns a list of all people following a specific user.
     * @param followee  The ID of the user who is being followed
     * @return  A list of users following the followee
     */
    public static List<User> listFollowers(Long followeeId) {
            User followee = User.findById(followeeId);

            return(listFollowers(followee));
    }
}

