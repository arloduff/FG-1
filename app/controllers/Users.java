/******************************************************************************
 * Users.java
 * 
 * Allows access to the admin user panel to delete users
 * 
 ******************************************************************************/

package controllers;

import play.*;
import play.mvc.*;

//Require admin access to use any of these features
@Check("admin")
@With(Secure.class)
public class Users extends CRUD {
}
