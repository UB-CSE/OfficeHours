const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('prevS',rollTape);
socket.on('valid',Startup);
socket.on('try_again',Suss);



function Suss() {
    let div = document.createElement('div');
    div.setAttribute("id","interesting")
    div.className = "alert";
    div.innerHTML = "<strong>Wrong Info!</strong> Try Again.";
    document.body.append(div);
}

function registerClicked() {
    const troy = document.getElementById("username");
    const bake = document.getElementById("password");
    const infoS = {
        "username": troy,
        "password": bake
    }
    socket.emit("register",infoS)

}
function Startup(){
    document.write("<br/>");
    document.write(" <h3 id=\"message\">Welcome!</h3>");
    document.before("<br/>");
    document.write("<p>Name</p>") ;
    document.write("<input type=\"text\" id=\"name\"/>");
    document.write("<br/><br/>");
    document.write("<input type=\"text\" id=\"reason\"/>");
    document.write("<button onclick=\"enterQueue();reasonSaid()\">Enter Queue</button>");
    document.write("<br/><br/>");
    document.write("<button onclick=\"readyToHelp();\">TA Ready to Help</button>");
    document.write("");
    document.write("<div id=\"queue\"></div>");
    document.write("<h4>Your History</h4>");
    document.write(" <div id=\"previousVs\"></div>");
    const loginStuff = document.getElementById("register");
    loginStuff.remove();
    const errorMsg = document.getElementById("interesting");
    errorMsg.remove();

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


function rollTape(historyJson) {
    const trackOf = JSON.parse(historyJson);
    var count = 0;
    let formattedHistory = "";
    for(const pastVs of trackOf) {
        count += 1;
        formattedHistory += pastVs + " on " + count + " visit " + "<br/>"
    }
    document.getElementById("previousVs").innerHTML = formattedHistory;
}


function enterQueue() {
    let name = document.getElementById("name").value;
    socket.emit("enter_queue", name);
    document.getElementById("name").value = "";
}
function reasonSaid() {
    let reason = document.getElementById("reason").value;
    socket.emit("reasonGiven",reason);
    document.getElementById("reason").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
