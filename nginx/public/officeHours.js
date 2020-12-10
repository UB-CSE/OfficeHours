const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

let username = "";
let fullName = "";
let email = "";
let password = "";
let kindOfUser = "";

let dataJson = {};

socket.on('queue', displayQueue);
socket.on('message', displayMessage);

loggedIn();

successfulMessage();

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
}

function displayQueue(queueJSON) {
    const queue = JSON.parse(queueJSON);
    let formattedQueue = "";
    for (const student of queue) {
        formattedQueue += student['username'] + " has been waiting since " + student['timestamp'] + "<br/>"
    }
    document.getElementById("queue").innerHTML = formattedQueue;
}


function enterQueue() {
    let name = document.getElementById("name").value;
    socket.emit("enter_queue", name);
    document.getElementById("name").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}

/**
 * Authentication:
 * User will login using email or username.
 * send all information to a server regarding the user
 *
 * If User click register send the information to a server and check if the user already exists. If not
 * Save the user information into the DB using MySql
 * Send an activation email also to the user. Once the user activate his/her email.
 * WIll be able to login and then can make an appointment and other feature provided by the "Name of the websites"
 * * */
function register(){
    const userName1 = document.getElementById("username").value;
    const fullName1 = document.getElementById("full_name").value;
    const email1 = document.getElementById("email").value;
    const password1 = document.getElementById("password").value;
    const kind = document.getElementById("typeOfAccount").value;
    if(userName1 !== "" && fullName1 !== "" && email1 !== "" && password1 !== "" && kind !== ""){
        //Create a json with the information above
        username = userName1;
        fullName = fullName1;
        email = email1;
        password = password1;
        kindOfUser = kind;

        dataJson["username"] = username;
        dataJson["fullName"] = fullName;
        dataJson["email"] = email;
        dataJson["password"] = password;
        dataJson["kindOfUser"] = kindOfUser;
        //Printout all json var
        console.log(dataJson);

        socket.emit("register", JSON.stringify(dataJson));
    }
}
function login(){
    const email1 = document.getElementById("emailLogin").value;
    const password1 = document.getElementById("password_login").value;
    if(email1 !== "" && password1 !== ""){
        if(checkInputEmailOrUsername(email1)){
            email = email1;
            dataJson["email"] = email;
            password = password1;
            dataJson["password"] = password;
            socket.emit("login", JSON.stringify(dataJson));
        }else{
            alert("Invalid Email");
        }
    }

}


function checkInputEmailOrUsername(data){
    var check = false
    //const dataCheck = document.getElementById("emailLogin").value;
    const re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

    if(re.test(data)){
        check = true
    }
    return check
}

/**
 * When a user logged in, show a new page for that
 * */
function loggedIn(){
    socket.on("loggedIn", function (data){
        let pageHTML = ' <div id="content1">\n' +
            '        <div class="container">\n' +
            '            <table class="table table-dark table-striped">\n' +
            '                <thead>\n' +
            '                <tr>\n' +
            '                    <th scope="col">#</th>\n' +
            '                    <th scope="col">First</th>\n' +
            '                    <th scope="col">Last</th>\n' +
            '                    <th scope="col">Class</th>\n' +
            '                    <th scope="col">Action</th>\n' +
            '                </tr>\n' +
            '                </thead>\n' +
            '\n' +
            '                <tbody>\n' +
            '                <tr>\n' +
            '                    <th scope="row">1</th>\n' +
            '                    <td>Jesse</td>\n' +
            '                    <td>Lecture</td>\n' +
            '                    <td>CSE-116</td>\n' +
            '                    <td><button id="schedule">Schedule</button></td>\n' +
            '                </tr>\n' +
            '                </tbody>\n' +
            '            </table>\n' +
            '        </div>\n' +
            '    </div>'

        document.getElementById("pageChange").innerHTML = pageHTML;

    });
}

//Successfully User register:
function successfulMessage(){
    socket.on("registered_succefully", function (event) {
        document.getElementById("successfully").innerText = "You have registered successfully, Login now!!" + event
    });
}