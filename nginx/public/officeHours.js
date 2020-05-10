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
        formattedQueue += student['username'] + " has been waiting to discuss \"" + student['description'] + "\" since " + student['timestamp'] + "<br/>"
    }
    document.getElementById("queue").innerHTML = formattedQueue;
}


function enterQueue() {
    let payload = document.getElementById("name").value;
    payload = payload + "᳄";
    payload = payload + document.getElementById("description").value;
    socket.emit("enter_queue", payload);
    document.getElementById("name").value = "";
    document.getElementById("description").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
