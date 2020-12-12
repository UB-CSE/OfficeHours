const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('positionToServer', positionToServer)
socket.on('TA_in_progress', TA_frontEnd);
socket.on('position_in_queue', positionInQueue);
socket.on('UpdateStudent', UpdateStudent);

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

function TA_frontEnd() {
    let TA_HTML = '<button onclick="readyToHelp();">TA Ready to Help</button> '

    document.getElementById('panel').innerHTML = TA_HTML;
}

function positionInQueue(place) {
    const position = place
    let StudentHTML = '<h3 id="message">You are in Queue, still need wait for ' + position + ' people </h3>'
    StudentHTML += '<button onclick="positionToServer();">Check Processing Status</button>'
    document.getElementById('panel').innerHTML = StudentHTML;
}

function UpdateStudent(TAname) {
    let StudentHTML = '<h3 id="message">TA ' + TAname + ' is now helping you </h3>'
    document.getElementById('panel').innerHTML = StudentHTML;
}

//

function positionToServer () {
    let name = document.getElementById("name").value;
    socket.emit("CheckQueue", name);
    document.getElementById("name").value = "";
}

function enterQueue() {
    let name = document.getElementById("name").value;
    socket.emit("enter_queue", name);
    document.getElementById("name").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}

function TAShowUp() {
    let name = document.getElementById("TAname").value;
    socket.emit("TA_sign_in", name);
    document.getElementById("TAname").value = "";
}
