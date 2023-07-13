// Send request to the API with async fetch

function token() {
    return document.cookie.split('=')[1];
} 

function setToken(token, expiration) {
    let date = new Date();
    date.setTime(date.getTime() + (expiration * 24 * 60 * 60 * 1000));
    let expires = "expires="+date.toUTCString();
    document.cookie = "token=" + token + ";" + expires + ";path=/";
}

async function request(module, library, action, id = false, method = "POST", params = false)
{
    //console.log("Requesting API: " + module + "/" + library + "/" + action);

    if(params != false) params = JSON.stringify(params);

    // Create cookie 'token' if it doesn't exist with 1 day expiration
    if(token() == undefined) setToken(Math.random().toString(36).substring(7), 1);

    console.log(method)
    let result = await $.ajax(`http://localhost:4080/api/${token()}/${module}/${library}/${action}${id ? '/' + id : ''}`, 
    {
        type: method,
        data : params
    });

    // console.log(result);

    try { result = JSON.parse(result); } catch (e) {}
	return result;
}
