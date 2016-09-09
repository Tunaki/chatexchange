# Chatexchange

The purpose of this project is to provide a simple API to interact with the chat system on Stack Overflow, and the Stack Exchange network.

## How to use

Start by creating a `StackExchangeClient`; this is the class used to authenticate with Stack Overflow. Give it the e-mail address you want to connect to chat with, along with the password:

    StackExchangeClient client = new StackExchangeClient(emailAddress, password);

With the client, you can then join any chat room by invoking the `joinRoom` method, taking as first parameter the host of the chat server and, as second parameter, the id of the room to join.

    Room room = client.joinRoom(ChatHost.STACK_OVERFLOW, roomId);