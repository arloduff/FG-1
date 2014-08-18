/*******************************************************************************
 * Posts.java
 * 
 * Right now, user's editing of posts is done through the Userprofile controller,
 * since that's the only page on which users can create or edit posts.
 * 
 * This file enables admins to edit posts through the admin panel, which utilizes
 * the CRUD plugin. Right now, all this needs to do is deliver the post's content
 * to the edit form.
 * 
 ******************************************************************************/

package controllers;

import play.*;
import play.mvc.*;
import models.*;
import java.util.*;

//You should only be able to write or edit a post if you're logged in.
@Check("admin")
@With(Secure.class)
public class Posts extends CRUD {


    /**
     * Delivers a post's content to the edit form.
     * @param id The ID of the post to edit.
     */
    public static void form(Long id) {
      if(id != null) {
          Post post = Post.findById(id);
          render(post);
      }
      render();
  }

}
