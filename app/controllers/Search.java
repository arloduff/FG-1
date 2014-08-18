/*******************************************************************************
 * Search.java
 * 
 * Contains logic for search functions, such as searching for restaurants by city
 * or by restaurant name, suggesting solutions for misspellings, and so on.
 *
 ******************************************************************************/

package controllers;

import play.*;
import play.mvc.*;
import play.data.validation.*;
import play.db.jpa.Blob;
import play.db.jpa.Model;
import play.jpa.*;
import play.modules.paginate.*;
import play.modules.paginate.ModelPaginator.*;

import java.util.*;

import models.*;


public class Search extends Controller {
  @Before

    /**
    * Sets the connected user prior to doing anything else
    */
    static void setConnectedUser() {
        if(Security.isConnected()) {
          User user = User.find("byEmail", Security.connected()).first();
          renderArgs.put("loggedInAs", user);
        }
    }

  /**
   * Main search page before the user entered anything. Displays all cities to choose from.
   */
    public static void index() {
        int byCity = BY_CITY;  //Non-static variables must be used to pass values to templates
        int byName = BY_NAME;
        List<String> cities = Restaurant.find("select distinct r.city from Restaurant r where r.city is not null").fetch();

        render(cities, byCity, byName);
    }

    /**
     * Searches for a restaurant by restaurant name, or by city.
     * Should be called by the search form after the user enters search criteria.
     * @param input
     * @param search_type 
     */
    public static void search(@Required String input, int search_type) {
        if(validation.hasErrors()) {    //Go back to the index page if there was an error.
            List<String> cities = Restaurant.find("select distinct not null r.city from Restaurant r").fetch();
            render("@index", cities);
        }

        //else
        if(search_type == BY_CITY) { //If searching by city
            byCity(input);  //Calls render by itself
        }

        else {
            byRestaurantName(input);    //Calls render by itself
        }
    }

    //Search functions with page indicated

    /**
     * Lists all restaurants in a given city
     * This function calls render on its own. This way, it can offer suggestions if there are misspellings.
     * @param city The city name
     */
    public static void byCity(String city) {
        List<Restaurant> restaurants = Restaurant.find("byCity", city).fetch();
        int results_size = restaurants.size();

        ModelPaginator paginator = new ModelPaginator(Restaurant.class, "city like '%" + city + "%'"); //Inline way of getting name in %like% operator

        //Check for city misspellings
        Restaurant misspelled_city_restaurant = Restaurant.find("byCityLike", city.substring(0,3) + '%').first();
        String misspelled_city = misspelled_city_restaurant != null ? misspelled_city_restaurant.city : null;

        render(city, paginator, results_size, misspelled_city);
    }

    /**
     * Searches for a restaurant by name
     * This function calls render on its own. This way, it can offer suggestions if there are misspellings.
     * @param name The restaurant name
     */
    public static void byRestaurantName(String name) {
        List<Restaurant> restaurants = Restaurant.find("byNameLike", name).fetch();
        int results_size = restaurants.size();

        ModelPaginator paginator = new ModelPaginator(Restaurant.class, "name like '%" + name + "%'"); //Inline way of getting name in like

        //Check for city misspellings
        Restaurant misspelled_restaurant = Restaurant.find("byNameLike", name.substring(0,3) + '%').first();
        String misspelled_restaurant_name = misspelled_restaurant != null ? misspelled_restaurant.name : null;

        render(paginator, name, results_size, misspelled_restaurant_name);
    }

    /**
     * Lists restaurants with a similar name to the one typed. Used for the "Did you mean" suggestion on search results.
     * Todo: Right now, we're only using the MySQL like comparator. We should use a more advanced algorithm to check for typos.
     * @param name The name of the restaurant.
     * @return 
     */
    public static List<String> findSimilarName(String name) {
            List<String> restaurantList = new ArrayList();	//List to store similar restaurants found
            String firstThree = name.substring(0, 3);	//First three characters

            //Get the restaurants that match the first three characters
            List<String> matchesFirstThree = Restaurant.find("select r.name from Restaurant r where r.name like ?", firstThree + "%").fetch();

            return matchesFirstThree;
    }
    
    public static int BY_CITY = 1;
    public static int BY_NAME = 2;
}
