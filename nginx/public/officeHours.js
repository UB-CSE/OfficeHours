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

let topicMap={
    "lecture":lecture,
    "hw":hw,
    "appObj":appObj,
    "lab":lab
}


function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
}

function displayQueue(queueJSON) {

    document.getElementById("subtile").innerHTML="You are in queue now";

    const queue = JSON.parse(queueJSON);
    let formattedQueue = "";
    for (const student of queue) {
        formattedQueue += student['username'] + " has been waiting since " + student['timestamp'] + "<br/>"
    }
    document.getElementById("queue").innerHTML = formattedQueue;
}

function displayNameTopic() {
    document.getElementById("optionButtons").innerHTML="";
    document.getElementById("subtile").innerHTML="Enter your name and select what you need assistance in";

    let selections="<input type=\"text\" id=\"name\"/><br/>" +
        "        <select id=\"topic\">\n" +
        "        <option value=\"lecture\">Lecture</option>\n" +
        "        <option value=\"hw\">Homework</option>\n" +
        "        <option value=\"appObj\">Application Obj</option>\n" +
        "        <option value=\"lab\">Lab</option>\n" +
        "        </select><br/>" +
        "        <div id=\"topic2\">"+
        "        <button id=\"buttonNext\" onclick=\"displaySubtopic();\">Next</button>"+
        "        </div>\n"

    document.getElementById("optionButtons").innerHTML=selections;
}

function displaySubtopic() {
    document.getElementById("topic2").innerHTML="";

    let subtopic=topicMap[document.getElementById("topic").value]

    var selections="<select id=\"subtopic2\">\n"

    for(const element of subtopic){
        selections+="<option value="+element+">"+element+"</option>\n"
    }
    selections+= "</select><br/>"+"<button id=\"finish\" onclick=\"enterQueue();\">Finsh</button>"

    document.getElementById("topic2").innerHTML=selections;
}


function enterQueue() {
    let name = document.getElementById("name").value;
    let topic = document.getElementById("topic").value;
    let subtopic = document.getElementById("subtopic2").value;

    var data={
        "username":name,
        "topic":topic,
        "subtopic":subtopic,
    }
    document.getElementById("topic2").innerHTML = "";
    document.getElementById("optionButtons").innerHTML = "";
    socket.emit("enter_queue", JSON.stringify(data));

}

function readyToHelp() {
    socket.emit("ready_for_student");
}
