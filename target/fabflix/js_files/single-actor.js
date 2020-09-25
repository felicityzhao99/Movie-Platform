var url = new URL(window.location.href);
var actor_id = url.searchParams.get("id");

function handleSingleActor(resultData){
    let nameField = jQuery("#star_name");
    let birthYearField = jQuery("#year_of_birth");
    let moviesField = jQuery("#movies_acted");

    // update the star information
    nameField.append(resultData[0]["name"]);
    birthYearField.append(resultData[0]["birthYear"]);

    let movies = resultData[0]["movies"].split(",");
    let movie_id = resultData[0]["movie_id"].split(",");
    let temp = "";
    for(let i=0;i<movies.length;i++){
        temp += '<a href="single-movie.html?id=' + movie_id[i] + '">' + movies[i];
        if(i<movies.length-1)
            temp += ", ";
        temp += "</a>";
    }
    moviesField.append(temp);
}

// Makes the HTTP GET request and registers on success callback function handleSingleActor
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/actor", // Setting request url, which is mapped by ActorServlet
    data: {
        id: actor_id
    },
    success: (resultData) => handleSingleActor(resultData) // Setting callback function to handle returned data
});