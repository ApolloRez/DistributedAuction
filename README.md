Distributed Auctions
============
CS 351 Project 5 - Distributed Auction

Introduction
============
This project is focused on auctions between Auction Houses and Agents with
the Bank as a mediator. Each Auction House registers with the Bank,receives
its bank ID, and begins selling. Likewise, each Agent registers with the Bank, 
receives its bank ID, and deposits money. Agents who registered receive
a list of available Auction Houses from the Bank, and the Agent connects
with them. An Agent can bid on an Item for sale at the Auction House.
The Auction House rejects/accepts the bid based on whether the Agent's
bank account can afford the bid. Once a bid is accepted, neither Agent
nor Auction House can exit. The Agent is notified if an Item they bid
on was outbid by someone else, or if they won the Item. The Agent then
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

AgentBackup
----
1. Click on far file and an interface should pop up.
2. Enter ip address and port number of Bank
3. Press "connect" and the Agent will attempt to connect to the Bank.
4. After connecting, the Agent will immediately desposit 200 and get a list of currently 
   connected Auction Houses from the Bank. It will then connect to those Auction Houses.
5. If an Auction House registers after the Agent received the list, press "refresh" 
   so that the Agent can check again if there are any new Auction Houses.
6. On the right side of the interface, a button(or buttons) will appear. Each button
   represents one Auction House. The string of characters on the button is the first 
   four characters of that Auction House's ID from the Bank.
7. Press on any of the Auction House buttons, and a interface will pop up in the center.
8. The interface will show the list of items currently for sale at that specific Auction
   House. For each item there will be a TextField and button to the right of the item name.
   Each item will have a timer until the item stops being for sale.
9. Enter the amount you want to bid for an item in that item's TextField. Then press "bid"
   and the Agent will bid on that item.
10. The Auction House will send a rejection/acceptance notification that will appear in the
    bottom portion of the interface. Note, you cannot exit the program will ANY bid is in
    progress.
11. The Auction House will notify you if you won the item or another Agent outbid you.
12. Press "shutdown" or exit the program when you're finished. Note, if you want you can
    press "connect" again to create another Agent.

Contributions
=============
Steven Chase - I did AuctionHouse package, AuctionMessage in shared, NetInfo
in shared, and the AgentBackup package.  
Magnus Lindland - I did the bank package, and shared.Message class.   


Known Bugs  
==========
* If the Auction House is disconnected from the bank without sending a deregister message,  
all Auction Houses that share said computers IP address will be removed from the known  
Auction Houses of the bank. A client can no longer connect to any of these auction houses unless  
they reregister with the bank.

Authors
=======
Steven Chase  
Magnus Lindland  
Aidan O'Hara