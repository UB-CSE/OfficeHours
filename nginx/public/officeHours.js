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
        formattedQueue += student['username'] + " has been waiting since " + student['timestamp'] + " needs assistance because/with: "  + student['purpose'] + "<br/>"
    }
    document.getElementById("queue").innerHTML = formattedQueue;
}


function enterQueue() {
    let name = document.getElementById("name").value;
    let purpose = document.getElementById("purpose").value;
    socket.emit("enter_queue", name + "///" + purpose);
    document.getElementById("name").value = "";
    document.getElementById("purpose").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}


