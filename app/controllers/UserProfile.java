/*******************************************************************************
 * Userprofile.java
 * 
 * Controller for the user's profile page. Contains logic to create, edit, and
 * delete posts, and add or remove followers, as well as functions used by the
 * profile page to display JSON data or output for AJAX functions, etc.
 * 
 ******************************************************************************/

package controllers;

import play.*;
import play.mvc.*;

import play.libs.*;
import play.cache.*;
import play.db.jpa.Blob;
import play.data.validation.*;
import play.utils.*;

import java.util.*;
import models.*;
import controllers.*;

import play.modules.paginate.*;
import play.modules.paginate.ModelPaginator.*;

import play.vfs.VirtualFile;


public class UserProfile extends Controller {
	/* Number of users to display on followers/following windows */
	public static final int SN_LIST_SIZE = 10;

  @Before

   /**
   * Makes sure all functions have access to the current user before doing anything
   * else
   */
  static void setConnectedUser() {
    if(Security.isConnected()) {
      User user = User.find("byEmail", Security.connected()).first();
      renderArgs.put("loggedInAs", user);
    }
  }

    public static void index(Long id) {
            if(id == null)	{				//Redirect to the login page if no id is found
                    try {
                            Secure.login();
                    }
                    catch (Throwable e) {
                            Application.index();
                    };
            }

            User user = User.findById(id);
            List<Post> posts = Post.find("select p from Post p where p.author = ? order by postedAt desc", user).fetch(5);
            List<Comment> comments = Comment.find("select c from Comment c where c.author = ? order by postedAt desc", user).fetch(5);

            if(user!= null) {
                    render(user, posts, comments);
            }
            else {
                    renderTemplate("UserProfile/notfound.html");
            }
    }

    public static void Signup() {
            String randomID = Codec.UUID();

            render(randomID);
    }

    public static void registerUser(
    @Required(message="Please enter your name.") String firstname,
    @Required(message="Please enter your last name.") String lastname,
    @Email @Required(message="A valid e-mail address is required.") String email,
    @Required (message="Please enter a password.") String password,
    @Required (message="Please confirm your password.") String confirm,
    @Required (message="Please enter the code.") String captcha,
    String randomID) {

//Make sure password is confirmed and the captcha is right.
            validation.equals(password, confirm).message("The passwords do not match.");
            validation.equals(captcha, Cache.get(randomID)).message("Please type the code again.");

            User user = new User(email, password, firstname, lastname);

//Validate
            validation.valid(user);
            if(validation.hasErrors()) {
                    render("@Signup");
            }

            user.save();
            Cache.delete(randomID);
            session.put("username", user);

            flash.success("1");

            editform(user.id);
    }

    /**
     * Edit form for users to edit their profiles
     * Should be called when the user clicks Edit Profile
     * @param id The ID of the post to edit
     */
    public static void editform(Long id) {
            User user = User.findById(id);

            render(user);
    }

    /**
     * Saves the user's profile
     * Should be called after the user clicks Save after editing their profile
     * 
     * @param id            The user's ID
     * @param profilePic    The user's profile pic
     * @param firstname     The user's first name
     * @param lastname      The user's last name
     * @param email         The user's e-mail address
     * @param city          The user's city
     * @param state         The user's state
     * @param aboutme       The "About Me" text
     */
    public static void save(
        Long id,
        Blob profilePic,
        @Required String firstname,
        @Required String lastname,
        @Email @Required String email,
        String city,
        String state,
        String aboutme) {


        //Get the user
        User user = User.findById(id);

        //Set the info to what they entered
        user.firstname = firstname;
        user.lastname = lastname;
        user.email = email;
        user.city = city;
        user.state = State.findOrCreateByName(state);

        //Only set a profile pic if they uploaded one
        if(profilePic != null) {
                user.profilePic = profilePic;
        }

        user.aboutme = aboutme;

        //Make sure everything entered was okay
        validation.valid(user);
        if(validation.hasErrors()) {
                render("@editform", user);
        } //else

        user.save();

        index(id);
    }

