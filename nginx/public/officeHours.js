const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('empty_warning', displayWarning);

const inputBox = document.getElementById("name");
inputBox.addEventListener("change", borderColorController)

function borderColorController(e) {
    const target = e.currentTarget
    // console.log(target)
    if (!target) return;
    if (!target.value || target.value === "") {
        target.style.borderColor = "red"
    } else {
        target.style.borderColor = "grey"
    }
}

function displayWarning(dataStr) {
    console.log(dataStr)
    alert(dataStr)
}

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
    if (!inputBox) return;
    let name = inputBox.value;
    // if (!name || name === "") {
    //
    // } else {
        socket.emit("enter_queue", name);
        inputBox.value = "";
    // }
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
