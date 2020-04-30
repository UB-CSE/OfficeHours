const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('queuePos', displayPosition);

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

function displayPosition(queueJSON) {
    const queue = JSON.parse(queueJSON);
    let position = parseInt(queue['position']);
    let formattedQueue = "";
    if(position < 1){
        formattedQueue = "You are no longer in the office hours queue!" + "<br/>";
    }
    else{
        formattedQueue = "You are currently in position " + queue['position'] + " of the office hours queue!" + "<br/>";
    }
    document.getElementById("queuePos").innerHTML = formattedQueue;
}


function enterQueue() {
    let name = document.getElementById("name").value;
    socket.emit("enter_queue", name);
    document.getElementById("name").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
