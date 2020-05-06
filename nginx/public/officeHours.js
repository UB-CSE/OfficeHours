const socket = io.connect("http://localhost:8080", {transports: ['websocket']});
google.charts.load('current', {'packages':['corechart']});
google.charts.load('current', {'packages':['table']});

socket.on('queue', displayQueue);
socket.on('message', displayMessageTA);
socket.on('message2', displayMessageStu);
socket.on('alert', alertWindow);
socket.on('done', studentDone);
socket.on('count', counter);
socket.on('valid_Check',displayTA);
socket.on("invalid",displayInvalid);
socket.on("showTable",getTableData)
socket.on("showTopicPie",getTopicPieData);
socket.on("showSubtopicPie",getSubtopicPieData);

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
let username=""

var data=[]
var data2=[]
var table=[]
var statdisplay=false

function displayMessageTA(newMessage) {
    username=newMessage
    var studentValues=newMessage.split("#")
    document.getElementById("currentStudent").innerHTML = "<h2  id=\"currentStudentName\" style=\"color:green\" value=newMessage>"+"You are now helping "+studentValues[0]+"</h2>\n";
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
            var studentValues=student['username'].split("#")
            formattedQueue += "<li>" + studentValues[0] + " ~ " + student['topic'] + " / " + student['subtopic'] + "</li>" + "<br/>"
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
    document.getElementById("subtitle").innerHTML="Enter your Username and select what you need assistance in";

    let selections="<label>Username:</label>"+
        "        <input type=\"text\" id=\"name\"/><br/>" +
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

    document.getElementById("HelpInfo").innerHTML=""

    let TAname=document.getElementById("username").value
    document.getElementById("optionButtons").innerHTML="";
    document.getElementById("subtitle").innerText="You are viewing the student queue";

    let format="<div class=\"optionButton\" onclick=\"readyToHelp();\"> Next Student </div>\n";
    let format2="<div class=\"optionButton\" onclick=\"doneHelping();\"> Done Helping </div>\n<br/>\n";


    document.getElementById("optionButtons").innerHTML=format+format2;

    let queueFormat="<div id=\"queue\" class=\"queue\"></div>";
    document.getElementById("queueSection").innerHTML=queueFormat;

    let statInfo="<div id=\'statMain\' class=\"optionButton2\" onclick=\"getStatsOption();\"> Statistics </div>\n"+
        "<div id=\"statOption\"></div>"

    document.getElementById("statistics").innerHTML=statInfo;


    socket.emit("display_TA",TAname)
}

function readyToHelp() {


    if(document.getElementById("currentStudent").innerHTML.length==0){
        socket.emit("ready_for_student");
    }
}
function doneHelping() {
    if(document.getElementById("currentStudent").innerHTML.length>0) {
        document.getElementById("currentStudent").innerHTML = "";
        socket.emit("done_helping",username)
    }
}
function alertWindow() {
    window.alert("It's your turn! Have your question prepared");
}

function studentDone() {
    document.getElementById('currentStudent').innerHTML="<h2 style=\"color:green\">Thanks for attending Office Hours! See you next time :) </h2>\n";
}
function counter(count) {
    let values=JSON.parse(count)
    let student=values["student"]
    let ta=values["ta"]

    document.getElementById("Counter").innerHTML="TA Count: "+ta+" Student Count: "+student
}
function firstcounter(){
    socket.emit("first_count")
}

function displayLogin() {
    document.getElementById("optionButtons").innerHTML="";
    document.getElementById("subtitle").innerHTML="Enter your Username and Password";

    let selections="<label>Username:</label>\n"+
        "<input type=\"text\" id=\"username\" name=\"username\">\n"+
        "<label for=\"pass\">Password:</label>\n"+
        "<input type=\"password\" id=\"pass\" name=\"password\" required>\n<br/>"+
        "<button id=\"login\" onclick=\"checkLogin();\">Login</button>"

    document.getElementById("optionButtons").innerHTML=selections;
}
function checkLogin() {
    let name=document.getElementById("username").value
    let password=document.getElementById("pass").value
    let message={
        "username":name,
        "password":password
    }
    message=JSON.stringify(message)
    socket.emit("check_login",message)
}
function displayInvalid() {
    let Error="<br/><p style=\"color:red\" class=\"center\" >Invalid Username or Password!</p>"
    document.getElementById("HelpInfo").innerHTML=Error
}

