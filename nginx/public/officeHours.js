const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
//because json can do null.as[String]
if (sessionStorage.getItem("username") == null) {
    sessionStorage.setItem("username", "null")
    console.log("happening")
}
//receives events sent by server to do with logging in and creating accounts
socket.on("successful login", function(data){
    sessionStorage.setItem("username", data)
    sessionStorage.setItem("loggedin", true)
    changePage()
});
socket.on("bad user", function() {
  alert("please enter a valid username")
});
socket.on("bad pass", function(){
    alert("error wrong password")
});
socket.on("DNE", function(){
    console.log("DNE")
    alert("error account does not exists please create an account")
    createPage()
});
socket.on("error", function(){
    console.log("error")
    alert("an error occurred")
});
socket.on("successCreate", function(){
    alert("account successfully created please login")
    loginPage()
});
socket.on("failedCreate", function() {
    alert("account already exist please login")
});
socket.on("invalid", function() {
  alert("invalid credentials redirecting to login page")
  loginPage()
});
socket.on("valid", function(){
    let html = '<br/>\n' +
        '\n' +
        '<h3 id="message">Welcome!</h3>\n' +
        '\n' +
        '<p>' + sessionStorage.getItem("username") + '</p>\n' +
        '<button onclick="enterQueue();">Enter Queue</button>\n' +
        '<br/><br/>\n' +
        '\n' +
        '\n' +
        '\n' +
        '<button onclick="readyToHelp();">TA Ready to Help</button>\n' +
        '\n' +
        '<br/>\n' +
        '\n' +
        '<div id="queue"></div>'
    document.getElementById("queueSet").innerHTML = html
});

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
    socket.emit("enter_queue", sessionStorage.getItem("username"));
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
//logs you in if information is correct
function login() {
    let usernameRef = document.getElementById("username")
    let passwordRef = document.getElementById("password")
    let username = usernameRef.value
    let password = passwordRef.value
    console.log(username)
    usernameRef.value = ""
    passwordRef.value = ""
    console.log(username)
    console.log(password)
    socket.emit("login", JSON.stringify({"username": username, "password": password}));
}
//creates an account if possible
function createAcc() {
    let usernameRef = document.getElementById("username")
    let passwordRef1 = document.getElementById("password1")
    let passwordRef2 = document.getElementById("password2")
    let username = usernameRef.value
    let password1 = passwordRef1.value
    let password2 = passwordRef2.value

    if (password1 === password2) {
        socket.emit("register", JSON.stringify({"username": username, "password": password1}));
        usernameRef.value = ""
        passwordRef1.value = ""
        passwordRef2.value = ""
    }
    else {
        passwordRef1.innerHTML = ""
        passwordRef2.innerHTML = ""
    }


}
//sends you to the create account page
function createPage() {
    sessionStorage.setItem("username", "null")
    let html =
        '<label For="username">Username</label>'+
    '<input type="text" id="username"/>'+
    '<br>'+
    '    <label For="password1">Password</label>'+
    '    <input type="password" id="password1"/><br>'+
    '    <label For="password2">Password double check</label>'+
    '    <input type="password" id="password2"/><br>'+
    '    <button onClick="createAcc();">Create Account</button>'+
    '    <button onClick="loginPage();">Login</button> '

    document.getElementById("queueSet").innerHTML = html
}
//send you to the main page
function changePage() {
    socket.emit("challenge", JSON.stringify({"username": sessionStorage.getItem("username")}))
}
//sends you to the login page
function loginPage() {
    sessionStorage.setItem("username", "null")
    let html =
    '<label for="username">Username</label>' +
        '<input type="text" id="username"/> <br>' +
        '<label for="password">Password</label>'+
        '<input type="password" id="password"/> <br>' +
        '<button onclick="login();">Login</button>'
        '<button onclick="createPage();">Create Account</button>'
    document.getElementById("queueSet").innerHTML = html
}
//w3schools.org reference for html and javascript