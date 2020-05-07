const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on("entered", displayTextbox);
socket.on("sendingmessage", displayIssue);

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

function displayTextbox(amessage) {
    let somemessage = amessage;
    let messageHTML = "<br/><h1 >Let us know your issue while you wait for a TA!</h1>";
    messageHTML += '<p> Your issue:</p>';
    messageHTML += '<p>' +
        '<textarea id="textinside" cols="60" rows="10">Write Here</textarea>' +
        '</p>';
    messageHTML += '<button onclick="sendMessage();">Submit Message</button>';

    document.getElementById('queueentered').innerHTML =  messageHTML;
}

function displayIssue(issue) {
    let issuemessage = issue;
    if (issuemessage == "Write Here") { //this has an space
        document.getElementById("issuemessage").innerHTML = "User didn't write any issue in the textbox"
    } else {
        let issuing = "The issue this user has is " + issue;
        document.getElementById("issuemessage").innerHTML = issuing;
    }
}


function enterQueue() {
    let name = document.getElementById("name").value;
    socket.emit("enter_queue", name);
    document.getElementById("name").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}

function sendMessage() {
    const messageEntered = document.getElementById("textinside").value;
    if (messageEntered !== "Write Here") {
        message = messageEntered;
        socket.emit("messageReceived", message);
        let messageHTML = "<br/><h1 >Thanks for your submission, we will help you as soon as possible!</h1>";
        document.getElementById("queueentered").innerHTML = messageHTML
    }
    else {
        message = "Write Here";
        socket.emit("messageReceived", message);
        let messageHTML = "<br/><h1 >Thanks for your submission, we will help you as soon as possible!</h1>";
        document.getElementById("queueentered").innerHTML = messageHTML
    }
}
