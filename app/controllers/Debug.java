/*******************************************************************************
 * Debug.java
 * 
 * Implements debugging functions for testing purposes. By going to the Debug
 * page, a user will be able to choose from a set of Debug options, such as being
 * able to make all users follow them, being able to make no users follow them,
 * being able to follow all users, being able to follow no users, and creating
 * thousands of restaurants and users in the database.
 * 
 * Currently, any user who is logged in can access this, since we're still early
 * in the development stages. Obviously, this will need to be changed later on
 * down the line.
 * 
 ******************************************************************************/

package controllers;

import play.*;
import play.mvc.*;
import play.data.validation.*;

import java.util.*;
import models.*;

public class Debug extends Controller {
  @Before

  /**
   * Startup function to set the connected user.
   */
  static void setConnectedUser() {
    if(Security.isConnected()) {
      User user = User.find("byEmail", Security.connected()).first();
      renderArgs.put("loggedInAs", user);
    }
  }

  /**
   * Nothing special to do here, just render the Debug index page with debug options to choose from.
   */
  public static void index() {
    render();
  }

  /**
   * Creates 20,000 restaurants all named Per Se in New York, for debugging purposes.
   */
  public static void create_restaurants() {
    for(int i = 0; i < 20000; i++) {
            String name = "Per Se " + i;

            Restaurant restaurant = new Restaurant(name, "New York");
            restaurant.save();
    }
  }

  /**
   * Creates 2,000 users with the password "secret" and last name "Foo."
   */
  public static void create_users() {
    for(int x = 1; x <= 2000; x++) {
        String name = "User" + x;
        User user = new User(name + "@gmail.com", "secret", name, "Foo");
        user.save();
    }
  }

  /**
   * Makes all users followers of the user specified by ID.
   * Should only be used for testing purposes, dev or QA mode.
   * @param id The ID everyone should become a follower of.
   */
  public static void follow_me(Long id) {
    User who = User.findById(id);
    List<User> users = User.findAll();

    for(int x = 0; x < users.size(); x++)
            followerRelations.addFollower(users.get(x), who);
  }

  /**
   * Causes the user with ID id to follow all other users.
   * Should only be used for testing purposes, in dev or QA mode.
   * @param id The user ID which will become a follower of all other users.
   */
  public static void follow_everyone(Long id) {
    User who = User.findById(id);
    List<User> users = User.findAll();

    for(int x = 0; x < users.size(); x++)
            followerRelations.addFollower(who, users.get(x));
  }

  /**
   * Causes the user with ID id to no longer be following anyone anymore.
   * Should only be used for testing purposes, in dev or QA mode.
   * @param id The ID of the user who will no longer follow anyone.
   */
  public static void unfollow_everyone(Long id) {
    User who = User.findById(id);
    List<User> users = followerRelations.listFollowing(who);

    for(int x = 0; x < users.size(); x++)
            followerRelations.removeFollower(who, users.get(x));
  }

  /**
   * Removes all of a user's followers from their following list
   * @param id The user who will no longer have any followers
   */
  public static void remove_my_followers(Long id) {
    User who = User.findById(id);
    List<User> users = followerRelations.listFollowers(who);

    for(int x = 0; x < users.size(); x++)
            followerRelations.removeFollower(users.get(x), who);
    }

    @After
    /**
     * Displays the debug options. Automatically called after each debug operation.
     */
    public static void show_menu() {
        renderTemplate("Debug/index.html");
    }
}

