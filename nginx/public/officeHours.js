const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on("alreadyInQueue",alreadyInQueue)
socket.on("impatientReady",impatientReady)
socket.on("nameChosen",nameChosen)

function nameChosen(name) {
    let k = document.getElementById("pleaseWait")
    k.innerHTML = name + " is already in queue, please choose a different name."
}

function alreadyInQueue() {
    let k = document.getElementById("pleaseWait")
    k.innerHTML = "You're already in queue, please be patient and a TA will get to you soon."
}

function impatientReady() {
    let k = document.getElementById("pleaseWait")
    k.innerHTML = " "
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
