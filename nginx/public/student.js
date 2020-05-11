const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('message', displayMessage);
socket.on('name_already_used', name_already_used);
socket.on('name_valid', name_valid);
socket.on('position', queue_position);

function displayMessage(message) {
  document.getElementById("message").innerHTML = message;
}

function enterQueue() {
  let name = document.getElementById("name").value;
  socket.emit("enter_queue", name);
}

function name_valid() {
  document.getElementById("enterName").innerHTML = "";
}

function name_already_used() {
  alert("The name you chose is already being used!");
}

function queue_position(positionJSON) {
  const position = JSON.parse(positionJSON);
  document.getElementById("message").innerHTML = "You are #" + position['position'] + " in line";
}
