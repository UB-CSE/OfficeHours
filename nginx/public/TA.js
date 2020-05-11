const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);

function enter_TA()
{
  socket.emit("enter_TA");
}

enter_TA();

function displayQueue(queueJSON) {
  const queue = JSON.parse(queueJSON);
  let formattedQueue = "";
  for (const student of queue) {
    formattedQueue += student['username'] + " has been waiting since " + student['timestamp'] + "<br/>"
  }
  document.getElementById("queue").innerHTML = formattedQueue;
}

function readyToHelp() {
  socket.emit("ready_for_student");
}

function displayMessage(message) {
  document.getElementById("message").innerHTML = message;
}