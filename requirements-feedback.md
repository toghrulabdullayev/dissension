Exists:

2 abstract classes: Yes (3 found)
AbstractChatEvent.java:5
AbstractChannelMessagePolicy.java:6
AbstractSessionRegistry.java:10

3 interfaces: Yes (7 found)
ChatEventListener.java:3
TargetedChatEvent.java:5
AppServerRepository.java:10

1 use of composition: Yes
ChatMessageService.java:25

1 use of inheritance: Yes
ChatChannelMessagePolicy.java:8
ChatWebSocketSessionRegistry.java:6

Meaningful class/variable names: Yes (looks good overall)
ChatMessageService.java:29

Proper packages: Yes
AuthService.java:1
ServerController.java:1
ChatEventPublisher.java:1

equals/hashCode where needed: Yes now
AppUser.java:68
AppServer.java:58
AppChannel.java:84
ServerMembership.java:77
ChatMessage.java:74

At least 1 GoF pattern: Yes
Strategy:
ChatMessageService.java:62
ChatMessageService.java:84
Observer-style publisher/listener:
ChatEventPublisher.java:9
ChatEventPublisher.java:17

Clear OOP concepts: Yes (abstraction, inheritance, polymorphism, encapsulation visible in current design)

Missing:

Draw UML diagram: Not found in backend directory (no UML artifact detected).