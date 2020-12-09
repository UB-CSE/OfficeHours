const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);

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
function login(){
    socket.emit("login_user");
}

function register(){
    socket.emit("register_user");
}