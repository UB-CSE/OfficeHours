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
        formattedQueue += "<b>" + student['username'] + "</b> has been waiting for help with <b>" + student['helpDescription'] + "</b> since " + student['timestamp'] + "<br/>"
    }
    document.getElementById("queue").innerHTML = formattedQueue;
}


function enterQueue() {
    let name = document.getElementById("name").value;
    let helpDescription = document.getElementById("helpDescription").value;
    let infoJson ={name:name,helpDescription:helpDescription};
    infoJson = JSON.stringify(infoJson);
    socket.emit("enter_queue", infoJson);
    document.getElementById("name").value = "";
    document.getElementById("helpDescription").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}