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
        formattedQueue += student['username'] + " has been waiting for help with "+ student['assignment']+" since " + student['timestamp'] + "<br/>"
    }
    document.getElementById("queue").innerHTML = formattedQueue;
}


function enterQueue() {
    let name = document.getElementById("name").value;
    let assignment = document.getElementById("assignment").value;
    let mappie = {
        "assignment": assignment,
        "name": name
    }
    let jasonMappie = JSON.stringify(mappie)
    socket.emit("enter_queue", jasonMappie);
    document.getElementById("name").value = "";
    document.getElementById("assignment").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
