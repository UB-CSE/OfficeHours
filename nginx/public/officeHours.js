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
        formattedQueue += student['username'] + " has been waiting since " + student['timestamp'] + ". They are inquiring about: " + student['issue'] + "<br/>"
    }
    document.getElementById("queue").innerHTML = formattedQueue;
}


function enterQueue() {
    let name = document.getElementById("name").value;
    let issue = document.getElementById("issue").value;
    let userMap = [name, issue]
    socket.emit("enter_queue", userMap);
    document.getElementById("name").value = "";
    document.getElementById("issue").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
