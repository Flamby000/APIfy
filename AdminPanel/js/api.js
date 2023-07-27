// Send request to the API with async fetch
class APIManager {
    static apiPath() {
        return 'http://localhost:4090/api';
    }

    static token() {
        // Get token from cookie
        try {
            return document.cookie.split('; ').find(row => row.startsWith('token')).split('=')[1];
        } catch (error) {
            APIManager.setToken(1);
            return document.cookie.split('; ').find(row => row.startsWith('token')).split('=')[1];
        }

    } 

    static setToken(expiration) {
        // generate token with 4 randoms word of 8 alpha-numerics caracters separated by '-'
        // Remove the old token
        

        let token = Math.random().toString(36).substring(7) + '-' + Math.random().toString(36).substring(7) + '-' + Math.random().toString(36).substring(7) + '-' + Math.random().toString(36).substring(7);
        let date = new Date();
        date.setTime(date.getTime() + (expiration * 24 * 60 * 60 * 1000));
        let expires = "expires="+date.toUTCString();
        document.cookie = "token=" + token + ";" + expires + ";path=/";
    }




    static async requestURL(url, method = "POST", params = false) {
        // console.log(url, method, params);
        if(params != false) params = JSON.stringify(params);
        if(APIManager.token() == undefined) APIManager.setToken(1);
        let response = false;
        try {
            let result = $.ajax(`${url}`, 
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
    static async request(module, library, action, id = false, method = "POST", params = false)
    {
        if(params != false) params = JSON.stringify(params);
        if(APIManager.token() == undefined) APIManager.setToken(1);
        let response = false;
        try {
            let result = $.ajax(`${APIManager.apiPath()}/${APIManager.token()}/${module}/${library}/${action}${id ? '/' + id : ''}`, 
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


    static async fullRequest(module, library, action, id = false, method = "POST", params = false)
    {
        
        if(params != false) params = JSON.stringify(params);
        if(APIManager.token() == undefined) APIManager.setToken(1);
        let result = $.ajax(`${APIManager.apiPath()}/${APIManager.token()}/${module}/${library}/${action}${id ? '/' + id : ''}`, 
        {
            type: method,
            data : params
        });
        await result;
        return result;
    }

    static getError(response, errorName) {
        if(!response.success) {
            return response.errors.find(error => error.error == errorName);
        }
        return false;
    }

    static logout() {
        APIManager.setToken(1);
        return {
            success: true
        }
    }

    static async login(login, password, remember_days = 1) {
        APIManager.setToken(remember_days);

        let hash_pass = await APIManager.hashStringToSHA256(password);

        let response = await APIManager.request('Core', 'Auth', 'Login', false, 'POST', {
            login : login,
            password: hash_pass
        });

        return response;
    }

    static async register(email, username = "NULL", password, passwordConfirm, firstname = "NULL", lastname = "NULL", phone = "NULL") {
        let regex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$/;
        if(!regex.test(password)) {
            return {
                success: false,
                errors: [
                    {
                        error: 'password',
                        message: 'Le mot de passe doit contenir au moins 8 caractères dont 1 chiffre'
                    }
                ]
            }
        }
        if(password != passwordConfirm) {
            return {
                success: false,
                errors: [
                    {
                        error: 'passwordConfirm',
                        message: 'Les mots de passe ne correspondent pas'
                    }
                ]
            }
        }

        // hash password in JS
        let hash_pass = await APIManager.hashStringToSHA256(password);
        let response = await APIManager.request('Core', 'Auth', 'Register', false, 'POST', {
            email: email,
            password: hash_pass,
            firstname: firstname,
            lastname: lastname,
            phone: phone,
            username : username
        });

        return response;
    }

    static async hashStringToSHA256(inputString) {
        const encoder = new TextEncoder();
        const data = encoder.encode(inputString);
      
        // Generate the SHA-256 hash
        const hashBuffer = await crypto.subtle.digest('SHA-256', data);
      
        // Convert the hash buffer to a hexadecimal string
        const hashArray = Array.from(new Uint8Array(hashBuffer));
        const hashHex = hashArray.map(byte => byte.toString(16).padStart(2, '0')).join('');
      
        return hashHex;
    }
}