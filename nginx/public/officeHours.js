const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('fbQueue', displayFeedback);
socket.on('failedEntry', displayError);
socket.on('message', displayMessage);
socket.on('leave', displayLeave);
socket.on('hide', displayHide);
socket.on('show', displayShow);
socket.on('failedSubmit', displayError2);
socket.on('failedLeave', displayError3);

function submitFeedback(){
    let message = document.getElementById("feedback").value;
    document.getElementById("feedback").value = "";
    document.getElementById("message").innerHTML = "Thank you for your feedback.";
    socket.emit("fb", message);
}

function displayError3() {
    document.getElementById("message").innerHTML = "Not in queue. Cannot Leave";
}

function displayError2() {
    document.getElementById("message").innerHTML = "Not in queue. Cannot submit feedback";
}

function displayShow() {
    document.getElementById("feedback").style.display = "block";
    document.getElementById("feedbackBtn").style.display = "block";
    document.getElementById("feedbackTitle").style.display = "block";
}

function displayHide() {
    document.getElementById("feedback").style.display = "none";
    document.getElementById("feedbackBtn").style.display = "none";
    document.getElementById("feedbackTitle").style.display = "none";
}

function displayLeave(usern){
    document.getElementById("message").innerHTML = usern + " has left the queue";
}

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
}

function displayError() {
    document.getElementById("message").innerHTML = "Already in the queue";
}

function displayQueue(queueJSON) {
    const queue = JSON.parse(queueJSON);
    let formattedQueue = "";
    for (const student of queue) {
        formattedQueue += student['username'] + " has been waiting since " + student['timestamp'] + "<br/>"
    }
    document.getElementById("queue").innerHTML = formattedQueue;
}

function displayFeedback(queueJSON) {
    const queue = JSON.parse(queueJSON);
    let formattedQueue = "";
    for(const message of queue) {
        formattedQueue += message + "<br/>"
    }
    document.getElementById("fbArea").innerHTML = formattedQueue;
}


function enterQueue() {
    let name = document.getElementById("name").value;
    socket.emit("enter_queue", name);
    document.getElementById("name").value = "";
}

function leaveQueue() {
    socket.emit("leave_queue");
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
