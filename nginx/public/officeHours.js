const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('feedback', postFeedback);

var currentStudent = null

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
}

function displayQueue(queueJSON) {
    document.getElementById("thanks").style.display = "none";
    const queue = JSON.parse(queueJSON);
    currentStudent = queue[0]['username']
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

function postFeedback(){
    if(currentStudent!=null) {
        document.getElementById("texter").innerHTML = "How was your experience " + currentStudent + "?"
        document.getElementById("feedback").style.display = "inline";
    }
}
function feedbackSubmitted() {
    const data = document.getElementById("feed");
    const value = data.value;
    data.value = '';
    const toSend = JSON.stringify(value);
    socket.emit("feedbackSubmitted", toSend)
    document.getElementById("feedback").style.display = "none";
    document.getElementById("thanks").style.display = "inline";
}