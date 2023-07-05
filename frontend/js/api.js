// Send request to the API with async fetch

async function request(module, library, action, params = false)
{
    //console.log("Requesting API: " + module + "/" + library + "/" + action);

    if(params != false) params = JSON.stringify(params);
    // console.log(params);

    let result = await $.ajax(`http://localhost:4080/api/${module}/${library}/${action}`, 
    {
        type: 'POST',
        data : params
    });

    // console.log(result);

    try { result = JSON.parse(result); } catch (e) {}
	return result;
}
