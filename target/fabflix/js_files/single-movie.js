var url2 = new URL(window.location.href);
var movie_id = url2.searchParams.get("id");

function handleSingleMovie(resultData){
    // in case no movie data returned
    if(resultData.length==0){
        window.location.replace('movieList.html');
        return;
    }
    let titleField = jQuery("#sm_title");
    let yearField = jQuery("#sm_year");
    let directorField = jQuery("#sm_director");
    let genresField = jQuery("#sm_genres");
    let starsField = jQuery("#sm_stars");
    let ratingField = jQuery("#sm_rating");

    // update the star information
    titleField.append(resultData[0]["title"]);
    yearField.append(resultData[0]["year"]);
    directorField.append(resultData[0]["director"]);

    //genresField.append(resultData[0]["genres"]);
    let genres = resultData[0]["genres"].split(",");
    for (let g=0; g<genres.length; g++) {
        let temp1 = '';
        temp1 += '<a onclick="browseBy2(' + "'genre','" + genres[g] + "');" + '"' +
            ' href="javascript:;" >' + genres[g];
        if (g < genres.length - 1)
            temp1 += ', ';
        temp1 += '</a>';
        genresField.append(temp1);
    }

    let stars = resultData[0]["stars"].split(",");
    let stars_id = resultData[0]["starId"].split(",");
    ratingField.append(resultData[0]["rating"]);


    for(let i=0;i<stars.length;i++){
        let temp2 = "";
        temp2 += '<a href="single-actor.html?id=' + stars_id[i] + '">' + stars[i];
        if(i<stars.length-1)
            temp2 += ", ";
        temp2 += "</a>";
        starsField.append(temp2);
    }

    document.getElementById("add_to_cart_button").innerHTML = '<a class="nav-link" href="javascript:;"'
        + ' onclick="addCart(' + "'" + movie_id + "','true', '1'" + ');">Add to Cart</a>';
}


// Makes the HTTP GET request and registers on success callback function handleSingleActor
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movie", // Setting request url, which is mapped by ActorServlet
    data: {
        id: movie_id
    },
    success: (resultData) => handleSingleMovie(resultData) // Setting callback function to handle returned data
});


function browseBy2(option,param){
    let url = "api/browse?option=" + option + "&param=" + param;
    saveSearchLink(url);
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: url,
        error: handleError,
        success: (resultData) => handleMovieResult2(resultData)
    });
}

function handleMovieResult2(resultData) {
    saveToSession(resultData);
    window.location.href = 'movieList.html';
}