# Chatexchange

The purpose of this project is to provide a simple API to interact with the chat system on Stack Overflow, and the Stack Exchange network.

## Maven dependency

This library is currently hosted on GitHub. To depend on it, a repository needs to be added:

```xml
<repositories>
  <repository>
    <id>Tunaki-mvn-repo</id>
    <url>https://raw.github.com/Tunaki/chatexchange/mvn-repo/</url>
  </repository>
</repositories>
```

The dependency is:

```xml
<dependency>
  <groupId>fr.tunaki.stackoverflow</groupId>
  <artifactId>chatexchange</artifactId>
  <version>1.0.0</version>
</dependency>
```

## How to use

### Joining a room

Start by creating a `StackExchangeClient`; this is the class used to authenticate with Stack Overflow. Give it the e-mail address you want to connect to chat with, along with the password:

```java
StackExchangeClient client = new StackExchangeClient(emailAddress, password);
```

**The client must be closed to log-out**, by invoking `client.close()`. With the client, you can then join any chat room by invoking the `joinRoom` method, taking as first parameter the host of the chat server and, as second parameter, the id of the room to join.

```java
Room room = client.joinRoom(ChatHost.STACK_OVERFLOW, roomId);
```

Once you have a `Room` object, you can use it to send messages or reply to other messages:

```java
room.send("Hiya o/");
room.replyTo(messageId, "Hey!");
```

Each method in the `Room` class runs asynchronously and returns a [`CompletionStage`](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletionStage.html) that holds the result of the action. Sending a message or replying to a message returns a `CompletionStage<Long>` holding the id of the posted message. This allows for fluid method calls, like uploading an image and posting it as a one-box:

```java
room.uploadImage(Paths.get(pathToImage)).thenAccept(room::send);
```

### Listening to events

Once a `Room` was joined, it is possible to listen to various events on it, like a user joining the chat room or a user posting
a message. All the possible events to listen to are documented in the `EventType` class.

Each event inherits from the `Event` class and gives access to several properties, like the date at which it was raised, the user that raised it and the room in which it was raised. Events related to messages further inherit from `MessageEvent`, giving access
to the id of the message that raised the event.

For example, to listen to mentions, that is to say a message that mentioned the current logged-in user with `@`, the code is

```java
room.addEventListener(EventType.USER_MENTIONED, event -> {
	Message message = event.getMessage(); // gets the message that triggered the mention 
	Room room = event.getRoom(); // gets the room in which it was made
	// ...
});
```