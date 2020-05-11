const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
}

function displayQueue(queueJSON) {
    const queue = JSON.parse(queueJSON);
    let formattedQueue = "";
    let numberInQueue = 1;
    for (const student of queue) {
        if(numberInQueue == 1){
            const aRandomColorForText = Math.floor(Math.random()*Math.pow(256,3)).toString(16);
            formattedQueue += (student['username'] + " has been waiting since " + student['timestamp'] + " and will be the next student " + " helped today!" + "<br/>").fontcolor(aRandomColorForText);
            numberInQueue += 1
        }else{
            const aRandomColorForText = Math.floor(Math.random()*Math.pow(256,3)).toString(16);
            formattedQueue += (student['username'] + " has been waiting since " + student['timestamp'] + " and will be student number " + numberInQueue + " in the line." + "<br/>").fontcolor(aRandomColorForText);
            numberInQueue += 1
        }
    }
    if(formattedQueue == ""){
        document.getElementById("queue").innerHTML = "Everyone has been helped!";
    }else{
        document.getElementById("queue").innerHTML = formattedQueue;
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
