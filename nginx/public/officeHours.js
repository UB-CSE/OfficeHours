const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
}

//Made changes in displayQueue function as it now takes the value of the preferred TA, process and send it to the server

function displayQueue(queueJSON) {
    var date = new Date();
    var timestamp = date.getTime();
    console.log(timestamp)
    const queue = JSON.parse(queueJSON);
    let formattedQueue = "";
    for (const student of queue) {
    var diff = student['username'].split(",")
        formattedQueue +=  "<tr onclick='readyToHelp(this);'><td>" + diff[0] + "</td><td>"+ diff[1] +"</td><td>"+ student['timestamp'] + "</td><td><input type='checkbox'></td></tr>"
    }
    console.log(formattedQueue)
    document.getElementById("queue").innerHTML = "<tr><th>Student Name</th><th>Preferred TA</th><th>Waiting Time</th><th>Completed</th></tr>"+formattedQueue;
}


function enterQueue() {
    let name = document.getElementById("name").value;
    let ta = document.getElementById("ta").value
    let sendable = name+","+ta
    socket.emit("enter_queue", sendable);
    document.getElementById("name").value = "";
    document.getElementById("ta").value = "";
}


//Made changes in the readyToHelp() function as it now sends the rowIndex of the table to delete once the student completes his/her OH. x is the rowIndex

function readyToHelp(x) {
    socket.emit("ready_for_student", (x.rowIndex - 1));
}
