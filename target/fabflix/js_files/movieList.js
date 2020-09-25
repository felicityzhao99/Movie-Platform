// handles the dropdown menu
(function($){
    $('.dropdown-menu a.dropdown-toggle').on('click', function(e) {
        if (!$(this).next().hasClass('show')) {
            $(this).parents('.dropdown-menu').first().find('.show').removeClass("show");
        }
        var $subMenu = $(this).next(".dropdown-menu");
        $subMenu.toggleClass('show');

        $(this).parents('li.nav-item.dropdown.show').on('hidden.bs.dropdown', function(e) {
            $('.dropdown-submenu .show').removeClass("show");
        });

        return false;
    });
})(jQuery)

// handle advanced search dropdown
$(document).ready(function() {
    $(document).click(function(e) {
        if (!$(e.target).is('#advanced_link, #advanced_form, #advanced_form *')) {
            $("#advanced_form").hide();
        }
    });
});

function advancedDropdown(){
    if(document.getElementById("advanced_form").style.display == "none" ||
        document.getElementById("advanced_form").style.display == "")
        document.getElementById("advanced_form").style.display = "block";
    else
        document.getElementById("advanced_form").style.display = "none";
}


// handle search by title
$('#search_form').on('submit', function(e) {
    e.preventDefault();
    e.stopPropagation();
    searchByTitle();
    return false;
});


// handle advanced search
$('#advanced_form').on('submit', function(e) {
    advancedSearch();
    advancedDropdown();
    return false;
});


// should only work if resultData is not empty
function handleMovieResult(resultData) {
    resetTable();
    if(resultData==="empty")                // this means no cache in session
        return;
    if(resultData=="[]") {                  // this means no search result
        document.getElementById("p-holder").innerHTML = "Sorry, your search reached the last page " +
            "or did not find any result.";
        return;
    }
    if(typeof resultData === "string")
        resultData = JSON.parse(resultData);

    saveToSession(resultData);
    // Populate the movie table
    let movieList = jQuery("#movie_list");

    //scroll-to-top
    var myButton = document.getElementById("back_to_top");
    window.onscroll = function() {scrollFunction(myButton)};


    //num_of_results = resultData.length;
    //document.getElementById("result_counts").innerText= num_of_results.toString();
    // check if previous button is available or not
    if (initial_No == 0) $(".previous").hide();
    else $(".previous").show();

    // Check if next button is available or not
    if (resultData.length > num_of_page) {
        $(".next").show();
        resultData.length -= 1;
    }
    else {
        $(".next").hide();
    }

    // Iterate through resultData
    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject

        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + (initial_No+i+1).toString() + "</td>";
        rowHTML += "<td>" +
            // Add a link to single-movie.html
            '<a href="single-movie.html?id=' + resultData[i]["movie_id"] +'">'
            + resultData[i]["movie_title"] + "</a></td>";

        rowHTML += "<td>" + resultData[i]["movie_year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_director"] + "</td>";

        let genres = resultData[i]["movie_genre"].split(",");
        rowHTML += "<td>";
        for(let g=0; g<genres.length;g++) {
            rowHTML += '<a onclick="browseBy('   + "'genre'," + "'" + genres[g] + "');" + '"' +
            ' href="#" >' + genres[g];
            if (g < genres.length - 1)
                rowHTML += ", ";
            rowHTML += "</a>";
        }
        rowHTML += "</td>";

        let actors = resultData[i]["movie_actor"].split(",");
        let actor_id = resultData[i]["actor_id"].split(",");
        rowHTML += "<td>";
        for(let j=0;j<actors.length;j++){
            rowHTML += '<a href="single-actor.html?id=' + actor_id[j] + '">' + actors[j];
            if(j<actors.length-1)
                rowHTML += ", ";
            rowHTML += "</a>";
        }
        let movieId = resultData[i]["movie_id"];

        rowHTML += "</td>";
        rowHTML += "<td>" + resultData[i]["movie_rating"] + "</td>";
        rowHTML += '<td><a class="btn btn-primary" href="javascript:;"' + ' onclick="addCart(' + "'" + movieId + "','true', '1'"
                    + ');">';
        rowHTML += "Add</a></td>";
        rowHTML += "</tr>";
        // Append the row created to the movie table
        movieList.append(rowHTML);
    }
    document.getElementById("table").style.display="block";
}


// save loaded table to session
function saveToSession(data){
    jQuery.ajax({
        dataType: "json",
        method: "POST",
        data: {'table':JSON.stringify(data)},
        url: "api/saveTable"
    });
}


