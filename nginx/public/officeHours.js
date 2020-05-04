const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('emailAlert', SendEmail);

function SendEmail(email) {
    socket.emit("send_email", email);
}

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
    if(name.includes("@")){
        socket.emit("enter_queue", name);
        document.getElementById("name").value = "";
        //Get Audio for elevator music
        var audio = document.getElementById("ElevatorMusic");
        //Play it
        document.getElementById("message").innerHTML = "You're waiting in queue " + name + "!";
        audio.play();
    }

}

function readyToHelp() {
    socket.emit("ready_for_student");
    var audio = document.getElementById("ElevatorMusic");
    audio.pause();
    audio.currentTime = 0;
    var upNow = document.getElementById("YoureUp");
    upNow.play();

    if(document.getElementById("btnCheckIn") == null){
        var btnCheckIn = document.createElement("button");
        btnCheckIn.setAttribute("id","btnCheckIn");
        btnCheckIn.innerHTML = "Im Here!";
        document.getElementById("imHereDiv").appendChild(btnCheckIn);
        document.getElementById("btnCheckIn").addEventListener("click", stopCheckinSound);
    }
    else{
        document.getElementById("btnCheckIn").hidden = false
    }
}

function stopCheckinSound(){
    var checkinAlert = document.getElementById("YoureUp");
    checkinAlert.pause();
    checkinAlert.currentTime = 0;
    alert("Good Luck!");
    document.getElementById("btnCheckIn").hidden = true
}

