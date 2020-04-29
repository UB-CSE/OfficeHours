const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('ta', displayTA);

function getDisplay() {
    socket.emit("display");
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

//TA functionality
function checkIn() {
    let ta_name = document.getElementById("ta_name").value;
    socket.emit("check_in", ta_name);
    document.getElementById("ta_name").value = "";
}

function checkOut() {
    let ta_name = document.getElementById("ta_name").value;
    socket.emit("check_out", ta_name);
    document.getElementById("ta_name").value = "";
}

function displayTA(taJSON) {
    const ta_list = JSON.parse(taJSON);
    let formattedTA = "";
    for (const ta of ta_list) {
        formattedTA += ta['username'] + " has checked in since " + ta['timestamp'] + "<br/>"
    }
    document.getElementById("ta_list").innerHTML = formattedTA;
}

