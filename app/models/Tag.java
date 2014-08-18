/*******************************************************************************
 * Tag.java
 * 
 * The model representation of tags. As on forum boards, tags are strings of text
 * which indicate subjects with which a specific post is associated.
 * 
 ******************************************************************************/

package models;
 
import java.util.*;
import javax.persistence.*;
 
import play.db.jpa.*;
import play.data.validation.*;
 
@Entity
public class Tag extends Model implements Comparable<Tag> {
 
    @Required
    public String name;             //The tag
    
    /**
     * Constructor taking the tag name
     * 
     * @param name 
     */
    private Tag(String name) {
        this.name = name;
    }
    
    /**
     * Finds a tag with the given name, or creates one if it doesn't exist
     * 
     * @param name  The tag to search for
     * @return      A tag with the specified name
     */
    public static Tag findOrCreateByName(String name) {
        Tag tag = Tag.find("byName", name).first();
        if(tag == null) {
            tag = new Tag(name);
        }
        return tag;
    }
    
    /**
     * Returns a map of all tags and the posts they correspond to, grouped by name
     * 
     * @return A map of all tags
     */
    public static List<Map> getCloud() {
        List<Map> result = Tag.find(
            "select new map(t.name as tag, count(p.id) as pound) from Post p join p.tags as t group by t.name"
        ).fetch();
        return result;
    }
    
    /**
     * Returns the name of the tag
     * 
     * @return The tag name
     */
    public String toString() {
        return name;
    }
    
    /**
     * Compares one tag to another
     * 
     * @param   otherTag  Tag object of the tag to compare this to
     * @return  The result of running String.compareTo on the tag names
     */
    public int compareTo(Tag otherTag) {
        return name.compareTo(otherTag.name);
    }
 
}