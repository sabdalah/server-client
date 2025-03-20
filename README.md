# server-client

Just a simple guess the number game with GUI in java using server-client socket programming

This is a very simple Guess the word Game with a Client and Server structure.
The Client is a GUI that interacts with the server responsible for handling the logic of the
Game and generating the random number. The server will Respond to the client if it is too high, too low, invalid entry OR if it is a correct GUESS.
To avoid any issues please compile the program following these steps

1. Navigate to the directory with the files in your shell/terminal/bash command
2. Enter: javac Server.java ClientGUI.java
3. Run the Server: java Server
   4 Open a new command line and navigate back to the directory with the compiled clientgui and run the client
   With java ClientGUI
   When the GUI comes up, please enter a username.
4. Repeat step 4 for the second client.
   6 Begin the game.

———————————————
I have found that following these steps in this order performed best. If not there might be some type of synchronization issue as the Server will not know a client is already joined UNTIL after a username is entered, so if you open a client gui and then skip the username and open another one, it may not perform the same way.
