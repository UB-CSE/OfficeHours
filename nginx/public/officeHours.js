
const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
}

function displayQueue(queueJSON) {
    const queue = JSON.parse(queueJSON);
    let formattedQueue = "";
    let count = 1
    for (const student of queue) {
        formattedQueue += count + ")  " + student['username'] + "  has been waiting for  " + (student['timestamp']/60000000000.0).toFixed(3) + "  minutes "+ "  ETA:  " + count*10 + "  minutes" + "<br/><br/>"
        count+=1
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
