

const socket = io.connect("http://localhost:8080", {transports: ['websocket']});
//const socket = io.connect({transports: ['websocket']});

socket.on('passCode', displayMessage);

function displayMessage(newMessage) {
    document.getElementById("password").innerHTML = "<h3>" + JSON.parse(newMessage) + "</h3>";
}

socket.emit("passCode");