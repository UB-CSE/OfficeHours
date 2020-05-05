const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
}

function displayQueue(queueJSON) {
    const queue = JSON.parse(queueJSON);
    document.getElementById("studentHelpedTable").innerHTML = "<tr>\n" +
        "        <th>Name</th>\n" +
        "        <th>Time In</th>\n" +
        "    </tr>";
    for (const student of queue) {
        let queueTable = document.getElementById("studentHelpedTable");
        let row = queueTable.insertRow(1);
        let firstCell = row.insertCell(0);
        let secondCell = row.insertCell(1);
        firstCell.innerHTML = student['username'];
        secondCell.innerHTML = student['timestamp'];
    }
}


function enterQueue() {
    let name = document.getElementById("name").value;
    socket.emit("enter_queue", name);
    document.getElementById("name").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
