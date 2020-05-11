const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('quickQueue', displayQuickQueue);
socket.on('normalQueue', displayNormalQueue);
socket.on('message', displayMessage);

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
}

function displayQueue(queueJSON) {
    const queue = JSON.parse(queueJSON);
    let formattedQueue = "";
    for (const student of queue) {
        formattedQueue += student['username'] + " has been waiting since " + student['timestamp'] + " hours with message: " + student['message'] + "<button onclick = 'addToQuickQueue()'>Add to Quick Queue</button>" + "<button onclick = 'addToNormalQueue()'>Add to Normal Queue</button>" + "</br>"
    }
    document.getElementById("queryQueue").innerHTML = formattedQueue;
}

function displayQuickQueue(queueJSON) {
    const queue = JSON.parse(queueJSON);
    let QuickFormattedQueue = "";
    for (const student of queue) {
        QuickFormattedQueue += student['username'] + " has been waiting for " + student['timestamp'] + "hours" + "<br>";
    }
    document.getElementById("quickQueue").innerHTML = QuickFormattedQueue;
}

function displayNormalQueue(queueJSON) {
    const queue = JSON.parse(queueJSON);
    let NormalFormattedQueue = "";
    for (const student of queue) {
        QuickFormattedQueue += student['username'] + " has been waiting for " + student['timestamp'] + "hours" + "<br>";
    }
    document.getElementById("normalQueue").innerHTML = NormalFormattedQueue;
}

function enterQueryQueue() {
    let name = document.getElementById("name").value;
    const message = document.getElementById("question").innerHTML;
    if(name.length > 0) {
        socket.emit("enter_queue", name, message); //check this
        document.getElementById("name").value = "";
        document.getElementById("question").value = "";
    }
}

function readyToHelpQuick() {
    socket.emit("ready_for_quick_student");
    println("Quick Removed");
}

function readyToHelpNormal() {
    socket.emit("ready_for_normal_student");
}
function addToQuickQueue(){
    socket.emit("add_to_quick_queue", document.getElementById("name").value);
    socket.emit("remove_from_query_queue", document.getElementById("name").value)
}
function addToNormalQueue(){
    socket.emit("add_to_normal_queue", document.getElementById("name").value);
    socket.emit("remove_from_query_queue", document.getElementById("name").value)
}
