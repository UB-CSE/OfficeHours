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

function openOH(){
    let password=document.getElementById("password").value;
    socket.emit("oh_starting",password);
    document.getElementById("password").value="";
}
function closeOH(){
    let password=document.getElementById("password").value;
    socket.emit("oh_ending",password);
    document.getElementById("password").value="";
}

function clearQueue(){
    let password=document.getElementById("password").value;
    socket.emit("clear_queue",password);
    document.getElementById("password").value="";
}