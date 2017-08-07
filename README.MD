# ChatExchange

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
  <version>1.1.1</version>
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

The same could be done to listen to replies, which are messages linked to a specific message of the current logged-in user,
with the `EventType.MESSAGE_REPLY` constant.

Another example, showing how to listen to user joining the chat room:

```java
room.addEventListener(EventType.USER_ENTERED, event -> {
	System.out.println("User " + event.getUserId() + " joined the room " + event.getRoomId());
});
```

### Sample usage for a bot

Here's an example of using the library in order to build a simple chat bot. In this code, `email` and `password` represent
the credentials the bot is going to use to log into Stack Exchange, and `roomId` represent the id of the room on the Chat.SO
platform, which is the number at the end of the URL to access it: `https://chat.stackoverflow.com/rooms/{roomId}`.

What it does is simply listening to all messages posted, through the `MESSAGE_POSTED` event; it replies something to the poster if that message is `"coffee"` and the bot is stopped if the message is `"die"`.

```java
public static void main(String[] args) throws Exception {
    StackExchangeClient client = new StackExchangeClient(email, password);
    CountDownLatch countDownLatch = new CountDownLatch(1);
    Room room = client.joinRoom(ChatHost.STACK_OVERFLOW, roomId);
    room.addEventListener(EventType.MESSAGE_POSTED, e -> {
    	Message message = e.getMessage();
        switch (message.getPlainContent()) {
        case "coffee": room.replyTo(message.getId(), "Have some coffee!"); break;
        case "die": room.send("Bye."); countDownLatch.countDown(); break;
        }
    });
    try {
        countDownLatch.await();
    } finally {
        client.close();
    }
}
```

All of this processing happens in background threads. In this example, the main thread is kept waiting with a [`CountDownLatch`](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CountDownLatch.html) of 1. Once `"die"` is posted, it reaches 0 and the main thread closes the client, causing the bot to leave the room. 
