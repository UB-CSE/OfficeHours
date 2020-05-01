const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('current_usage_stats_response', display_current_usage_stats);

function onLoadFunctions() {
    setInterval(send_current_usage_stats_request,2000);
}

function send_current_usage_stats_request() {
    socket.emit("current_usage_stats_request");
}

function display_current_usage_stats(current_usage_stats_json) {
    const current_usage_stats = JSON.parse(current_usage_stats_json);
    document.getElementById("number_connected").innerHTML = current_usage_stats["number_connected"];
    document.getElementById("number_waiting").innerHTML = current_usage_stats["number_waiting"];
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
    let name = document.getElementById("name").value;
    socket.emit("enter_queue", name);
    document.getElementById("name").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}