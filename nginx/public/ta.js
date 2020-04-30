

const socket = io.connect("http://localhost:8080", {transports: ['websocket']});
//const socket = io.connect({transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
}

function displayQueue(queueJSON) {
    const queue = JSON.parse(queueJSON);
    let formattedQueue = "";
    for (const student of queue) {
        formattedQueue += student['username'] + " with queue no " + student['queueNo']+ " has been waiting since " + student['timestamp'] + "<br/>"
    }
    document.getElementById("queue").innerHTML = formattedQueue;
}




function readyToHelp() {
    socket.emit("ready_for_student");
}
socket.emit("TaHere");
