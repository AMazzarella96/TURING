# Reti Project a.y. 2018/19

## TURING - disTribUted collaboRative edItiNG

The project aims to implement a system for collaborative document editing, in which
which users have the opportunity to contribute to the editing of a document by working on the sections
into which the latter is divided. The system offers the user the following features:
- ***Registration for the service**: each user registers for TURING by providing a unique username
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
- `UserGUI`: main frame, implements all the mechanisms that allow the user to
interact with the system and manages all related frames:
  - `CreateGUI`: frame for creating documents;
  - `InviteGUI`: frame for invitations;
  - `ShowDocGUI`: frame for choosing documents/sections to display. Once the
made the choice, a frame (DocVisualizer) is created for on-screen printing.
  - `EditDocGUI`: frame for choosing the section to be edited. Following confirmation
the frame for the actual editing is created (EditingGUI), where the area
where the text of the document is displayed and the chat;

Maintains an ArrayList of all frames that are opened to manage their closure on
logout;
