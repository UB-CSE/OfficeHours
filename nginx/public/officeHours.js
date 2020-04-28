// const socket = io.connect("http://localhost:8080", {transports: ['websocket']});
//
// socket.on('queue', displayQueue);
// socket.on('message', displayMessage);
//
// function displayMessage(newMessage) {
//     document.getElementById("message").innerHTML = newMessage;
// }
//
// function displayQueue(queueJSON) {
//     const queue = JSON.parse(queueJSON);
//     let formattedQueue = "";
//     for (const student of queue) {
//         formattedQueue += student['username'] + " has been waiting since " + student['timestamp'] + "<br/>"
//     }
//     document.getElementById("queue").innerHTML = formattedQueue;
// }
//
//
// function enterQueue() {
//     let name = document.getElementById("name").value;
//     socket.emit("enter_queue", name);
//     document.getElementById("name").value = "";
// }
//
// function readyToHelp() {
//     socket.emit("ready_for_student");
// }

const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

let current_user_timestamp = 0;

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('release_help_button', release_button);

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
    let username = document.getElementById("ta_user").value;
    let password = document.getElementById("ta_password").value;
    let object = {"username": username, "password": password};
    socket.emit("ta_login", JSON.stringify(object))
}

function release_button() {
    let id = document.getElementById("help_button");
    let buttonElement = document.createElement("BUTTON");
    buttonElement.innerHTML = "Help Next Student";
    buttonElement.onclick = readyToHelp;
    document.body.appendChild(buttonElement)
}
