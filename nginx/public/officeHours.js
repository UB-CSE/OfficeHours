const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('start', displayTA)
socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('tooManyTAs', displayError)
socket.on('makeControlsAppear', controlButtonAppear)

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
}

function displayTA(TA_name) {
    let formattedQueue = "";
    let TAcolor = "<span style=\"color:#005BBB;\">" + TA_name + "</span>";
    formattedQueue += TAcolor
    formattedQueue += " has started their session!"
    document.getElementById("message").innerHTML = formattedQueue;
}

function displayQueue(queueJSON) {
    const queue = JSON.parse(queueJSON);
    let formattedQueue = "";
    for (const student of queue) {
        let studentColor = "<span style=\"color:#005BBB;\">" + student['username'] + "</span>";
        formattedQueue += studentColor
        formattedQueue += " has been waiting since "
        let timeColor = "<span style=\"color:#00b5c8;\">" + student['timestamp'] + "</span>";
        formattedQueue += timeColor + "<br/>"
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

function enterTA(button) {
    let name = document.getElementById("name").value;
    socket.emit("check_TA", name);
    document.getElementById("name").value = "";
}

function controlButtonAppear(){
    let button1 = document.getElementById("nextButton")
    let button2 = document.getElementById("signoutButton")
    button1.disabled = false;
    button2.disabled = false;
}

function displayError(){
    alert("Only one TA per session")
}

function signOut() {
    socket.emit("TA_sign_out")
}