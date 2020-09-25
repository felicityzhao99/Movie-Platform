var movie_ids_list = [];
var final_price = 0;

jQuery.ajax({
    dataType: "text",
    method: "POST",
    url: "api/shoppingCart",
    success: handleFinalCartResult,
    error: handleError
});

function handleError(resultDataString) {
    console.log(resultDataString);
}

function handleFinalCartResult(resultData) {
    if(resultData=="empty" || resultData=="{}") {
        return;
    }
    json = JSON.parse(resultData);
    for (let key in json) {
        if (json.hasOwnProperty(key))
            movie_ids_list.push(key);
    }
    movie_ids_list.sort();
    getFinalMoviesTitle(movie_ids_list);
}

function getFinalMoviesTitle(list){
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
        success: handleFinalCartResult2,
        error: handleError
    });
}

function handleFinalCartResult2(resultData) {
    let resultList = jQuery("#result_list");
    let titles = resultData.split(",");
    let price = 10.99;
    let saleId = 13561;//New SaleID begins at 13561

    for (let i = 0; i < titles.length; i++) {
        let title = titles[i];
        let qty = json[movie_ids_list[i]];
        let saleid = (saleId + i).toString();
        final_price += parseInt(qty) * price;

        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + saleid + "</td>";
        rowHTML += "<td>" + title + "</td>";
        rowHTML += "<td>" + qty + "</td>";
        rowHTML += "</tr>";
        resultList.append(rowHTML);
    }
    document.getElementById("total_price").innerText = (final_price).toString();

}