    /**
     * Creates a new post authored by the current user
     * Should be called when the user clicks Submit after writing a new post
     * 
     * @param id                The ID of the post, if any
     * @param title             The title of the post
     * @param message           The post content
     * @param restaurantData    The name of the restaurant, with optional City and State separated by commas.
     *                          If entered, will assume the restaurant is in the same city as specified in the user's profile
     * @param rating 
     */
    public static void newPost(Long id,
        @Required(message = "Please enter a title for your post") String title,
        @Required(message = "Please enter a message for your post") String message,
        String restaurantData,
        int rating) {

        //Find the user and create a new post authored by them
        User user = User.findById(id);
        Post post = new Post(user, title, message);

        //If the user specified a restaurant, look for one in their city to associate the post with. Create the restaurant if it doesn't already exist
        if(restaurantData.length() != 0)	{		//Parse data from Restaurant field
            Restaurant restaurant = find_or_create_restaurant(restaurantData, user.city, user.state);
            post.restaurant = restaurant;
        }

        post.rating = rating;

        //Make sure everything's valid
        if(validation.hasErrors()) {
            List<Post> posts = Post.find("select p from Post p where p.author = ? order by postedAt desc", user).fetch(20);

            params.flash();
            render("@index", user, posts);
        } //else

        post.save();
        index(id);
    }

    /**
     * Calls the view template which allows the user to edit their posts
     * Should be called after the user clicks Edit next to a post
     * 
     * @param id The ID of the post to edit
     */
    public static void editPost(Long id) {
        //Don't do anything unless the user is trying to edit their own post
        //Todo: add an error page or a 403 response if the user tries to edit another person's post
        if(Security.isConnected()) {
                User user = User.find("byEmail", Security.connected()).first();
                Post post = Post.find("byId", id).first();

                if(post.author == user)
                        render(post, user);
        }
    }

    /**
     * Saves an edited post
     * Should be called after the user clicks Save on the Edit Post page
     * 
     * @param id                The ID of the post
     * @param title             The post's title
     * @param message           The content of the post
     * @param restaurantData    The name of the restaurant with optional City and State
     *                          separated by commas
     * @param rating            The rating the user gave to the restaurant
     */
    public static void saveEditedPost(Long id,
    @Required(message = "Please enter a title for your post") String title,
    @Required(message = "Please enter a message for your post") String message,
    String restaurantData,
    int rating) {

        //Don't do anything unless the user is logged in
        //Todo: Add an error message or 403 response if the user tries to submit
        //an edit of another user's post
        if(Security.isConnected()) {
                User user = User.find("byEmail", Security.connected()).first();
                Post post = Post.findById(id);

             //Make sure there are no errors
            if(validation.hasErrors()) {
                    render("@editPost", post, user);
            }

            post.title = title;
            post.content = message;

            //Search for a restaurant with the given name in the user's city. Create one if it doesn't exist
            if(restaurantData.length() != 0)	{		//Parse data from Restaurant field
                    Restaurant restaurant = find_or_create_restaurant(restaurantData, user.city, user.state);
                    post.restaurant = restaurant;
            }
            
            post.rating = rating;
            post.save();

            UserProfile.index(user.id);
        }
    }

    /**
     * Deletes a post
     * Should be called when the user clicks Delete on their Edit a Post page
     * 
     * @param id The ID of the post
     */
    public static void deletePost(Long id) {
        //Find the post
        Post post = Post.findById(id);
        //Only do something if the user is logged in and the post is theirs
        //Todo: Display an error message or a 403 error if the user tries to delete
        //someone else's post
        if(Security.isConnected()) {
            User user = User.find("byEmail", Security.connected()).first();

            if(user == post.author)
                    post.delete();

            UserProfile.index(user.id);
        }
    }

     /**
     * Render the user's profile picture
     * Should be called when the user's profile picture needs to be displayed in
     * an <img> tag
     * @param id The user's ID
     */
    public static void profilePic(Long id) {
        //Find the user and display an error if the user doesn't exist
        User user = User.findById(id);
        notFoundIfNull(user);

        response.setContentTypeIfNotSet(user.profilePic.type());

        //Display the user's profile picture if they uploaded one
        if(user.profilePic.get() != null){
                renderBinary(user.profilePic.get());
        }

        //If they didn't upload one, display the standard default picture
        else {
            VirtualFile vf = Play.getVirtualFile("/public/images/no-profile-pic.jpg");
            response.setContentTypeIfNotSet("image/jpeg");
            renderBinary(vf.getRealFile());
        }
    }


    /**
     * Checks whether one user is following another
     * 
     * @param whoFollowingId
     * @param whoFollowedId
     * @return true if whoFollowingId is following whoFollowedId, false otherwise
     */
    public static boolean isFollowing(Long whoFollowingId, Long whoFollowedId) {
        return followerRelations.isFollowing(whoFollowingId, whoFollowedId);
    }

