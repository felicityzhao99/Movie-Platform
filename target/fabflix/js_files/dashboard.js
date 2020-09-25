let login_form = $("#login_form2");
let star_form = $("#starform");
let movie_form = $("#movieform");
// once onload, check if employee has logged in
checkLoggedIn();

const attributes = {
    customers: "id<br>\n" + "firstName<br>\n" + "lastName<br>\n" + "ccId<br>\n" + "address<br>\n" + "email<br>\n" + "password",
    creditcards: "id<br>\n" + "firstName<br>\n" + "lastName<br>\n" + "expiration",
    employees: "email<br>\n" + "password<br>\n" + "fullname",
    genres: "id<br>\n" + "name",
    genres_in_movies: "genreId<br>\n" + "movieId",
    movies: "id<br>\n" + "title<br>" + "year<br>" + "director",
    ratings: "movieId<br>rating<br>numVotes",
    sales: "id<br>customerId<br>movieId<br>saleDate",
    stars: "id<br>name<br>birthYear",
    stars_in_movies: "starId<br>movieId"
}

const datatype = {
    customers: "int<br>\n" + "varchar<br>\n" + "varchar<br>\n" + "varchar<br>\n" + "varchar<br>\n" + "varchar<br>\n" + "varchar",
    creditcards: "varchar<br>\n" + "varchar<br>\n" + "varchar<br>\n" + "date",
    employees: "varchar<br>\n" + "varchar<br>\n" + "varchar",
    genres: "int<br>\n" + "varchar",
    genres_in_movies: "int<br>\n" + "varchar",
    movies: "varchar<br>varchar<br>int<br>varchar",
    ratings: "varchar<br>float<br>int",
    sales: "int<br>int<br>varchar<br>date",
    stars: "varchar<br>varchar<br>int",
    stars_in_movies: "varchar<br>varchar"
}


const selector = document.getElementById("table_name");
selector.addEventListener("change", function () {
    showTable(selector.value);

});

function showTable(tableName){
    document.getElementById("table_attribute").innerHTML = attributes[tableName];
    document.getElementById("table_data").innerHTML = datatype[tableName];
}

function checkLoggedIn(){
    // console.log("checking session");
    $.ajax(
        "api/employee_login", {
            method: "GET",
            success: handleCheck,
            error: handleError
        }
    );
}

function handleCheck(result) {
    // console.log(result);
    if(result=="yes")
        showDashboard();
}

function showDashboard() {
    document.getElementById("footer").style.display = "none";
    document.getElementById("c1").style.display = "none";
    document.getElementById("warning").style.display = "none";
    document.getElementById("logout_btn").style.display = "block";
    document.getElementById("panel").style.display = "block";
}

function handleError(resultDataString) {
    console.log(resultDataString);
}

function handleLoginResult(resultDataString) {
    // console.log(resultDataString);
    let resultDataJson = JSON.parse(resultDataString);

    // If login succeeds, it will display the actual dashboard to employee
    if (resultDataJson["status"] === "success") {
        showDashboard();
    } else {
        // If login fails, the web page will display error messages
        // console.log(resultDataJson["message"]);
        $("#login_error_message").text(resultDataJson["message"]);
    }
}

function submitLoginForm(formSubmitEvent) {
    // console.log("submit login form");
    formSubmitEvent.preventDefault();
    $("#login_error_message").text("");
    $.ajax(
        "api/employee_login", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: login_form.serialize(),
            success: handleLoginResult,
            error: handleError
        }
    );
}


function handleStarResult(resultDataString) {
    $("#login_error_message2").text(resultDataString);
}

function handleMovieResult(resultDataString) {
    document.getElementById('login_error_message4').innerHTML = resultDataString;
    // $("#login_error_message4").text(resultDataString);
}

function submitStarForm(formSubmitEvent) {
    // console.log("submit star form");
    formSubmitEvent.preventDefault();
    $("#login_error_message2").text("");
    $.ajax(
        "api/insert_star", {
            method: "POST",
            data: star_form.serialize(),
            success: handleStarResult,
            error: handleStarResult
        }
    );
}

function submitMovieForm(formSubmitEvent) {
    // console.log("submit movie form");
    formSubmitEvent.preventDefault();
    $("#login_error_message4").text("");
    $.ajax(
        "api/insert_movie", {
            method: "POST",
            data: movie_form.serialize(),
            success: handleMovieResult,
            error: handleMovieResult
        }
    );
}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);
star_form.submit(submitStarForm);
movie_form.submit(submitMovieForm);

function logout(){
    jQuery.ajax({
        dataType: "text",
        method: "GET",
        url: "api/employee_logout",
    });
    window.location.replace("/fabflix/_dashboard.html");
}

function showForm(form) {
    document.getElementById("c3").style.display = "none";
    document.getElementById("c2").style.display = "none";
    document.getElementById("c4").style.display = "none";
    document.getElementById(form).style.display = "block";
}

