// const socket = io.connect("http://localhost:8080", {transports: ['websocket']});
const socket = io.connect({transports: ['websocket']});


let isJumbotron = false; // Keep track of if this is being used as a jumbotron
// if it is, don't try to reference elements that don't exist on the jumbotron

socket.on('queue', displayQueue);
socket.on('message', displayMessage);

socket.on('login_successful', showAlert);
socket.on('release_help_button', release_button);
socket.on('login_failed', invalid_login);
socket.on('already_logged_in', already_in);

socket.on('jumbotron', updateJumbotron);

function displayMessage(newMessage) {
    if (!isJumbotron) {
        document.getElementById("message").innerHTML = newMessage;
    }
}

function displayQueue(queueJSON) {
    const queue = JSON.parse(queueJSON);
    let formattedQueue = "";
    for (const student of queue) {
        formattedQueue += "<b>" + student['username'] + "</b> has been waiting for help with <b>" + student['helpDescription'] + "</b> since " + student['timestamp'] + "<br/>"
    }
    if (!isJumbotron) {
        document.getElementById("queue").innerHTML = formattedQueue;
    }
}


function enterQueue() {
    let name = document.getElementById("name").value;
    let helpDescription = document.getElementById("helpDescription").value;
    let infoJson ={name:name,helpDescription:helpDescription};
    infoJson = JSON.stringify(infoJson);
    socket.emit("enter_queue", infoJson);
    document.getElementById("name").value = "";
    document.getElementById("helpDescription").value = "";
    if (!isJumbotron) {
        let name = document.getElementById("name").value;
        socket.emit("enter_queue", name);
        document.getElementById("name").value = "";
    }
}

function readyToHelp() {
    socket.emit("ready_for_student");
}


function ta_login() {
    let username = document.getElementById("ta_user");
    let password = document.getElementById("ta_password");
    let object = {"username": username.value, "password": password.value};
    socket.emit("ta_login", JSON.stringify(object));
    username.value = "";
    password.value ="";
}

function release_button() {
    let buttonElement = document.createElement("BUTTON");
    buttonElement.innerHTML = "Help Next Student";
    buttonElement.onclick = readyToHelp;
    document.body.appendChild(buttonElement);
}

function invalid_login() {
    alert("Incorrect username or password. Try Again.")
}

function showAlert() {
    alert("Login Successful!")
}

function already_in() {
    alert("You are already logged in!")

function jumbotronButtonPressed() {
    socket.emit("jumbotron");
    isJumbotron = true;
    showJumbotron()
}

function updateJumbotron(queueJSON) {
    const queue = JSON.parse(queueJSON);
    let queueLength = queue.length;
    let top = document.getElementById("jumbotop");
    let mid = document.getElementById("jumbomiddle");
    let bot = document.getElementById("jumbobottom");

    if (queueLength == 1) {
        top.innerHTML = "There is";
        bot.innerHTML = "student in the queue";
    } else {
        top.innerHTML = "There are";
        bot.innerHTML = "students in the queue";
    }

    if (queueLength === 0) {
        mid.innerHTML = "no";
        mid.style.color = "green";
    } else if (queueLength <= 2) {
        mid.innerHTML = queueLength;
        mid.style.color = "green";
    } else if (queueLength <= 6) {
        mid.innerHTML = queueLength;
        mid.style.color = "yellow";
    } else if (queueLength <= 10) {
        mid.innerHTML = queueLength;
        mid.style.color = "orange";
    } else {
        mid.innerHTML = queueLength;
        mid.style.color = "red";
    }
}

// Converts the page into a jumbotron, refresh the page to undo
function showJumbotron() {
    let body = document.getElementsByTagName("body")[0];
    body.innerHTML = ""; // Clear the page

    body.innerHTML += '<h1 id="jumbotop" class="display-1"></h1>';
    body.innerHTML += '<h1 id="jumbomiddle" class="display-1" style="font-size: 3000%;"></h1>';
    body.innerHTML += '<h1 id="jumbobottom" class="display-1"></h1>';

}
