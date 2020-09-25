let payment_form = $("#payment_form");
var first_name = "";
var last_name = "";
var credit_card = "";
var month = "";
var year = "";
var movie_ids = [];
var total_prices = 0;

jQuery.ajax({
    dataType: "text",
    method: "POST",
    url: "api/shoppingCart",
    success: handleCartResult,
    error: handleError
});


function handleUserInfoResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    first_name = document.getElementById("first_name").value;
    last_name = document.getElementById("last_name").value;
    credit_card = document.getElementById("credit_card").value;
    month = document.getElementById("month_select").value;
    year = document.getElementById("year_select").value;

    console.log("handle user info response");
    console.log(resultDataJson["status"]);

    //If user info matches, it will direct to placeOrder page (confirmation page)
    //And also record info to the sales table later in the placeOrder page
    if (resultDataJson["status"] === "success") {
        window.location.replace("/fabflix/placeOrder.html");

    }
    //If user info doesn't match, it will return back to the payment page with error info
    else {
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#user_error_message").text(resultDataJson["message"]);
    }
}

function handleError(resultDataString) {
    console.log(resultDataString);
}

function submitUserForm(formSubmitEvent) {
    console.log("submit user form");
    formSubmitEvent.preventDefault();

    jQuery.ajax({
        url: "api/pay",
        method: "POST",
        data: payment_form.serialize(),
        success: handleUserInfoResult,
        error: handleError
    });
}

payment_form.submit(submitUserForm);

function handleCartResult(resultData) {
    if(resultData=="empty" || resultData=="{}") {
        return;
    }
    json = JSON.parse(resultData);
    for (let key in json) {
        if (json.hasOwnProperty(key))
            movie_ids.push(key);
    }
    movie_ids.sort();
    getMoviesTitle(movie_ids);
}

function getMoviesTitle(list){
    let url = "api/getTitles?id=(";
    for(let i=0; i<list.length; i++){
        url += "'" + list[i] + "'";
        if(i!=list.length-1)
            url += ",";
    }
    url += ")";
    jQuery.ajax({
        dataType: "text",
        method: "GET",
        url: url,
        success: handleCartResult2,
        error: handleError
    });
}

function handleCartResult2(resultData) {
    let titles = resultData.split(",");
    let price = 10.99;

    for(let i=0; i<titles.length; i++) {
        let title = titles[i];
        let qty = json[movie_ids[i]];
        total_prices += parseInt(qty) * price;
    }
    document.getElementById("myPrice").innerText = (total_prices).toString();
}
