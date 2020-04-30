const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);

//listen 'position' to display each student's position
socket.on('position', displayPosition);

//listen 'boolean' to check if the TA code is correct or not
socket.on('boolean', Action);

//Assume all clients at the beginning are not the TA
var TAstate = false;

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

//display student's position in html
function displayPosition(YourPosition){
    document.getElementById("position").innerHTML = YourPosition;
}


function enterQueue() {
    let name = document.getElementById("name").value;
    socket.emit("enter_queue", name);
    document.getElementById("name").value = "";
}

//grab the code from html to check if it's the right code, send this from the client to the server
function checkCode() {
    let code = document.getElementById("TAcode").value;
    socket.emit("validate", code);
}

//base on the verification, give different actions using control flow
function Action(boolean){
    if (boolean == "true" && TAstate == false){
        TAstate = true;
        var help = document.createElement("button");
        help.innerHTML = "TA Ready to Help";
        document.body.appendChild(help);
        help.addEventListener ("click", function readyToHelp(){
            socket.emit("ready_for_student");
        });
        alert("valid code");
        socket.emit("verified")
    }
    else if (boolean == "true" && TAstate == true){
        alert("you are TA already!");
    }
    else{
        alert("invalid code");
    }
}

//abandon this
//function readyToHelp() {
    //socket.emit("ready_for_student");
//}
