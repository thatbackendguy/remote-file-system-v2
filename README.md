# Remote File System v2
created by: https://github.com/yashpra1010/

```
remote-file-system/
└─ src/main/java/
   └── com/
       └── remoteFS/
           ├── server/
           │   ├── Server.java                     // Main class to start the server
           │   ├── ServerConfig.java               // Configuration class for server setup
           │   ├── controller/
           │   │   └── FileSystem.java             // Functions for File System Managment
           │   │   └── User.java                   // Functions for User Managment
           │   └── handler/
           │       ├── ClientHandler.java          // Thread/process to handle client requests
           │       └── ClientConnection.java       // Represents a client connection
           │
           └── client/
               ├── Client.java                     // Main class to start the client
               ├── ClientConfig.java               // Configuration class for client setup
               ├── ui/
               │   └── FileManager.java
               │   └── UserAuthentication.java
               └── handler/
                   ├── ServerConnection.java       // Handles client requests and communication
                   └── FileSystemClient.java       // Manages file system requests
                   └── UserHandlerClient.java      // Manages user level requests
```
