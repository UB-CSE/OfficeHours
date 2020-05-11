const socket = io.connect({transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
}

function displayQueue(queueJSON) {
    const queue = JSON.parse(queueJSON);
    let formattedQueue = "<div class=\"details\"> <p>Good job! No students are currently in queue!</p> </div>"
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

var options = {
  bottom: '64px',              // default: '32px'
  right: '32px',              // default: '32px'
  left: 'unset',                // default: 'unset'
  time: '0.5s',                // default: '0.3s'
  mixColor: '#f2f2f2',            // default: '#fff'
  backgroundColor: '#fff',     // default: '#fff'
  buttonColorDark: '#100f2c',  // default: '#100f2c'
  buttonColorLight: '#f2f2f2',    // default: '#fff'
  saveInCookies: true,         // default: true,
  label: '🌓',                 // default: ''
  autoMatchOsTheme: true       // default: true
}

const darkmode = new Darkmode(options);
darkmode.showWidget();
