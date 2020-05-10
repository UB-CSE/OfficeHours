const socket = io.connect("http://localhost:8080", {transports: ['websocket']});
let namesInQueue = Array();

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('bad_username', usernameError);
socket.on('bad_socket', socketError);

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
}

function usernameError(usernameJson) {
    const user = JSON.parse(usernameJson)
    document.getElementById("error_msg").innerHTML = user['name']+ " is already in the queue.\nTry a new name.";
}

function socketError() {
    document.getElementById("error_msg").innerHTML = "You are already in the queue.";
}

function displayQueue(queueJSON) {
    const queue = JSON.parse(queueJSON);
    let index = 0;
    let formattedQueue = "";
    for (const student of queue) {
        index += 1;
        formattedQueue += student['username'] + " has been waiting since " + student['timestamp'] + "<br/>"
    }

    let queueLengthMsg = "You are "

    let tensPlace = index;
    while (tensPlace.toString.length != 1) {
        tensPlace = tensPlace / 10
    }

    switch(tensPlace) {
        case 0:
            queueLengthMsg = "You are not in line\n";
            break;
        case 1:
            queueLengthMsg += index + "st in Line\n";
            break;
        case 2:
            queueLengthMsg += index + "nd in Line\n";
            break;
        case 3:
            queueLengthMsg += index + "rd in Line\n";
            break;
        default:
            queueLengthMsg += index + "th in Line\n";
    }


    document.getElementById("queueLength").innerHTML = queueLengthMsg
    document.getElementById("queue").innerHTML = formattedQueue;
    document.getElementById("error_msg").innerHTML = "";

}


function enterQueue() {
    let name = document.getElementById("name").value;
    socket.emit("enter_queue", name);
    document.getElementById("name").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
