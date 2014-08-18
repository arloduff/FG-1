/*******************************************************************************
 * User.java
 * 
 * Model for storing users' accounts in the database.
 * 
 * Users cannot be created with About Me, profile picture, city, or state content.
 * These fields mustb e specified through the user's profile page, which is managed
 * via the UserProfile controller.
 * 
 ******************************************************************************/

package models;

import java.util.*;
import javax.persistence.*;

import play.db.jpa.*;
import play.data.validation.*;
import play.db.jpa.Blob;

@Entity
public class User extends Model {


    @Required
    public String email;            //User's e-mail address, used to identify them

    @Required
    public String password;         //Password to their account

    public String firstname;        //User's first name
    public String lastname;         //User's last name
    public String aboutme;          //About Me content on their profile page
    public Blob profilePic;         //Profile picture, if any

    public String city;             //City they're from

    @ManyToOne
    public State state;             //Their state

    public boolean isAdmin;         //Used internally for admin-only features


    /**
     * Constructor. Currently, it is not possible to upload a profile picture or
     * specify a city, state, or About Me content from the Signup page, so the
     * constructor only takes e-mail, password, and first and last name. All other
     * data must be changed via the user's profile page.
     * 
     * @param email         The user's e-mail address
     * @param password      The user's password
     * @param firstname     User's first name
     * @param lastname      User's last name
     */
    public User(String email, String password, String firstname, String lastname) {
        this.email = email;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.state = State.findOrCreateByName("");
    }

    /**
     * Looks for a user with the specified e-mail address and password combination.
     * Used to verify correct username and password.
     * 
     * @param email     The user's e-mail address
     * @param password  The user's password
     * @return          The user found
     */
    public static User connect(String email, String password) {
        return find("byEmailAndPassword", email, password).first();
    }

    /**
     * Returns the user's e-mail address
     * 
     * @return  The user's e-mail address
     */
    public String toString() {
        return email;
    }

}
