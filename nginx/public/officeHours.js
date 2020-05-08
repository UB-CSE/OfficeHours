const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);

let num = 0
let num2 = 1

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
    if(name.length!=0){
        num = num + 1;
        name = "Ticket "+num+":"+name;
        document.getElementById("ticket").innerHTML = num;
    }
    socket.emit("enter_queue", name);
    document.getElementById("name").value = "";
}

function readyToHelp() {
    if(num2 <= num){
        document.getElementById("ticket2").innerHTML = num2;
        num2 = num2 + 1
    }
    socket.emit("ready_for_student");
}
