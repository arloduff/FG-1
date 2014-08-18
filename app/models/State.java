/*******************************************************************************
 * State.java
 * 
 * Model for representing U.S. states
 * 
 * Unlike cities, states are represented by their own model. Because there are
 * numerous cities, some with the same name in multiple states, city names are
 * currently stored as strings in the User and Restaurant objects, and as such,
 * are not prone to any error checking. As the application expands, we may want
 * to create a separate City object to be mapped as a @ManyToOne relationship within
 * the State class.
 * 
 ******************************************************************************/

package models;

import java.util.*;
import javax.persistence.*;

import play.data.binding.*;
import play.data.validation.*;
import play.db.jpa.Model;

@Entity
public class State extends Model implements Comparable<State> {
    @Required
    public String name;                 //The name of the state. In this case, we
                                        //only want the abbreviation, such as "CA"
    /**
     * Constructor for a new state object, taking only its name
     * 
     * @param name  The abbreviation of the state
     */
    public State(String name) {
        this.name = name;
    }

    /**
     * Lists all states which currently exist in our database.
     * To list all possible states, use the listAllPossibleStates() function.
     * 
     * @return      A list of all states which have been created
     */
    public static List<State> listStates() {
        List<State> states = State.find("select s from State s").fetch();
        return states;
    }

    /**
     * Returns a state with the given name if it exists, creates and returns one
     * otherwise.
     * 
     * @param name  The name of the state
     * @return      The state object with the given name
     */
    public static State findOrCreateByName(String name) {
        State state = State.find("byName", name).first();
        if(state == null) {
            state = new State(name);
            state.save();
        }
        return state;
    }

    /**
     * Runs the String.compareTo method on two states' names
     * 
     * @param other_state   The state to compare it to
     * @return              The result of the comparison
     */
    public int compareTo(State other_state) {
        return name.compareTo(other_state.name);
    }

    /**
     * Returns the state's name
     * 
     * @return  The state's name
     */
    public String toString() {
        return this.name;
    }
}