if(window.location.href.includes("movieList.html")) {
    // reload the page with table
    jQuery.ajax({
        dataType: "text",
        method: "GET",
        url: "api/saveTable",
        success: (resultData) => handleMovieResult(resultData)
    });

    // retrieve genre names and set the dropdown menu
    jQuery.ajax({
        dataType: "text",
        method: "GET",
        url: "api/genre",
        success: (resultData) => setGenreMenu(resultData)
    });
}


/*
// Makes the HTTP GET request and on success call function handleMovieResult
function top20() {
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/movies", // Setting request url, which is mapped by MoviesServlet
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle returned data
    });
}*/


// clear userId in the session, and redirect to login page
function logout(){
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/logout"
    });
    window.location.replace("/fabflix/index.html");
}


function setGenreMenu(resultData){
    let genre_menu = jQuery("#genre_menu");
    let alpha_menu = jQuery("#alpha_menu");
    let genres = resultData.split(",");

    for (let i = 0; i < genres.length; i++) {
        let rowHTML = '<li><a class="dropdown-item dropdown_i"' + ' onclick="browseBy('   + "'genre'," + "'" + genres[i] + "');" + '"' +
            ' href="#">';
        rowHTML += genres[i];
        rowHTML += "</a></li>";

        // Append the row created to the genre menu
        genre_menu.append(rowHTML);
    }

    let initials = ['*','0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','I','J','K',
                    'L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'];
    for(let j=0; j<initials.length; j++){
        let rowHTML = '<li><a class="dropdown-item dropdown_i"' + ' onclick="browseBy('   + "'initial'," + "'" + initials[j] + "');" + '"' +
            ' href="#">';
        rowHTML += initials[j];
        rowHTML += "</a></li>";
        // Append the row created to the alphanumerical menu
        alpha_menu.append(rowHTML);
    }
}

function resetTable(){
    document.getElementById("p-holder").innerHTML = "";
    document.getElementById("movie_list").innerHTML = "";

}

// when user clicks on any title initial or movie genre
function browseBy(option,param){
    let url = "api/browse?option=" + option + "&param=" + param;
    saveSearchLink(url);
    getSearchLink();
    initial_No = 0;
    //console.log("browsing by "+option+", "+"param");
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: url,
        error: handleError,
        success: (resultData) => handleMovieResult(resultData)
    });
}

// search a movie by title
function searchByTitle(){
    let movieTitle = document.getElementById("search_input").value;
    let url = "api/search?title=" + movieTitle + "";
    saveSearchLink(url);
    getSearchLink();
    initial_No = 0;

    jQuery.ajax({
        dataType: "text",
        method: "GET",
        url: url,
        success: handleMovieResult,
        error: handleError
    });
}

// advanced search
function advancedSearch() {
    let title = document.getElementById("title").value;
    let year = document.getElementById("year").value;
    let director = document.getElementById("director").value;
    let actor = document.getElementById("actor").value;
    let url = "api/search?title=" + title + "&year=" + year + "&director=" + director + "&actor=" + actor;
    saveSearchLink(url);
    getSearchLink();
    initial_No = 0;

    jQuery.ajax({
        dataType: "text",
        method: "GET",
        url: url,
        success: handleMovieResult,
        error: handleError
    });
}

function handleError(resultDataString) {
    console.log(resultDataString);
}

var sort_btn = [null, null]; //<-- global var
var browse_search_link = "";
getSearchLink();
var num_of_page = 10; //10,20,50,100, default 10
//var num_of_results = 0; //max 9052
var initial_No = 0;

function sortButton(ele) {
    if (sort_btn[0] === null && sort_btn[1] === null) {
        sort_btn[0] = ele;
    }
    else if (sort_btn[0] != null && sort_btn[1] === null) {
        if (parseInt(ele / 10) ==  parseInt(sort_btn[0] / 10)) {
            sort_btn[0] = ele;
        } else if (parseInt(ele / 20) == parseInt(sort_btn[0] / 20)) {
            sort_btn[0] = ele;
        } else{
            sort_btn[1] = ele;
        }
    }
    else {
        if (parseInt(ele / 10) ==  parseInt(sort_btn[1] / 10)) {
            sort_btn[0] = ele;
            sort_btn[1] = null;
        } else if (parseInt(ele / 20) == parseInt(sort_btn[1] / 20)) {
            sort_btn[0] = ele;
            sort_btn[1] = null;
        } else{
            sort_btn[0] = ele;
        }
    }
    sortButtonHelper(num_of_page);

}

