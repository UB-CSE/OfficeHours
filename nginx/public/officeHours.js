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
        formattedQueue += student['username'] + " has been waiting since " + student['timestamp'] + " and has a description about his/her problem that '" +student['description']+ "'" + "<br/>"
    }
    document.getElementById("queue").innerHTML = formattedQueue;
}


function enterQueue() {
    let name = document.getElementById("name").value;
    let problem = document.getElementById("description").value
    let sendfile = new Map()
    sendfile["name"] = name
    sendfile["description"] = problem
    var sendstuff = JSON.stringify(sendfile)
    socket.emit("enter_queue", sendstuff);
    document.getElementById("name").value = "";
    document.getElementById("description").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
