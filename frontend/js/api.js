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

    if(params != false) params = JSON.stringify(params);
    if(token() == undefined) setToken(Math.random().toString(36).substring(7), 1);
    let response = false;
    try {
        let result = $.ajax(`http://localhost:4090/api/${token()}/${module}/${library}/${action}${id ? '/' + id : ''}`, 
        {
            type: method,
            data : params
        });
        await result;

        response = JSON.parse(result.responseText);
        response.status = result.status;
    } catch (error) {
        response = JSON.parse(error.responseText);
        response.status = error.status;
    }
    
    return response;
}


async function fullRequest(module, library, action, id = false, method = "POST", params = false)
{
    
    if(params != false) params = JSON.stringify(params);
    if(token() == undefined) setToken(Math.random().toString(36).substring(7), 1);
    let result = $.ajax(`http://localhost:4090/api/${token()}/${module}/${library}/${action}${id ? '/' + id : ''}`, 
    {
        type: method,
        data : params
    });
    await result;
    return result;
}

function getError(response, errorName) {
    if(!response.success) {
        return response.errors.find(error => error.error == errorName);
    }
    return false;
}