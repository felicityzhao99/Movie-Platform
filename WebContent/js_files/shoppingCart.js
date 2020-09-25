let movieIds = [];  // stores all movieId, sorted
let json = "";      // json object storing id and qty
let total_price = 0;

// retrieve shopping cart from session
jQuery.ajax({
    dataType: "text",
    method: "POST",
    url: "api/shoppingCart",
    success: handleShoppingResult,
    error: handleError
});

/*
function getMovieIDs() {
    console.log(movieIds);
    return movieIds;
}*/

function handleShoppingResult(resultData) {
    if(resultData=="empty" || resultData=="{}") {
        document.getElementById("p1").style.display = "block";
        return;
    }
    document.getElementById("p1").style.display = "none";
    json = JSON.parse(resultData);
    for (let key in json) {
        if (json.hasOwnProperty(key))
            movieIds.push(key);
    }
    movieIds.sort();
    getMovieTitles(movieIds);
}

// retrieve movie titles by id
function getMovieTitles(list){
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
        success: handleShopping2,
        error: handleError
    });
}


function handleShopping2(resultData){
    let shoppingList = jQuery("#shopping_list");
    let titles = resultData.split(",");
    let price = 10.99;

    for(let i=0; i<titles.length; i++){
        let title = titles[i];
        let qty = json[movieIds[i]];
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + title + "</td>";

        //Quantity
        rowHTML += "<td>" + "<input id='" + movieIds[i] + "qty'" +
            " type='number' name='quantity' min='1' value='" + qty + "'>" +
            "</td>";
        total_price += price * parseInt(qty);
        //Price
        rowHTML += "<td>" + '$' + price + "</td>";

        //Delete
        rowHTML += "<td><input id='" + movieIds[i] +
            "' class='delbtn' type='button' value='Delete' onclick='deleteFunction(this)'></td>";
        rowHTML += "</tr>";
        shoppingList.append(rowHTML);
    }

    document.getElementById("cart_table").style.display="block";
}


function handleError(resultData) {
    console.log(resultData);
}

function deleteFunction(o) {
    var p=o.parentNode.parentNode;
    p.parentNode.removeChild(p);

    let url = "api/updateCart?option=delete&id=" + o.getAttribute("id");
    jQuery.ajax({
        dataType: "text",
        method: "GET",
        url: url,
        success: showNotification,
        error: showError
    });
}


function updateCart(){
    let items = document.getElementsByClassName("delbtn");
    for(let i=0;i<items.length; i++){
        updateQuantity(items[i]);
    }
}


function updateQuantity(o){
    let id = o.getAttribute("id");
    let qty = document.getElementById(id+"qty").value;
    let url = "api/updateCart?option=update&id=" + id + "&quantity=" + qty;
    jQuery.ajax({
        dataType: "text",
        method: "GET",
        url: url,
        success: showNotification,
        error: handleError
    });
}


// Makes the HTTP GET request and registers on success callback function handleSingleActor
function saveToSession(data){
    jQuery.ajax({
        dataType: "json",
        method: "POST",
        data: {'table':JSON.stringify(data)},
        url: "api/saveTable"
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

function payment_nav() {
    if (document.getElementById("p1").style.display == "none") {
        window.location.replace("/fabflix/payment.html");
    }
}
