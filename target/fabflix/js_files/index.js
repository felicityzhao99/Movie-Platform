// clear localStorage on Start
localStorage.clear();

let login_form = $("#login_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString) {
    // if the user does not pass reCaptcha verification
    if(resultDataString=='reCaptchaFail'){
        $("#login_error_message").text("Make sure you passed reCAPTCHA first!");
        return;
    }

    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to the main page
    if (resultDataJson["status"] === "success") {
        window.location.replace("/fabflix/movieList.html");
    } else {
        // If login fails, the web page will display error messages
        // console.log(resultDataJson["message"]);
        $("#login_error_message").text(resultDataJson["message"]);
        // reset reCAPTCHA instance after login failure
        grecaptcha.reset();
    }
}

function handleError(resultDataString) {
    console.log(resultDataString);
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    //console.log("submit login form");
    formSubmitEvent.preventDefault();
    $("#login_error_message").text("");
    $.ajax(
        "api/login", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: login_form.serialize(),
            success: handleLoginResult,
            error: handleError
        }
    );
}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);