    /**
     * Adds a person to the list of people the user is following
     * Should be called after the user clicks "Follow" on another person's profile
     * Currently, this function is only called through an AJAX request, and it will
     * render a JSON response in the format:
     * UserProfileResponse: {
     *  success: whether or not the add was successful
     *  name: username of the person they tried to follow
     * }
     * 
     * If the user tries to access this function without being logged in as the
     * correct user, it currently just renders zero.
     * Todo: change this to an error message or a 403 response
     * 
     * @param whoFollowedId 
     */
    public static void addFollower(Long whoFollowedId) {
        //First, get the user who is following them:
        User whoFollowing;

        //Only do something if the user is logged in
        //Todo Add an error or 403 response if the user tries to access this function
        //while not logged in
        if(Security.isConnected()) {
            String response;
            whoFollowing = User.find("byEmail", Security.connected()).first();
            User whoFollowed = User.findById(whoFollowedId);	//Get user we want to follow

            boolean result = followerRelations.addFollower(whoFollowing, whoFollowed);

            //Construct JSON object to return
            UserProfileResponse jSONresponse = new UserProfileResponse(result, whoFollowed.firstname);
            renderJSON(jSONresponse);
        }
        renderJSON("0");
    }

    /**
     * Remove a person from the list of people a user is following
     * Should be called after the user clicks Unfollow next to the list of people
     * they are following
     * Currently, this function is only called through an AJAX request, and it will
     * render a JSON response in the format:
     * UserProfileResponse: {
     *  success: whether or not the remove was successful
     *  name: username of the person they tried to unfollow
     * }
     * 
     * If the user tries to access this function without being logged in as the
     * correct user, it currently just renders zero.
     * Todo: change this to an error message or a 403 response
     * 
     * @param whoFollowedId 
     */
    public static void removeFollower(Long whoFollowedId) {
    //First, get the user who is following them:
            User whoFollowing;

        //Only do something if the user is logged in
        //Todo Add an error or 403 response if the user tries to access this function
        //while not logged in
        if(Security.isConnected()) {
            whoFollowing = User.find("byEmail", Security.connected()).first();
            User whoFollowed = User.findById(whoFollowedId);	//Get user we want to follow

            boolean result = followerRelations.removeFollower(whoFollowing, whoFollowed);

            //Construct JSON object to return
            UserProfileResponse jSONresponse = new UserProfileResponse(result, whoFollowed.firstname);

            renderJSON(jSONresponse);
        }
        renderJSON("0");
    }

    /**
     * Renders a list of the people the user is following
     * Should be called by the portions of the profile page which list the people
     * a user is following (currently AJAX requests)
     * 
     * Currently, anyone can use this to get a list of people a user is following,
     * since this is displayed on their public profile page.
     * In the future, we'll add some privacy settings so that this only works if
     * they've allowed this
     * 
     * @param id The user's ID
     */
    public static void listFollowing(Long id) {
        User user = User.findById(id);

        //At some point, we'll add code here to check for privacy settings so people can't try to find out another person's followers by
        //accessing this page. For now, just return the list of people the user is following.

        List<User> following = followerRelations.listFollowing(user, SN_LIST_SIZE);
        int sn_list_size = SN_LIST_SIZE;	//Pass on to template
        render(user, following, sn_list_size);
    }

    /**
     * Renders a list of a user's followers
     * Should be called by the parts of the user's profile page which list a user's
     * followers (currently AJAX requests)
     * 
     * Currently, anyone can use this to get a list of people a user is following,
     * since this is displayed on their public profile page.
     * In the future, we'll add some privacy settings so that this only works if
     * they've allowed this
     * 
     * @param id The user's ID
     */
    public static void listFollowers(Long id) {
        User user = User.findById(id);

        List<User> followers = followerRelations.listFollowers(user, SN_LIST_SIZE);

        int sn_list_size = SN_LIST_SIZE;
        render(user, followers, sn_list_size);
    }

    /**
     * Renders a JSON list of all restaurants with a name similar to the input parameter
     * Typically, this used by the jQueryUI Autocomplete feature to assist users
     * in finding restaurants while they're typing them into the Restaurant Name
     * box as they're entering data for a new post
     * 
     * @param term What the user has typed so far
     */
    public static void findRestaurants(String term) {
        //Surround the term with % signs for the MySQL like query
        term = "%" + term + "%";
        List<Restaurant> restaurantList = Restaurant.find("select r from Restaurant r where r.name like ?", term).fetch(10);

        List<String> restaurantInfoList = new ArrayList();

        //Form a list with restaurant name, city, and state to help narrow the scope for the user
        Iterator<Restaurant> li = restaurantList.iterator();
        while(li.hasNext()) {
            Restaurant r = li.next();
            String toAdd = r.name;

            //Only add city and state if they're not null, otherwise autocomplete will show ", null, null"
            if(r.city != null && r.city.length() > 0) {
                    toAdd += ", " + r.city;
                    if(r.state != null && r.state.name != null)
                            toAdd += ", " + r.state.name;
            }

            restaurantInfoList.add(toAdd);
        }

        renderJSON(restaurantInfoList);
    }

