// Send request to the API with async fetch

function request(module, library, action, data) {
    // Fetch on `http://localhost:3000
    return fetch(`http://localhost:3002/`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    });
}