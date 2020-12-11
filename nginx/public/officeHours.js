const socket = io.connect("http://localhost:8081", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('index',displayIndex);

//socket.on('position',checkPosition);

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
    //
}

function displayQueue(queueJSON) {
    const queue = JSON.parse(queueJSON);
    let formattedQueue = "";
    for (const student of queue) {
        formattedQueue += student['username'] + " has been waiting since " + student['timestamp'] + "<br/>"
    }
    document.getElementById("queue").innerHTML = formattedQueue;
}

function displayIndex(index){
    document.getElementById("index").innerHTML=index
}

function enterQueue() {
    let name = document.getElementById("name").value;
    socket.emit("enter_queue", name);
  //  document.getElementById("name").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}

function checkPosition(){
    let name = document.getElementById("name").value;
    socket.emit("check_position", name);
 //   document.getElementById("name").value = "";
}
//Add authentication
// function submitUsername(){
//     const enterUsername=document.getElementById("username").value;
//     let listOfTAs =["Jacob","Logan","Jesse"]
//    if(listOfTAs.includes(enterUsername)){
//        username=enterUsername;
//        setUpForTAs();
//
//    }else{
//        username=enterUsername;
//        setUpForStudent();
//
//    }
// }