const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

//socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('new_place', function (place) {
    let placeInline = "you are number " + place + " in line";
    if (Number(place) == 0){
    placeInline = "Ta is ready to meet you!"
    socket.emit("leave_queue")
     }
      document.getElementById("place").innerHTML = placeInline ;});

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
    socket.emit("place", name);
    document.getElementById("name").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
