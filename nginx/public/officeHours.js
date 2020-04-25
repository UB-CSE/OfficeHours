const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);

let lecture=Array("Unit Testing","Test Factoring","Voting Class","Reference Referee","Reference Trader","Reference Batteries",
    "Inheritance Batteries","Polymorphic Electronics","JSON Store","Player States","TV States","Car States",
    "Function and Type Parameters","Recursive Fibonacci","Recursive Factoring","Average in Range","Immutable Point",
    "Linked-List Reduce","Backlog","Expression Trees","BST toList","Graph Connections","Graph Distance",
    "Actors","Banking Actor","Traffic Actors","Websocket","Echo Server","DM Server")

let hw=Array("Physics Engine","Rhyming Dictionary","Calculator","Microwave","Genetic Algorithm","Recommendations","Decision Tree","Maze Solver","Clicker")
let appObj=Array("Program Execution","Object-Oriented Programming","Functional Programming","Data Structures & Algorithms","Event-Based Architectures")
let lab=Array("Program Execution","Object-Oriented Programming","Functional Programming","Data Structures & Algorithms","Event-Based Architectures")


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

function studentSide() {
    document.getElementById("test").innerHTML=""
    document.getElementById("subtile").innerHTML="Select what you need assistance in"

    let formatSelections=""

}


function enterQueue() {
    let name = document.getElementById("name").value;
    socket.emit("enter_queue", name);
    document.getElementById("name").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
