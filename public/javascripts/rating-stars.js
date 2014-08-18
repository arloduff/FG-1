/* rating-stars.js

Contains functions which highlight the rating stars when the page is loaded, and when a user selects a different number of stars
*/

$(document).ready(function() {
		//Highlight the correct stars for whichever rating is selected
		for(i = 1;i <= 5;i++) {
			var button = document.getElementById("rating_" + i);
			if(button.checked)
				highlightStar(i);
		}
});


function highlightStar(value) {
	var starNum = document.getElementById("rating");
	//Highlight the star and all the ones before it
	for(i = value; i > 0; i--) {
		starNum.children[i].style.backgroundImage = "url(/public/images/star-highlighted.gif)";
	}
	//Dim the stars after it
	for(i = 5; i > value; i--) {
		starNum.children[i].style.backgroundImage = "url(/public/images/star-dim.gif)";
	}
}
