const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessageTA);
socket.on('message2', displayMessageStu);
socket.on('alert', alertWindow);
socket.on('done', studentDone);




let lecture=Array("Unit-Testing","Test-Factoring","Voting-Class","Reference-Referee","Reference-Trader","Reference-Batteries",
    "Inheritance-Batteries","Polymorphic-Electronics","JSON-Store","Player-States","TV-States","Car-States",
    "Function-and-Type-Parameters","Recursive-Fibonacci","Recursive-Factoring","Average-in-Range","Immutable-Point",
    "Linked-List-Reduce","Backlog","Expression-Trees","BST-toList","Graph-Connections","Graph-Distance",
    "Actors","Banking-Actor","Traffic-Actors","Websocket","Echo-Server","DM-Server")

let hw=Array("Physics-Engine","Rhyming-Dictionary","Calculator","Microwave","Genetic-Algorithm","Recommendations","Decision-Tree","Maze-Solver","Clicker")
let appObj=Array("Program-Execution","Object-Oriented-Programming","Functional-Programming","Data-Structures-&-Algorithms","Event-Based-Architectures")
let lab=Array("Program-Execution","Object-Oriented-Programming","Functional-Programming","Data-Structures-&-Algorithms","Event-Based-Architectures")

let topicMap={
    "Lecture":lecture,
    "Hw":hw,
    "AppObj":appObj,
    "Lab":lab
}


function displayMessageTA(newMessage) {

    document.getElementById("currentStudent").innerHTML = "<h2  id=\"currentStudentName\" style=\"color:green\" value=newMessage>"+"You are now helping "+newMessage+"</h2>\n";
    socket.emit("alert_page",newMessage)
}

function displayMessageStu(newMessage) {
    document.getElementById("currentStudent").innerHTML = "<h2 style=\"color:green\">"+newMessage+"</h2>\n";
}

function displayQueue(queueJSON) {

    document.getElementById("subtitle").innerHTML="You're viewing the queue!";

    const queue = JSON.parse(queueJSON);

    if(queue.length>0) {
        let formattedQueue = "<ol>";
        for (const student of queue) {
            // formattedQueue +="<li>"+ student['username'] + " has been waiting since " + student['timestamp'] +"</li>"+ "<br/>"
            formattedQueue += "<li>" + student['username'] + " ~ " + student['topic'] + " / " + student['subtopic'] + "</li>" + "<br/>"
        }

        formattedQueue += "</ol>"
        document.getElementById("queue").innerHTML = formattedQueue;
    }
    else{
        document.getElementById("queue").innerHTML = "<h2>Empty Queue</h2>\n";
    }

}





function displayNameTopic() {
    document.getElementById("optionButtons").innerHTML="";
    document.getElementById("subtitle").innerHTML="Enter your name and select what you need assistance in";

    let selections="<input type=\"text\" id=\"name\"/><br/>" +
        "        <select id=\"topic\">\n" +
        "        <option value=\"Lecture\">Lecture</option>\n" +
        "        <option value=\"Hw\">Homework</option>\n" +
        "        <option value=\"AppObj\">Application Obj</option>\n" +
        "        <option value=\"Lab\">Lab</option>\n" +
        "        </select><br/>" +
        "        <div id=\"topic2\">"+
        "        <button id=\"buttonNext\" onclick=\"displaySubtopic();\">Next</button>"+
        "        </div>\n";

    document.getElementById("optionButtons").innerHTML=selections;
}

function displaySubtopic() {
    document.getElementById("topic2").innerHTML="";

    let subtopic=topicMap[document.getElementById("topic").value];

    var selections="<select id=\"subtopic2\">\n";

    for(const element of subtopic){
        selections+="<option value="+element+">"+element+"</option>\n"
    }

    selections+= "</select><br/>"+"<button id=\"finish\" onclick=\"enterQueue();\">Finsh</button>";

    document.getElementById("topic2").innerHTML=selections;
}


function enterQueue() {

    var name = document.getElementById("name").value;
    var topic = document.getElementById("topic").value;
    var subtopic = document.getElementById("subtopic2").value;

    if(name.length>0){

        var data={
            "username":name,
            "topic":topic,
            "subtopic":subtopic,
        };

        document.getElementById("topic2").innerHTML = "";
        document.getElementById("optionButtons").innerHTML = "";
        document.getElementById("HelpInfo").innerHTML=""


        let queueFormat="<div id=\"queue\" class=\"queue\"></div>";
        document.getElementById("queueSection").innerHTML=queueFormat;

        socket.emit("enter_queue", JSON.stringify(data));
    }
    else{
        let usernameError="<br/><p style=\"color:red\" class=\"center\" >Enter a username!</p>"
        document.getElementById("HelpInfo").innerHTML=usernameError
    }
}

function displayTA() {

    document.getElementById("optionButtons").innerHTML="";
    document.getElementById("subtitle").innerText="You are viewing the student queue";

    let format="<div class=\"optionButton\" onclick=\"readyToHelp();\"> Next Student </div>\n";
    let format2="<div class=\"optionButton\" onclick=\"doneHelping();\"> Done Helping </div>\n<br/>\n";


    document.getElementById("optionButtons").innerHTML=format+format2;

    let queueFormat="<div id=\"queue\" class=\"queue\"></div>";
    document.getElementById("queueSection").innerHTML=queueFormat;

    socket.emit("display_TA")
}

function readyToHelp() {


    if(document.getElementById("queue").innerHTML.length>23){
        socket.emit("ready_for_student");
    }
}
function doneHelping() {
    if(document.getElementById("currentStudent").innerHTML.length>0) {

        let text=document.getElementById("currentStudentName").innerText;
        let words=text.split(" ")
        let name= words[4]
        document.getElementById("currentStudent").innerHTML = "";
        socket.emit("done_helping",name)
    }
}
function alertWindow() {
    window.alert("It's your turn! Have your question prepared");
}

function studentDone() {
    document.getElementById('currentStudent').innerHTML="<h2 style=\"color:green\">Thanks for attending Office Hours! See you next TIME </h2>\n";
}
