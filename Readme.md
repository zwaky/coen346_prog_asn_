## Multithreading
:white_check_mark: Basic implementation of multithreading using Threads

:white_check_mark: Create a ClientServiceThread object to handle post and get requests

:white_check_mark: Create a list of all the connected clients (ClientServiceThread objects)



## Fund Transfer Functionality
:white_check_mark: Create an Account class

:white_check_mark: Go through the account.txt file and create an account object for each entry

:white_check_mark: Create a list of accounts

Inside the ClientServiceThread class, create a transferFund() method that is called inside handlePostRequest()


## Protections against Race Conditions and Deadlocks

:white_check_mark: Need to remove the "synchronized" parts. The assignment says we cannot use them. Implement using Reentrantlock

:white_check_mark: Add a lock around editing the ClientServiceThread list (creation and removal)

Add a lock around the transfering of funds






# Programming Assignment #2 : A Web Server for Bank Transfers

In this project, you will be developing a concurrent web server to handle transfers between bank accounts. We are providing a simple Java Server that operates with a single thread. Your task is to enhance this server by making it multithreaded and adding functionality to handle fund transfers between accounts. You will also need to identify and address potential synchronization problems and deadlock scenarios.


### Background on Web Servers

Web servers are software programs that handle requests from clients (such as web browsers) and send back responses containing web pages, files, or other resources. Traditional web browsers and web servers communicate through a text-based protocol known as HTTP, which stands for Hypertext Transfer Protocol. Here's how the interaction typically unfolds: A web browser initiates a connection to a web server and sends an HTTP request for specific content. The web server provides the requested content and terminates the connection. The browser processes and presents the received content on the user's screen.
Web servers are identified by both the machine they run on and the port they use for communication. Ports allow multiple independent network activities to happen simultaneously on a single machine. For example, a web server might handle web traffic on port 80, while a mail server handles emails on port 25.
Although web servers typically use port 80 for HTTP traffic, in some cases, a different port may be used (as in this project).

### Your tasks:

#### Multithreading:

The current implementation of the server is single-threaded. This means that each new client needs to wait until the previous client has finished for its request to be served. To help you see this, we have provided a SimpleWebClient class that waits for one minute before sending a request to the server. Run the server, then run the client and then open the webpage and see what happens.
To solve this problem, implement a multithreaded approach to allow the server to handle multiple client connections concurrently. You will need to detail your strategy (classes used, when a new thread is created, when it is started, what task does each thread handle) in the report.
To evaluate you, we might create thousands of clients, your server should support them.


#### Fund Transfer Functionality:

Extend the server to handle fund transfers between accounts. Clients should be able to submit the form with details such as source account, source value, destination account, and destination value. The server should accurately process these transfers while maintaining data integrity. For this, once the server is initialized, you should create the accounts provided in simple file in which each line represents an account as in the following example:

Account id, balance 
123, 4000 
321, 5000 
432, 2000


#### Synchronization and Deadlock Prevention

As you implement multithreading, it's crucial to be aware of synchronization issues. When multiple threads access shared data concurrently, it can lead to race conditions and data corruption. Identify critical sections in your code where synchronization is required to prevent such issues. Additionally, analyze the code for potential deadlock scenarios and implement strategies to prevent and handle them.
Analyze the code for potential deadlock situations, especially when handling transfers, and implement strategies to prevent and handle such scenarios.
You may use Java’s synchronization tools except the synchronized keyword.


### Deliverables:

All deliverables are submitted via Moodle. You should submit two files: a zip file of the code and a pdf file of the report. File names should be in the format <SID1>_<SID2>_assignment2_<type>
Where SID corresponds to the student id of each member of the group and type is either code of report.


#### Multithreaded Web Server Code: 
Provide a Java codebase for the enhanced web server with multithreading and fund transfer handling. Submit a zip file. Make sure to remove any compiled code.


#### Design Report: 
Offer a report detailing your multithreading strategy, your synchronization strategies implemented and how they address potential issues. Include any testing or scenarios you used to validate the correctness of your implementation.
