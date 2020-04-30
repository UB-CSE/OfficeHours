const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on("talog", displayTA)

let taqu = []
let tastr = ""


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

function displayTA(value){
    let str = "Online TAs: ";
    //taqu = value
    let lst = JSON.parse(value);
    taqu = lst
    for(var i in taqu){
        //alert(i);
        //alert(lst);
        if(i == 0){
            str += lst[i] + "";
        }
        else{
            str += (", " + lst[i]);
        }
    }
    tastr = str
    document.getElementById("taq").innerHTML = "<br>" + str + "<br/>";
}



function enterQueue() {
    let name = document.getElementById("name").value;
    socket.emit("enter_queue", name);
    document.getElementById("name").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}

function taLogin(){
    let ta = document.getElementById("ta").value
    socket.emit("ta_login", ta)
    document.getElementById("ta").value = "";
}

function taLogout(){
    let log = document.getElementById("ta").value;
    socket.emit("ta_logout", log);
    document.getElementById("ta").value = "";
}


function displayEverything(){
    socket.emit("refresh");
}
// function timestampToRealTime(timestamp){
//     var num = int(timestamp)
//     return Date(timestamp)
// }
