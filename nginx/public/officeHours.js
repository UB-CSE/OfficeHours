const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);

//recieves events sent by server to do with logging in and creating accounts
socket.on("successful login", function(data){
    sessionStorage.setItem("username", data)
    sessionStorage.setItem("loggedin", true)
    changePage()
})
socket.on("bad pass", function(){
    alert("error wrong password")
})
socket.on("DNE", function(){
    console.log("DNE")
    alert("error account does not exists please create an account")
    createPage()
})
socket.on("error", function(){
    console.log("error")
    alert("error failed to login")
})
socket.on("successCreate", function(){
    alert("account successfully created please login")
    loginPage()
})
socket.on("failedCreate", function() {
    alert("account already exist please login")
})

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
    let name = document.getElementById("name").value;
    socket.emit("enter_queue", name);
    document.getElementById("name").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
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

function createPage() {
    window.location.href = "createacc.html"
}

function changePage() {
    window.location.href = "index.html"
}

function loginPage() {
    window.location.href = "loginpage.html"
}
//w3schools.org reference for html and javascript