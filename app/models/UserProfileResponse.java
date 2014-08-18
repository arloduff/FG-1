/*******************************************************************************
 * UserProfileResponse.java
 * 
 * Model used internally by the UserProfile controller to construct JSON responses.
 * 
 * A UserProfileResponse object has two fields: the name of the user, and success
 * which is true or false.
 * 
 * Typically, these objects are used to send responses to AJAX requests which want
 * to know certain boolean information about a specific user, such as whether that
 * user is following another. But they can be used for any purpose which requires
 * a username and a true or false value.
 * 
 * Note that we use these for responses to AJAX requests which will be displayed
 * to the user. That's why the code creates a UserProfileResponse object with the
 * name of the individual, rather than the individual's ID.
 * 
 ******************************************************************************/

package models;

public class UserProfileResponse {
    boolean success;
    String name;

    /**
     * Constructor. Creates a new UserProfileResponse object with whether or not
     * the operation failed or was successful, and name settings.
     * 
     * @param success
     * @param name 
     */
    public UserProfileResponse(boolean success, String name) {
        this.success = success;
        this.name = name;
    }
}
