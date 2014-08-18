/*******************************************************************************
 * Restaurants.java
 * 
 * Editing a restaurant's info is handled through the RestaurantProfile controller.
 
 * This file is maintained in order to give admins access to edit restaurants
 * through the admin page, which utilizes the CRUD plugin.
 * 
 */

package controllers;

import play.*;
import play.mvc.*;

//Make sure only admins can access this.
@Check("admin")
@With(Secure.class)
public class Restaurants extends CRUD {
}