    /**
     * Finds a restaurant with the given name, city, and state, or creates one
     * if it does not exist already.
     * 
     * @param restaurantData    The restaurant's name with optional City and State
     *                          separated by commas
     * @param defaultCity       The restaurant's city
     * @param defaultState      The restaurant's state
     * @return                  The restaurant found or created
     */
    private static Restaurant find_or_create_restaurant(String restaurantData, String defaultCity, State defaultState) {
        //Parse data from what the user typed. This allows them to enter, for example, "Per Se New York, NY" all it one line when they enter the restaurant
        Restaurant r = get_restaurant_fields(restaurantData);
        Restaurant restaurant;

        //Search appropriately, based on what criteria we were able to extract
        if(r.city != null && r.state != null)
            restaurant = Restaurant.find("byNameAndCityAndState", r.name, r.city, r.state).first();
        else if(r.city != null)
            restaurant = Restaurant.find("byNameAndCity", r.name, r.city).first();
        else
            restaurant = Restaurant.find("byName", r.name).first();

        //If no restaurant was found, create a new one
        if(restaurant == null) {
            restaurant = new Restaurant(r.name, (r.city == null ? defaultCity : r.city), (r.state == null ? defaultState : r.state));
            restaurant.save();
            flash.success("success");	//Must flash success so the user is prompted to enter data for the restaurant
            flash.put("restaurantID", restaurant.id);
        }

        return restaurant;
    }

    /**
     * Parses a string of the format "Restaurant Name, City, State", where City
     * and State are optional fields, and returns a new Restaurant object with
     * the specified criteria. City and State will be null if not specified
     * 
     * @param restaurantData    The text entered
     * @return                  A new restaurant object corresponding to what was
     *                          entered.
     */
    private static Restaurant get_restaurant_fields(String restaurantData) {
        //The data we will extract
        String restaurantName, restaurantCity, restaurantState;
        //Array we will use to extract the info
        List<String> restaurantInfo = new ArrayList();
        //Restaurant object to return
        Restaurant restaurant;

        //Parse the text by commas
        restaurantName = restaurantCity = restaurantState = null;
        restaurantInfo.addAll(Arrays.asList(restaurantData.split(", ")));

        //As long as there's another component to our data, continue extracting
        Iterator<String> i = restaurantInfo.iterator();
        restaurantName = i.next();

        //Get the city and state from the data entered
        if(i.hasNext())
            restaurantCity = i.next();
        if(i.hasNext())
            restaurantState = i.next();

        restaurant = new Restaurant(restaurantName, restaurantCity, restaurantState);

        return restaurant;
    }

    /**
     * Renders a page listing all a user's posts using the paginator for pagination
     * 
     * @param id The user's ID
     */
    public static void posts(Long id) {
        User user = User.findById(id);
        ModelPaginator paginator = new ModelPaginator(Post.class, "author = ?", user).orderBy("postedAt desc");

        render(user, paginator);
    }

    /**
     * Renders a page listsing all a user's comments, using the paginator for pagination
     * 
     * @param id The user's ID
     */
    public static void comments(Long id) {
        User user = User.findById(id);
        ModelPaginator paginator = new ModelPaginator(Comment.class, "author = ?", user).orderBy("postedAt desc");

        render(user, paginator);
    }

    /**
     * Renders a page listing all the people a user is following, using the
     * paginator for pagination
     * 
     * @param id The user's ID
     */
    public static void friends(Long id) {
        User user = User.findById(id);
        List<User> following = followerRelations.listFollowing(user);
        ValuePaginator paginator = new ValuePaginator(following);

        render(user, paginator);
    }

    /**
     * Renders a page listing all a user's followers, using the paginator for pagination
     * 
     * @param id The user's ID
     */
    public static void followers(Long id) {
        User user = User.findById(id);
        List<User> followers = followerRelations.listFollowers(user);
        ValuePaginator paginator = new ValuePaginator(followers);

        render(user, paginator);
    }
}

