# Reti Project a.y. 2018/19

## TURING - disTribUted collaboRative edItiNG

The project aims to implement a system for collaborative document editing, in which
which users have the opportunity to contribute to the editing of a document by working on the sections
into which the latter is divided. The system offers the user the following features:
- **Registration for the service**: each user registers for TURING by providing a unique username
and a password, which he/she will later use to log in;
- **Document creation**: once the user logs in, he can create documents
by specifying their name and number of sections.
- **Inviting other users**: once created, the owner can proceed by inviting other users to
work on the document by specifying the username and name of the document in question. The
purpose is that only users who have been invited by the
creator.

- **Editing a document**: users begin the document editing phase by specifying its
its name and choosing the section they want to edit, provided that the section has not already been
chosen by another user. TURING provides an interface in which it is possible to
edit the text of the section and communicate, through a chat system, with other users
who are currently editing on the document.
- **Viewing a document**: each user has the ability to view an entire
document or a single section regardless of whether other users are editing
and, if so, the system specifies which sections are actually in editing at the
time of the request.

## Class Overview

The project consists of 12 classes and one interface:
- `Authentication [Interface]`: used to expose methods via RMI; contains methods
used by clients and the server to retrieve information about users, documents, and invitations;
- `MainServer`: Server class in which the NIO selector is implemented;
- `MainClient`: client class that provides for establishing the connection with the server and starting
the first frame of the GUI;
- `UserDB`: implements all the methods that the server calls in response to client requests
and maintains the online set of connected users;
- `Register`: implements the interface, used by the server to retrieve information
related to users; deals with the deserialization of data structures or their
initialization (in case they have not yet been serialized);
- `Request`: used to construct requests made to the server, each of which is organized
as a pair <String, ArrayList<String> >, request and array of parameters respectively;
- `Info`: contains all information about a user, such as password, list of documents
owners, list of documents to which he/she has been invited, and a field of type String that specifies
whether the user is in the editing phase;
- `Docs`: contains all information about a document, such as name, publisher list,
InetAddress for the chat implementation (randomly generated), an array of
booleans representing the status of the sections and the list of chat messages sent previously
previously by other users;
- `GUI`: frame for performing registration/login;
- `UserGUI`: main frame, implements all the mechanisms that allow the user to interact with the system and manages all related frames:
  - `CreateGUI`: frame for creating documents;
  - `InviteGUI`: frame for invitations;
  - `ShowDocGUI`: frame for choosing documents/sections to display. Once the made the choice, a frame (DocVisualizer) is created for on-screen printing.
  - `EditDocGUI`: frame for choosing the section to be edited. Following confirmation the frame for the actual editing is created (EditingGUI), where the area where the text of the document is displayed and the chat;

  Maintains an ArrayList of all frames that are opened to manage their closure on
  logout;

- `NotificationHandler`: Runnable class used to implement the reception of
notifications, sent via UDP;
- `ChatHandler`: Runnable class used to handle chat related to editing a
document;
- `Message`: used to represent chat messages within the system;
represented as a pair of <Username, Message> strings.

## Implementation specifics

The Server was implemented with Java NIO because, being buffer oriented and having the
Non-Blocking mode offered by channels, it provides greater flexibility during the process of
communication with clients. Considering the fact that clients send small data and
therefore do not involve excessive workloads, an implementation according to the
single threaded model, as it lends itself well to such situations.
Methods called by the server in response to requests generally return an integer, which is sent in
response to the client to handle any anomalous situations.
The GUI, on the other hand, was implemented with JavaSwing, which, although dated, provides a
decent range of components and several ways to customize the various frames.
Another point to dwell on is the sending of invitation-related notifications, for which UDP was
used UDP, since it lends itself very well to situations in which messages are sent in a small
size and sporadically. Furthermore, the fact that a notification is lost does not undermine in any
way the integrity of the system, since the document name will still appear in the list of
documents to which the user has been invited.
As for document editing, it was preferred to implement it within the system, to
make up for the problems of mutual exclusion and additional controls resulting from offline editing.

## Auxiliary structures
Turing execution revolves around two main structures used to keep track of the state
of users and documents in the system, implemented as two ConcurrentHashMaps,
users<String, Info> and documents<String, Docs>, respectively.

In users the username is used as the key, as it uniquely identifies a user, and
within Info we then find various information including the password. To facilitate the management
of documents a constraint was imposed on the uniqueness of the name, so that documents can be accessed
using the document name as the key.

The system also provides a serialization mechanism implemented within the class
Register, which involves the two HashMaps to ensure persistence of information between different
executions and correspondence with the physical files created.

For crash management and to prevent a user from trying to log in multiple times with the same account,
another ConcurrentHashMap was defined (in the UserDB class), representing the online set of
users in which each element is a <User, SocketAddress> pair.

## Thread activation cycle
The execution cycle of the program involves the activation of a main thread, that of the Server
which handles the selector, and then a thread for each client. For each user who performs the
login, the thread for managing notifications is then started and in the editing phase also the one for
chat management. The latter is interrupted when the client finishes editing through the
“End Edit” command or when it closes the frame, while the thread for notifications is stopped as soon as the
as soon as the client logs out.
Concurrency management was handled through the use of synchronized methods (as opposed to the
explicit locks that, due to the simplicity of the methods, were unnecessary) as they provide a
greater robustness and eliminate the possibility of running into deadlock situations.
