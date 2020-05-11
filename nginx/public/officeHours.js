
const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
}

function displayQueue(queueJSON) {
    let user = ""
    const queue = JSON.parse(queueJSON);
    let formattedQueue = "";
    for (const student of queue) {
        formattedQueue += student['username'] + " has been waiting since " + student['timestamp'] +
         " and needs help with"  + " "  + student["question"]
         + "<br/>"
        user = student['username'] + "<br/>"
    }
    document.getElementById("queue").innerHTML = formattedQueue;
}


function enterQueue() {
    let name = document.getElementById("name").value;
    let question = document.getElementById("question").value;
    console.log(question);
    let value = JSON.stringify({"name":name,"question":question});
    socket.emit("enter_queue", value);
    document.getElementById("name").value = "";
    document.getElementById("question").value = "";

}

function readyToHelp() {
    socket.emit("ready_for_student");
}
