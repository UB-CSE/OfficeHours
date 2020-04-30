const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('position', displayInquiry);

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

function displayInquiry(position) {

    console.log(position)

    let map = JSON.parse(position)

    let name = map.name.toString()

    let index = map.index.toString()

    let msg = name + " position is " + index



    document.getElementById("position").innerHTML = msg

}


function enterQueue() {
    let name = document.getElementById("name").value;
    socket.emit("enter_queue", name);
    document.getElementById("name").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");

}

function myPosition() {
    let name = document.getElementById("myName").value;
    socket.emit("check_position", name)
    document.getElementById("myName").value = ""

}
