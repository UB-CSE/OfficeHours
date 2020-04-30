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
        var date = new Date(student['timestamp'])
        var time = date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + " (" + (date.getMonth()+1) + "/" + date.getDate() + "/" + date.getFullYear() + ")";
        formattedQueue += student['username'] + " has been waiting since " + time + "<br/>"
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
