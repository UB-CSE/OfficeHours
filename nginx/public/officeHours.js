const socket = io.connect("http://localhost:8080", {transports: ['websocket']});
//Elements related to submitting name to queue
const form = document.getElementById("name");
const formFailBorder = document.getElementById("name-fail-border");
const formContinueText = document.getElementById("continue-text");
const formContent = document.getElementById("name-input");
//Used to keep track of if the (Not Initialized) tag is removed
const queueHeader = document.getElementById("queue-header-init");
let queueInit = false;

socket.on("connect", () => {
    document.getElementById("server-online").src = "./assets/arrow-green.svg";
});

//Handle the user attempting to join the queue
function handleSubmit(nameElement) {
    try {
        enterQueue(nameElement.value);
        nameElement.value = "";
    } catch (e) {
        //If an error occurs, the border of the input field will turn red for one second
        formFailBorder.style.opacity = "1";
        setTimeout(() => {
            formFailBorder.style.opacity = "0";
        }, 1000);
    }
}

//For [enter] key bind
form.addEventListener("submit", (e) => {
    e.preventDefault();
    handleSubmit(formContent);
});

//For continue text
formContinueText.addEventListener("click", () => {
    handleSubmit(formContent);
});

const taAcceptButton = document.getElementById("ta-accept");
const taAcceptModal = document.getElementById("confirm-modal");
const taAcceptNonModalKeys = document.getElementById("non-confirm-modal");
const taAcceptModalClose = document.getElementById("confirm-modal-close");
let modalOpen = false;

const openModal = () => {
    //Makes modal appear and hides the [ctrl] and +
    modalOpen = true;
    taAcceptModal.style.display = "block";
    taAcceptNonModalKeys.style.display = "none";
}

const closeModal = () => {
    //Opposite of openModal()
    modalOpen = false;
    taAcceptModal.style.display = "none";
    taAcceptNonModalKeys.style.display = "";
}

//Set the modal to display: none when the [x] is clicked
taAcceptModalClose.addEventListener("click", () => {
    closeModal();
});

//Make the modal appear when a user clicks the ta accept button
taAcceptButton.addEventListener("click", () => {
    if (modalOpen) {
        //Send request if modal is already open
        readyToHelp();
        closeModal();
    } else {
        //Open modal otherwise
        openModal();
    }
});

document.addEventListener("keyup", (e) => {
    if (modalOpen && e.code === "Enter") {
        //Sends request if modal is open and user presses [enter]
        readyToHelp();
        closeModal();
    } else if (e.code === "Enter" && e.shiftKey && e.ctrlKey) {
        //Skips the modal if [shift] is held in addition to [ctrl] and [enter]
        readyToHelp();
    } else if (e.code === "Enter" && e.ctrlKey) {
        //Opens modal
        openModal();
    }
});

socket.on('queue', displayQueue);

function displayQueue(queueJSON) {
    if (!queueInit) {
        queueHeader.style.display = "none";
        queueInit = true;
    }
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
        timeSpan.innerText = `${timestamp.getHours()}:${timestamp.getMinutes() < 10 ? "0" : ""}${timestamp.getMinutes()}`;

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

socket.on('message', displayMessage);

function displayMessage(newMessage) {
    console.log(newMessage);
    document.getElementById("message-received").src = "./assets/arrow-green.svg";
    document.getElementById("message-received").style.transform = "rotate(270deg)";
    document.getElementById("message").style.display = "inline-block";
    document.getElementById("message").innerText = newMessage;
}

function enterQueue(name) {
    if (name) {
        socket.emit("enter_queue", name);
    } else {
        throw new Error("You must enter a name to join the queue.")
    }
}

function readyToHelp() {
    socket.emit("ready_for_student");
}
