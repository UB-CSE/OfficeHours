const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

var totalPPL = 0
var waitTime = []

socket.on('queue', displayQueue);
socket.on('message', displayMessage);

function displayMessage(newMessage) {
    document.getElementById("message").innerHTML = newMessage;
}

function displayQueue(queueJSON) {
    const queue = JSON.parse(queueJSON);
    if (queue.length === 0) {
        document.getElementById("queue").innerHTML = "OH MY GOODNESS!!!! WOW!!!! HOLY MACKEREL!!!"
                                                            + "DAAANNGG!! THE TA'S HAVE GOTTEN THROUGH EVERY STUDENT"
                                                            + "<br/>" + totalPPL + " people helped thus far!";
    } else {
        let formattedQueue = "";
        for (const student of queue) {
                formattedQueue += student['username'] + " has been waiting since " + student['timestamp'] + "<br/>"
        }
        formattedQueue += queue.length + " people in queue." + "<br/>" + totalPPL + " people helped thus far!"
                       //+ "<br/>" + "Average Wait-Time: " + TIMECHANGE(queue).toString()

        document.getElementById("queue").innerHTML = formattedQueue;
    }
}
// function TIMECHANGE(queue){
//     let TIME = 0
//     let TIME2 = 0
//     let total = 0
//
//     if(!(queue.length === 0)) {
//         for (let i = 0; i < queue.length - 1; i++) {
//             TIME = queue[i]['timestamp'];
//             TIME2 = queue[i + 1]['timestamp'];
//             waitTime += (TIME2 - TIME);
//         }
//
//         for (let t of waitTime) {
//             total += t;
//         }
//         return total / waitTime.length;
//     }
//     else{return 0}
// }

function enterQueue() {
    let name = document.getElementById("name").value;
    socket.emit("enter_queue", name);
    document.getElementById("name").value = ""
}

function readyToHelp() {
    socket.emit("ready_for_student");
    totalPPL += 1;
}

