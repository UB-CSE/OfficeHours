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
        formattedQueue += student['username'] + " needs help with ".bold() + student['description']
            + " and has been waiting since ".bold() + student['timestamp'] + '</br>';
    }
    document.getElementById("queue").innerHTML = formattedQueue;
}


function enterQueue() {
    let name = document.getElementById("name").value;
    let problem = document.getElementById("description").value;
    let arr = [name, problem];
    socket.emit("enter_queue", arr);
    document.getElementById("name").value = "";
    document.getElementById("description").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
