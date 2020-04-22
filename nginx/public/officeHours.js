const socket = io.connect({transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('issue', displayIssue);
socket.on('queuePos', displayQueueIndex)

function displayIssue(Issue){
    document.getElementById("issue").innerHTML = Issue;
}

function displayQueueIndex(message){
    document.getElementById('queueIndex').innerHTML = message
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
    let issue = document.getElementById("issue").value;
    socket.emit("enter_queue", JSON.stringify({"name" : name, "issue" : issue}));
    document.getElementById("name").value = "";
    document.getElementById("issue").value= "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
