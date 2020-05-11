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
        const newColor = "#"+Math.floor(Math.random()*16777215).toString(16);
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
function getComment(){
    let comment = document.getElementById("comment").value;
    socket.emit("new_comment", comment)
    document.getElementById("comment").value = ""

}
