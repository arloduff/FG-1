/******************************************************************************
 * Security.java
 * 
 * Extension of the Secure class, which adds functionality to redirect users to
 * the appropriate pages after logging in or logging out.
 * 
 ******************************************************************************/

package controllers;

import models.*;

public class Security extends Secure.Security {

    /**
     * Redirects logged in users to their profile page.
     */
    public static void login() {
	render("UserProfile.html");
    }

    /**
     * Uses User.connect to verify that a username and password combination exists.
     * 
     * @param username  The username entered
     * @param password  The password entered
     * @return True on success, false on failure
     */
    static boolean authentify(String username, String password) {
        return User.connect(username, password) != null;
    }

    /**
     * Returns true if the user is an admin, false otherwise
     * @param profile The user's profile name
     * @return 
     */
    static boolean check(String profile) {
        if("admin".equals(profile)) {
            return User.find("byEmail", connected()).<User>first().isAdmin;
        } //else
        
        return false;
    }

    /**
     * Called when a user logs out. In that case, it returns them to the main index page.
     */
    static void onDisconnected() {
        Application.index();
    }

    /**
     * Logs a user in and takes them to their profile page.
     * Should be called when a user is authenticated.
     */
    static void onAuthenticated() {
        User user = User.find("byEmail", Security.connected()).first();
        UserProfile.index(user.id);
    }

}