function getStatsOption() {
    if(statdisplay){
        document.getElementById("statMain").innerText="Statistics"
        document.getElementById("statOption").innerHTML=""
        statdisplay=false
    }
    else {
        document.getElementById("statMain").innerText="Done"

        let selections="<label>What Statistics would you like:</label><br/>"+
            "        <select id=\"statsValue\">\n" +
            "        <option value=\"TaHelpCount\">TA Help Count</option>\n" +
            "        <option value=\"statTopic\">Topic Stats</option>\n" +
            "        <option value=\"statSubtopic\">Subtopic Stats</option>\n" +
            "        </select><br/>" +
            "        <button id=\"buttonNext\" class= \"addSpace\" onclick=\"getInfo();\">Show</button>"+
            "        <div id=\"displayinfo\" align='center' class= \"addSpace\" ></div>"

        document.getElementById("statOption").innerHTML=selections;
        statdisplay=true
    }

}
function getInfo() {
    let option=document.getElementById("statsValue").value;
    if(option=="TaHelpCount"){
        socket.emit("get_TA_Stat");
    }
    else{
        socket.emit("get_Student_Stat",option);
    }
}



function getTopicPieData(message) {
    var pasred=JSON.parse(message);
    var tempdata=[];
    var topics= pasred['topics']
    var count= pasred['count']

    tempdata.push(['Subject',"# of times asked"]);

    for( let value of topics){
        var index=topics.indexOf(value);
        var pairedVal=count[index];
        var temp=[value,pairedVal];
        tempdata.push(temp);
    }

    data=tempdata;
    callPie();
}

function getSubtopicPieData(message) {
    var pasred=JSON.parse(message);
    var tempdata=[];
    var subtopics= pasred['subtopics'];
    var count= pasred['count'];
    tempdata.push(['Subject',"# of times asked"]);

    for( let value of subtopics){
        var index=subtopics.indexOf(value);
        var pairedVal=count[index];
        var temp=[value,pairedVal];
        tempdata.push(temp);
    }

    data2=tempdata;
    callPie2();
}
function getTableData(message) {
    var pasred=JSON.parse(message);
    var tempdata=[];
    var names= pasred['names'];
    var count= pasred['count'];

    for( let value of names){
        var index=names.indexOf(value);
        var pairedVal=count[index];
        var temp=[value,pairedVal];
        tempdata.push(temp);
    }
    table=tempdata;
    callTable();
}
function callPie() {
    google.charts.setOnLoadCallback(displayPie);

}
function callPie2() {
    google.charts.setOnLoadCallback(displayPie2);

}
function callTable() {
    google.charts.setOnLoadCallback(displayTable);

}
function displayPie() {
    document.getElementById('displayinfo').innerHTML=""
    var chartload = google.visualization.arrayToDataTable(data);
    var options = {'title':'What Students needed help with:', 'width':550, 'height':500, 'backgroundColor': '#cccccc'};
    var chart = new google.visualization.PieChart(document.getElementById('displayinfo'));
    chart.draw(chartload, options);
}
function displayPie2() {
    document.getElementById('displayinfo').innerHTML=""
    var chartload = google.visualization.arrayToDataTable(data2);
    var options = {'title':'What Students needed help with:', 'width':550, 'height':500,'backgroundColor': '#cccccc'};
    var chart = new google.visualization.PieChart(document.getElementById('displayinfo'));
    chart.draw(chartload, options);
}
function displayTable() {
    var data = new google.visualization.DataTable();
    data.addColumn('string', 'Name');
    data.addColumn('number', '# of Students Helped');
    data.addRows(table)
    var table2 = new google.visualization.Table(document.getElementById('displayinfo'));

    table2.draw(data, {'showRowNumber': 'true', 'width': '50%', 'height': '100%'});
}

