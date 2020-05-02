// const socket = io.connect("http://localhost:8080", {transports: ['websocket']});
const socket = io.connect({transports: ['websocket']});


socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('login_successful', showAlert);
socket.on('release_help_button', release_button);
socket.on('login_failed', invalid_login);
socket.on('already_logged_in', already_in);

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
}

function displayQueue(queueJSON) {
    const queue = JSON.parse(queueJSON);
    let formattedQueue = "";
    for (const student of queue.reverse()) {
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

function ta_login() {
    let username = document.getElementById("ta_user");
    let password = document.getElementById("ta_password");
    let object = {"username": username.value, "password": password.value};
    socket.emit("ta_login", JSON.stringify(object));
    username.value = "";
    password.value ="";
}

function release_button() {
    let buttonElement = document.createElement("BUTTON");
    buttonElement.innerHTML = "Help Next Student";
    buttonElement.onclick = readyToHelp;
    document.body.appendChild(buttonElement);
}

function invalid_login() {
    alert("Incorrect username or password. Try Again.")
}

function showAlert() {
    alert("Login Successful!")
}

function already_in() {
    alert("You are already logged in!")
}
