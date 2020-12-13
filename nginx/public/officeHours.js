const socket = io.connect("http://localhost:8080", {transports: ['websocket']});

socket.on('queue', displayQueue);
socket.on('message', displayMessage);
socket.on('Waiting_music', displayMusic)

function displayMusic(newMusic){
    document.getElementById("music").innerHTML = newMusic;
}

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

function Music(){
    var SongPlaylist = [];
    SongPlaylist[0] = "https://www.youtube.com/watch?v=3FsrPEUt2Dg";
    SongPlaylist[1] = "https://www.youtube.com/watch?v=IxuThNgl3YA";
    SongPlaylist[2] = "https://www.youtube.com/watch?v=rblt2EtFfC4";
    SongPlaylist[3] = "https://www.youtube.com/watch?v=DtVBCG6ThDk";
    SongPlaylist[4] = "https://www.youtube.com/watch?v=bEea624OBzM";
    SongPlaylist[5] = "https://www.youtube.com/watch?v=5XcKBmdfpWs";
    SongPlaylist[6] = "https://www.youtube.com/watch?v=EFMD7Usflbg";
    SongPlaylist[7] = "https://www.youtube.com/watch?v=wccRif2DaGs";
    SongPlaylist[8] = "https://www.youtube.com/watch?v=e5MAg_yWsq8";
    SongPlaylist[9] = "https://www.youtube.com/watch?v=SyNt5zm3U_M";
    SongPlaylist[10] = "https://www.youtube.com/watch?v=gxEPV4kolz0";

    window.open(SongPlaylist[Math.floor(Math.random() * SongPlaylist.length)], '_blank')
    socket.emit("music")
}
