const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('position', displayPosition);
socket.on('update', displayPositionUpdate);

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
}

function displayPosition(position){
    let formattedPosition = "";
    formattedPosition = "Your Position in Queue is: " + position;
    document.getElementById("position").innerHTML = formattedPosition;
}

function displayPositionUpdate(position){
    let formattedPosition = "";
    formattedPosition = "Your Position in Queue is:" + (position);
    document.getElementById("position").innerHTML = formattedPosition;
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
