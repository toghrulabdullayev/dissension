2 abstract classes: Pass
Evidence: AbstractChannelMessagePolicy.java:6, AbstractSessionRegistry.java:10, AbstractChatEvent.java:5.

3 interfaces: Pass
Evidence: ChatEventListener.java:3, TargetedChatEvent.java:5, AppUserRepository.java:8, AppServerRepository.java:10, AppChannelRepository.java:10.

1 use of composition: Pass
Evidence: ChatMessageService.java:29, ChatEventPublisher.java:9.

1 use of inheritance: Pass
Evidence: ChatChannelMessagePolicy.java:8, InfoChannelMessagePolicy.java:11, ChatWebSocketSessionRegistry.java:6, UserBannedFromServerEvent.java:6.

Meaningful class/variable names: Pass
Examples: ChatMessageService.java:29, ServerMembershipService.java:1.

Proper packages: Pass
Evidence: package declarations across modules, e.g. ServerService.java:1, ChatController.java:1, AppUser.java:1.

equals/hashCode where needed: Pass
Evidence: AppUser.java:68, AppServer.java:58, AppChannel.java:84, ServerMembership.java:77, ChatMessage.java:74.

At least 1 GoF design pattern: Pass
Strategy pattern is explicitly applied in ChatMessageService.java:61 and ChatMessageService.java:84 via policy resolution and concrete strategies ChatChannelMessagePolicy.java:8, InfoChannelMessagePolicy.java:11.
Observer-style flow is also present via ChatEventPublisher.java:16 and WebSocketChatEventListener.java:18.

Clear OOP concepts: Pass
Abstraction, inheritance, polymorphism, and encapsulation are visible in the chat policy/event/session areas above.

Draw UML diagram: Pass (artifact exists), with a clarity caveat from Finding 1
Evidence: diagram.puml:1, output.puml.