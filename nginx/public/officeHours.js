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
        formattedQueue += student['username'] + " has been waiting since " + student['timestamp'] + " because " + student['reason']+ "<br/>"
    }
    document.getElementById("queue").innerHTML = formattedQueue;
}


function enterQueue() {
    let name = document.getElementById("name").value;
    let desc = document.getElementById("description").value;
    let map = {"name": name, "reason":desc}
    socket.emit("enter_queue", JSON.stringify(map));
    document.getElementById("name").value = "";
    document.getElementById("description").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
