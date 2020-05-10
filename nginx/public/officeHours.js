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

// Rudimentary Authentication System - Gets the Name and Password and adds them together to create a unique identifier that would be within a whitelist for TAs
function enterAvailable() {
    let name = document.getElementById("name").value;
    let password = document.getElementById("password").value;
    socket.emit("enter_available", (name + password));
    document.getElementById("name").value = "";
    document.getElementById("password").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
