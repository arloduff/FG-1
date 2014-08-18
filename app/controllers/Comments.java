/*******************************************************************************
 * Comments.java
 * 
 * Comments class to be implemented. For now, it just ensures that a user must
 * be logged in to post a comment, but we may extend it someday so that users
 * can post comments without being logged in.
 * 
 */

package controllers;

import play.*;
import play.mvc.*;

@Check("admin")
@With(Secure.class)
public class Comments extends CRUD {    
    /* to do */
}