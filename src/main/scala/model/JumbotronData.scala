package model

import com.corundumstudio.socketio.SocketIOClient

object JumbotronData {

  var clients: List[SocketIOClient] = Nil // Keep track of jumbotron clients

}
