const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('display', displayTwice);

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;}

function displayTwice(newMessage) {
    document.getElementById("display").innerHTML = newMessage;}
function password(){
let secondHTML = "<br/><h1 id=\"displayCurrency\">Enter Password</h1>";

secondHTML += '<br/>';
secondHTML += "<br/> <input type= text id=\"name\" />"
secondHTML += "<button   onclick=\"check();\">"+ 'enter' + "</button>";
secondHTML += '<br/>';
document.getElementById('game').innerHTML = secondHTML;
}

function TA(){

        let secondHTML = "<br/><h1 id=\"displayCurrency\">TA</h1>";
       secondHTML += "<button style=\"width:200px;height:100px;font-size:30px\"  onclick=\"readyToHelp();\">"+ 'readyToHelp' + "</button>";
        secondHTML += '<br/>';
         secondHTML += "<br/> <h1 id=\"message\"></h1> ";
         secondHTML += '<br/>';
         secondHTML += "<br/> <h9 id=\"queue\"></h9> ";
                  secondHTML += '<br/>';
        document.getElementById('game').innerHTML = secondHTML;

}

function check(){
let name = document.getElementById("name").value;
         if (name == "404"){
         TA();
        socket.emit("student history");
         }
         document.getElementById("name").value = "";
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
         socket.emit("enter_queue", name);
         document.getElementById("name").value = "";
     }

function readyToHelp() {
    socket.emit("ready_for_student");
}
