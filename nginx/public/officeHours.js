const socket = io.connect("http://localhost:8080", {transports: ['websocket']});
const input = document.getElementById("name-input");

socket.on('queue', displayQueue);
socket.on('message', displayMessage);

function displayQueue(queueJSON) {
    //Parses the text into a JSON
    const queue = JSON.parse(queueJSON);
    //Get the old queue that is to be replaced
    const oldQueue = document.getElementById("queue");
    //Creates a new dom element to replace the old queue
    const newQueue = document.createElement("div");
    newQueue.id = "queue";
    //Iterated through all of the people in the queue
    for (let i = 0; i < queue.length; i++) {
        //Creates a new queue element to display information about each people in queue
        const queueTemplate = document.createElement("div");
        queueTemplate.classList.add("user");

        //Place in queue
        const placeDiv = document.createElement("div");
        placeDiv.classList.add("place");
        placeDiv.innerText = (i + 1).toString();

        //Info div
        const infoDiv = document.createElement("div");
        infoDiv.classList.add("info");

        //Name div
        const nameSpan = document.createElement("span");
        nameSpan.classList.add("name");
        nameSpan.innerText = queue[i].username;

        //Text div
        const TextDiv = document.createElement("div");
        TextDiv.innerText = " joined the queue at ";

        //Time div
        const timeSpan = document.createElement("time");
        timeSpan.classList.add("time");
        //Convert the unix time into a Date object
        const timestamp = new Date(queue[i].timestamp);
        timeSpan.innerText = `${timestamp.getHours()}:${timestamp.getSeconds() < 10 ? "0" : ""}${timestamp.getSeconds()}`;

        //Append all three elements to the parent div
        infoDiv.appendChild(nameSpan);
        infoDiv.appendChild(TextDiv);
        infoDiv.appendChild(timeSpan);

        //Append both children to queueTemplate
        queueTemplate.appendChild(placeDiv);
        queueTemplate.appendChild(infoDiv);

        //Append the queue template to the new queue
        newQueue.appendChild(queueTemplate)

        // <div class="user">
        //     <div class="place"> place </div>
        //     <div class="info">
        //         <span class="name"> name </span>
        //         <div> joined the queue at </div>
        //         <span class="time"> time </span>
        //     </div>
        // </div>
    }
    //Replace the old queue with the new one
    oldQueue.parentNode.replaceChild(newQueue, oldQueue);
}

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
}

function enterQueue() {
    let name = document.getElementById("name").value;
    socket.emit("enter_queue", name);
    document.getElementById("name").value = "";
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
