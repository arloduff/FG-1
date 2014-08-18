package models;

import java.util.*;
import javax.persistence.*;

import play.data.binding.*;
import play.data.validation.*;
import play.db.jpa.*;
import play.db.jpa.Blob;


@Entity
public class Restaurant extends Model implements Comparable<Restaurant> {

    @Required
	public String name;

	public String country;
	public String street1;
	public String street2;
			@Required
	public String city;

	@ManyToOne
	public State state;

	public String zipcode;
	public String phone;
	@URL
	public String website;

	public String cuisine;

	public String cost;
	public String aboutUs;
	public int rating;

	public Blob profilepic;

	@OneToMany(mappedBy="restaurant", cascade=CascadeType.ALL)
	public List<Post> reviews;

	@ManyToOne
	public User owner;

	//constructor
	public Restaurant(String name, String city) {
		this.name = name;
		this.city = city;
	}

	public Restaurant(String name, String city, String state) {
		this.name = name;
		this.city = city;
		this.state = State.findOrCreateByName(state);
	}

	public Restaurant(String name, String city, State state) {
		this.name = name;
		this.city = city;
		this.state = state;
	}

	public Restaurant(String name, String street1, String street2, String city, String state, String zipcode, String phone, String website, String cuisine, String cost, String about, Blob pic) {
		this.name = name;
		this.street1 = street1;
		this.street2 = street2;
		this.city = city;
		this.state = State.findOrCreateByName(state);
		this.zipcode = zipcode;
		this.phone = phone;
		this.website = website;
		this.cuisine = cuisine;
		this.cost = cost;
		this.aboutUs = about;
		this.profilepic = pic;
	}

	public Restaurant(String name, String street1, String street2, String city, String state, String zipcode, String phone, String website, String cuisine, String cost, int rating, Blob profilepic){
		this.name = name;
		this.reviews = new ArrayList<Post>();
		this.country= country;
		this.street1= street1;
		this.street2= street2;
		this.city= city;
		this.state = State.findOrCreateByName(state);
		this.zipcode= zipcode;
		this.website= website;
		this.cuisine= cuisine;
		this.cost= cost;
		this.rating= rating;  //questionable if needed.  we might take how many likes in a post.
		this.profilepic = profilepic;
	}

	public Restaurant addReview(Post review) {
	    this.reviews.add(review);
	    this.save();
	    return this;
	}

	public static List<Post> findPosts(long restid) {

		//must rethink this one
		return Post.find(
		   "select distinct postid from restaurant where id = ? order by rating desc"
		).fetch();
	}

	public static Restaurant findOrCreateByName(String name, String city) {
		Restaurant restaurant = Restaurant.find("select r from Restaurant r where r.name = ?1 and r.city = ?2", name, city).first();
		if(restaurant == null) {
			restaurant = new Restaurant(name, city);
			restaurant.save();
		}
		return restaurant;
	}

	public int compareTo(Restaurant otherRestaurant) {
		return name.compareTo(otherRestaurant.name);
	}

	public String toString() {
		return this.name;
	}
        
        public static List<String> topTenCities() {
        List<String> topTen = Restaurant.find("select distinct r.city from Restaurant r where r.reviews.size > 0 order by r.city").fetch();
        return topTen;
}

}
