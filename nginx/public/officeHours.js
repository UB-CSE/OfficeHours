const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('position', displayQueuePosition);

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
}

function displayQueuePosition(newMessage) {
    document.getElementById("queuePosition").innerHTML = newMessage;
}

function displayQueue(queueJSON) {
    const queue = JSON.parse(queueJSON);
    let formattedQueue = "";
    for (const student of queue) {
        formattedQueue += student['username'] + " has been waiting since " + student['worldTime'] + "<br/>"
    }
    document.getElementById("queue").innerHTML = formattedQueue;
}

function leaveButton() {
    document.getElementById("leaveButton").innerHTML = '<button onclick="leaveQueue()"> Leave Queue</button>';
}

function leaveQueue() {
    socket.emit("leave_queue")
}


function enterQueue() {
    let name = document.getElementById("name").value;
    socket.emit("enter_queue", name);
    document.getElementById("name").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
