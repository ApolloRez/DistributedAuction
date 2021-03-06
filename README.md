Distributed Auctions
============
CS 351 Project 5 - Distributed Auction

Introduction
============
This project is focused on auctions between Auction Houses and Agents with
the Bank as a mediator. Each Auction House registers with the Bank,receives
its bank ID, and begins selling. Likewise, each agent registers with the Bank,
receives its bank ID, and deposits money. Agents who registered receive
a list of available Auction Houses from the Bank, and the agent connects
with them. An agent can bid on an Item for sale at the Auction House.
The Auction House rejects/accepts the bid based on whether the agent's
bank account can afford the bid. Once a bid is accepted, neither agent
nor Auction House can exit. The agent is notified if an Item they bid
on was outbid by someone else, or if they won the Item. The agent then
requests the Bank to transfer the funds from its account to the Auction 
House's account.

Prerequisites
=============
**Java 8 JRE**

Functionality
=============

Auction House  
-------------  

1. Click on the jar file and an interface should pop up.
2. Enter the ip address and port number of Bank.
3. Enter port number you want for the Auction House Server.  
3. Press "connect" and the Auction House will connect to the Bank 
   and create its own server.
4. A display will pop up showing the current Items for sale, their time left,
   ID of Auction House bank account, its current bank account balance, and a 
   log displaying messages between the Bank and Agents.
5. Press "Shutdown" to shutdown the Auction House. Note, you cannot click 
   "shutdown" or exit the program while a bid is in progress.
6. After shutting down, if you want you can open a new Auction House
   by pressing "connect" again.

Bank  
----

1. The bank jar will take one argument, the port number for the server. If no argument  
is entered, a default port of 4444 is used.  
2. When the program opens, clients and auction houses can connect using the IP address  
of the computer, this address is not provided in the program.  
3. From this point, various messages are displayed to the GUI.  
a) Connections and drops.  
b) Message commands, ie: register, deregister, transfer_funds, deposit, etc.  
4. Once finished, shut down the program using the window close.


Contributions
=============
Steven Chase - I did auctionhouse package, and AuctionMessage in shared.
Magnus Lindland - I did the bank package, and shared.Message class.   


Known Bugs  
==========
* If the Auction House is disconnected from the bank without sending a deregister message,  
all Auction Houses that share said computers IP address will be removed from the known  
Auction Houses of the bank. A client can no longer connect to any of these auction houses unless  
they reregister with the bank.
* The Items for sale at an Auction House will intially be in sync in terms of time left. Over 
time, the timers for each item will go out of sync without agent intervention


Authors
=======
Steven Chase  
Magnus Lindland  
Aidan O'Hara