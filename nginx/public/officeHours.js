const socket = io.connect("http://localhost:8080", {transports: ['websocket']});
//"http://localhost:8080",
socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('authenticateStaff', receiveAuthentication);

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

function authenticateTA(){
    let userName = document.getElementById("ubit").value;
    let password = document.getElementById("password").value;
    let userPass = userName + ":" + password;
    document.getElementById("ubit").value = "";
    document.getElementById("password").value = "";
    //console.log(userPass);
    socket.emit("authenticate_TA", userPass);
}

function registerNewStaff(){
    let userName = document.getElementById("ubit").value;
    let password = document.getElementById("password").value;
    let userPass = userName + ":" + password;
    document.getElementById("ubit").value = "";
    document.getElementById("password").value = "";
    socket.emit("register_TA", userPass)
}

function receiveAuthentication(ubit) {
    document.getElementById("privilege").innerHTML = "Privilege: Admin";
    document.getElementById("authenticate").innerHTML = "Welcome to OH hell " + ubit + "!";
    let helpStudent = document.createElement("BUTTON");
    helpStudent.innerHTML = "TA Ready to Help!";
    helpStudent.addEventListener("click", readyToHelp);
    document.body.appendChild(helpStudent);
    let registerStaff = document.createElement("BUTTON");
    registerStaff.innerHTML = "Register a new TA!";
    registerStaff.addEventListener("click", registerNewStaff);
    document.getElementById("buttons").appendChild(registerStaff)
}