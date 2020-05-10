const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('prevS',rollTape);



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


function rollTape(historyJson) {
    const trackOf = JSON.parse(historyJson);
    var count = 0;
    let formattedHistory = "";
    for(const pastVs of trackOf) {
        count += 1;
        formattedHistory += pastVs + " on " + count + " visit " + "<br/>"
    }
    document.getElementById("previousVs").innerHTML = formattedHistory;
}


function enterQueue() {
    let name = document.getElementById("name").value;
    socket.emit("enter_queue", name);
    document.getElementById("name").value = "";
}
function reasonSaid() {
    let reason = document.getElementById("reason").value;
    socket.emit("reasonGiven",reason);
    document.getElementById("reason").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
