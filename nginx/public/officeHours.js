const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('direct_message', function (event) {
    showDM(event)
});
socket.on('dm_dc', resetChat);

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

function sendDM() {
    let direct_message = document.getElementById("dmText").value;
    document.getElementById("dmText").value = "";
    let msg ={"message":direct_message};
    socket.emit("direct_message", JSON.stringify(msg));
}

function showDM(msgJson) {
    const message = JSON.parse(msgJson);
    let chat = document.getElementById("chat").innerHTML;
    document.getElementById("chat").innerHTML = ("<b>" + message['sender'] + "</b>: " + message['text'] + "</br>") + chat;
}

function resetChat() {
    document.getElementById("chat").innerHTML = ""
}