const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('studentMessage', displayStudentMessage);
socket.on('taMessage',displayTaMessage);
socket.on('position', displayPosition);

function displayStudentMessage(newMessage) {
    document.getElementById("initial").innerHTML = newMessage;
    document.getElementById("queue").innerHTML = '';
}

function displayTaMessage(newMessage) {
    document.getElementById("messages").innerHTML = newMessage;
}


function displayPosition(position){
    let info = JSON.parse(position);
    document.getElementById("queue").innerHTML = "Queue Position " + info.toString();
}

function displayQueue(queueJSON) {
    const queue = JSON.parse(queueJSON);
    let formattedQueue = "";
    for (const student of queue) {
        formattedQueue += student['username'] + " has been waiting since " + student['timestamp'] + "<br/>"
    }
    document.getElementById("queue").innerHTML = formattedQueue;
}


function enterQueueTA() {
    let name = document.getElementById("name").value;
    socket.emit("enter_queue_TA", name);
    let html = '<button onclick="readyToHelp();">TA Ready to Help</button>';
    document.getElementById("initial").innerHTML = html;
    document.getElementById("messages").innerHTML = '';
}

function enterQueueStudent() {
    let name = document.getElementById("name").value;
    socket.emit("enter_queue_Student", name);
    let html = "<h1>Waiting for next Available TA </h1>";
    document.getElementById("initial").innerHTML = html
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
