const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('repeat', displayRepeat);

function displayRepeat(sendMsg) {
    const queue = JSON.parse(sendMsg);
    if (queue['repeat'] === 'yes'){
        let updatedForRepeats = queue['username'] + " is already in queue"
        document.getElementById("repeat").innerHTML = updatedForRepeats;
    }else{
        let updatedForRepeats = queue['username'] + " is now in queue"
        document.getElementById("repeat").innerHTML = updatedForRepeats;
    }
}

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


function enterQueue() {
    let name = document.getElementById("name").value;
    socket.emit("enter_queue", name);
    document.getElementById("name").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
