//Call showRestaurant as soon as the page is loaded to determine whether or not the restaurant info field should be displayed.
	$(document).ready(function() {
		showRestaurant();
	}

//Determines whether the Restaurant name has text, and displays RestaurantInfo field if it does, hides it otherwise.
	function showRestaurant() {
		var infoField = document.getElementById('restaurant_info');
		var nameField = document.getElementById('restaurant_name');
		if(nameField.value != '') {
			infoField.style.display = 'block';
		}
		else {
			infoField.style.display = 'none';
		}
	}

