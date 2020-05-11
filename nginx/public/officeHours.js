const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('position', displayPosition);
socket.on('ta_list', displayTAList);
socket.on('ta_button', TAButton);

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
}

function displayQueue(queueJSON) {
    const queue = JSON.parse(queueJSON);
    let formattedQueue = "";
    for (const student of queue) {
        formattedQueue += student['username'] + " has been waiting since " + student['timestamp'] + "<br/>";
    }
    document.getElementById("queue").innerHTML = formattedQueue;
}

function displayPosition(position) {
    document.getElementById("queue").innerHTML = position;
}

function displayTAList(TAListJSON) {
    const TAList = JSON.parse(TAListJSON);
    let formattedList = "";
    for (const TA of TAList) {
        formattedList += TA + "</br>";
    }
    document.getElementById("ta_list").innerHTML = formattedList;
}

// inserts the TAButton
function TAButton() {
    document.getElementById("TAButton").innerHTML = "<button onclick='readyToHelp();'>TA Ready to Help</button>";
}

function enterQueue() {
    let name = document.getElementById("name").value;
    socket.emit("enter_queue", name);
    document.getElementById("name").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