function sortButtonHelper(page) {

    let sort1 = "";
    let sort2 = "";
    num_of_page = page;
    if (sort_btn[0] == null)
        sort1 = sort_btn[0];
    else
        sort1 = (sort_btn[0]).toString();
    if (sort_btn[1] == null)
        sort2 = sort_btn[1];
    else
        sort2 = (sort_btn[1]).toString();
    let url = browse_search_link + "&sort1=" + sort1  + "&sort2=" + sort2
        + "&limit=" + (num_of_page).toString() + "&offset=" + (initial_No).toString();
    jQuery.ajax({
        dataType: "text",
        method: "GET",
        url: url,
        success: handleMovieResult,
        error: handleError
    });
}


function scrollFunction(myButton) {
    if (document.body.scrollTop > 20 || document.documentElement.scrollTop > 20) {
        myButton.style.display = "block";
    } else {
        myButton.style.display = "none";
    }
}

function topFunction() {
    document.body.scrollTop = 0;
    document.documentElement.scrollTop = 0;
}

function previousBtn() {
    if (initial_No != 0) initial_No -= num_of_page;
    sortButtonHelper(num_of_page);
}

function nextBtn() {
    //var max_page = Math.ceil(num_of_results/num_of_page);
    //if (initial_No < max_page) initial_No += num_of_page;
    initial_No += num_of_page;
    sortButtonHelper(num_of_page);
}

function toShoppingCart(){
    document.getElementById("movie_page").style.display = "none";
    document.getElementById("shopping_cart").style.display = "block";
}

let url = new URL(window.location.href);
if(url.searchParams.get("location")=="shopping_cart"){
    toShoppingCart();
}

// saves search link to session
function saveSearchLink(searchLink){
    jQuery.ajax({
        dataType: "json",
        method: "POST",
        data: {'searchLink':JSON.stringify(searchLink)},
        url: "api/searchLink"
    });
}

function getSearchLink(){
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/searchLink",
        success: (resultData) => updateLink(resultData)
    });
}

function updateLink(resultData){
    browse_search_link = resultData;
}

// add movie to shopping cart
function addCart(movieId,add,quantity){
    // if add == 'true' then add one more to cart
    // otherwise update movie # to quantity
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/shoppingCart?id="+movieId+"&quantity="+quantity+"&add="+add,
        success: showNotification,
        fail: showError
    });
}

function showNotification(){
    let x = document.getElementById("snackbar");
    x.innerHTML = 'Shopping cart has been updated!';
    x.className = "show";
    setTimeout(function(){ x.className = x.className.replace("show", ""); }, 3000);
}

function showError(){
    let x = document.getElementById("snackbar");
    x.innerHTML = "Fail to add. Please try again.";
    x.className = "show";
    setTimeout(function(){ x.className = x.className.replace("show", ""); }, 3000);
}



if(localStorage.getItem("autocomplete_cache")==null) {
    const initial_cache = {};
    localStorage.setItem("autocomplete_cache", JSON.stringify(initial_cache));
}

function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated");
    const auto_cache =  JSON.parse(localStorage.getItem("autocomplete_cache"));

    // check if cache has suggestions for this query
    if(auto_cache[query]) {
        console.log("autocomplete search is using cached results");
        handleLookupAjaxSuccess(auto_cache[query], query, doneCallback);
        return;
    }
    // if not, send request to server
    console.log("autocomplete search is sending AJAX request to the server");
    jQuery.ajax({
        "method": "GET",
        "url": "autocomplete?query=" + escape(query),
        "success": function(data) {
            // pass the data, query, and doneCallback function into the success handler
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        "error": function(errorData) {
            console.log("lookup ajax error");
            console.log(errorData);
        }
    })
}


function handleLookupAjaxSuccess(data, query, doneCallback) {
    let auto_cache =  JSON.parse(localStorage.getItem("autocomplete_cache"));
    auto_cache[query] = data;

    // cache the result into localStorage
    localStorage.setItem("autocomplete_cache", JSON.stringify(auto_cache));

    doneCallback( { suggestions: JSON.parse(data)} );
}


// redirect to the suggested movie page
function handleSelectSuggestion(suggestion) {
    window.location.replace("/fabflix/single-movie.html?id="+suggestion["data"]["movieId"]);
}


$('#search_input').autocomplete({
    // set delay time
    deferRequestBy: 300,
    minChars: 3,

    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
});


// bind pressing enter key to a handler function
$('#search_input').keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode == 13) {
        searchByTitle();
    }
})
