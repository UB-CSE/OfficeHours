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
        formattedQueue += student['username'] + " has been waiting for " + student['tag'] + " help since " + student['timestamp'] + "<br/>"
    }
    document.getElementById("queue").innerHTML = formattedQueue;
}

let currentTag = ""
function enterQueue() {
    let name = document.getElementById("name").value;
    if (currentTag != "" && name != "") {
        let json = {name: name, curTag: currentTag}
        let jason = JSON.stringify(json)
        socket.emit("enter_queue", jason);
        currentTag = ""
        document.getElementById("name").value = "";
        document.getElementById("noTag").value = "";
    } else if (name == "") {
        document.getElementById("noTag").value = "please write your name";
    } else {
        document.getElementById("noTag").value = "please choose a tag";
    }
}

function readyToHelp() {
    socket.emit("ready_for_student");
}

function tag(category) {
    currentTag = ""
    currentTag = category
}
