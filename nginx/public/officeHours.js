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
        formattedQueue += student['username'] + " has been waiting since " + student['timestamp'] + "<br/>"
    }
    document.getElementById("queue").innerHTML = formattedQueue;
}


// function enterQueue() {
//     //let name = document.getElementById("name").value;
//     socket.emit("enter_queue", name);
//     document.getElementById("name").value = "";
// }

function readyToHelp() {
    socket.emit("ready_for_student");
}


//NEW! --> Later implement functionality to deal with empty names? Simple conditional
function registerStudent() {
    let name = document.getElementById("name").value;
    socket.emit("student", name);
    document.getElementById("name").value = "";

    document.getElementById("name").hidden = true;
    document.getElementById("header").innerHTML = "The queue is:";
    document.getElementById("header2").hidden = true;
    document.getElementById("student_register").hidden = true;
    document.getElementById("ta_register").hidden = true
}

function registerTA() {
    let name = document.getElementById("name").value;
    socket.emit("ta", name);
    document.getElementById("name").value = "";
    document.getElementById("name").type = "password";
    document.getElementById("header").innerHTML = "Enter the password:";
    // Remember the difference between .value and .innerHTML !

    //document.getElementById("name").hidden = true
    //document.getElementById("header").hidden = true
    document.getElementById("header2").hidden = true;
    document.getElementById("student_register").hidden = true;
    document.getElementById("ta_register").hidden = true;

    document.getElementById("password_submit").hidden = false;
}

//Obviously, this should be more secure, but the idea is here. At least I'm not storing the password in the front end!
function checkPassword(){
    let password = document.getElementById("name").value;
    socket.emit("password_check", password);
    document.getElementById("name").value = "";

    document.getElementById("ready_to_help_button").hidden = false;
}

// socket.on('verified', displayReadyButton);
// function displayReadyButton(){
//     //document.getElementById("ready_to_help_button").hidden = false;


// socket.on('connect', onConnect);
// function onConnect() {
//     //Hide all unecessary elements for now!
//     //document.getElementById("password_submit").hidden = true;
//
// }

//Security implemented because I will have the Backend checking each time someone hits the button for "remove from list".
//This shouldn't be visible on a students screen, but even if they adjust HTML, they won't be able to click it and have functionality happen
//Because the server will double check they are actually a TA!

//Add functionality so that if a TA clicks on a student, they are told their TA!