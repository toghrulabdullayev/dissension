2 abstract classes: ❌ Not satisfied.
No abstract class declarations were found in backend Java sources.

3 interfaces: ✅ Satisfied.
You have at least 4 interfaces, for example:
AppServerRepository.java:10
ServerMembershipRepository.java:10
AppChannelRepository.java:8
AppUserRepository.java:7

1 use of composition: ✅ Satisfied.
Example: ServerMembership contains AppServer and AppUser references:
ServerMembership.java:29

1 use of inheritance: ✅ Satisfied.
Example:
JwtAuthenticationFilter.java:19

Meaningful class/variable names: ✅ Mostly satisfied.
Names are generally clear and domain-based (AppUser, ServerMembership, ChannelService, etc.).

Proper packages: ✅ Satisfied.
Package structure is organized by feature (auth/channel/server), e.g.:
ServerService.java:1

equals/hashCode where needed: ⚠️ Likely incomplete.
I do not see explicit equals/hashCode overrides on entity classes; records do get generated equals/hashCode automatically, but entities like AppUser/AppServer/ServerMembership do not define them explicitly.

At least 1 design pattern: ❌ not satisfied.
UNLESS THE ONES PROVIDED BY SPRING BOOT COUNT:
  Repository pattern is clearly used via Spring Data repositories, e.g.:
  AppServerRepository.java:10
  
  Template Method
  You extend OncePerRequestFilter and override only the hook method while the superclass controls the full algorithm skeleton:
  JwtAuthenticationFilter.java:19
  JwtAuthenticationFilter.java:28

  Chain of Responsibility
  Security filters are composed into a chain, and your filter forwards with doFilter:
  SecurityConfig.java:42
  JwtAuthenticationFilter.java:35

  Proxy
  Spring Data creates runtime proxy implementations for repository interfaces:
  AppServerRepository.java:10
  AppUserRepository.java:7

Clear OOP concepts: ✅ Satisfied.
Encapsulation, layering, composition, and inheritance are present.

UML diagram: ❌ Not found in the workspace.
I only found requirements.md and README.md as relevant docs; no UML artifact (.puml/.drawio/etc.) was found.