const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);

let queueLength = 0;

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
}

function displayQueue(queueJSON) {
    const queue = JSON.parse(queueJSON);
    queueLength = queue.length;
    showJumbotron();
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

function jumbotronButtonPressed() {
    socket.emit("jumbotron");
    showJumbotron()
}

function showJumbotron() {
    let body = document.getElementsByTagName("body")[0];
    body.innerHTML = ""; // Clear the page

    body.innerHTML += '<h1 class="display-1">There are</h1>';
    body.innerHTML += '<h1 class="display-1" style="font-size: 3000%;">' + queueLength + '</h1>';
    body.innerHTML += '<h1 class="display-1">people in the queue</h1>';

}
