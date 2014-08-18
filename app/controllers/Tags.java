/*******************************************************************************
 * Tags.java
 * 
 * Right now, tags are created by the ~~~~ controller when a user adds tags to
 * a post.
 * 
 * This controller is maintained so admins can edit or delete tags if necessary
 * through the admin control panel, which is built on the CRUD plugin.
 * 
 ******************************************************************************/

package controllers;

import play.*;
import play.mvc.*;

//Require adminship to edit the tags
@Check("admin")
@With(Secure.class)
public class Tags extends CRUD {    
}