README.txt
created by Kyle Krüger - 25.07.16
ChatCat Android Application
~~~~~~~~~~~~~~~~~~~~~~~~~~~

The README file is where the plan of a development project lives,
in this file you will find various useful pieces of information:
    1. Application intention
    2. Contribution Guidelines
    2. Future Plans
    3. Application Structure

APPLICATION INTENTION
-~-~-~-~-~-~-~-~-~-~-~
The purpose of the ChatCat app is to provide a communication tool which allows users to
have conversations on private databases hosted for various companies. This may function as
internal chat for businesses; a channel for customer relations; or open and public chat groups.

The communication tool connects through a database which is shared between web, iOS, and android
platforms! Currently the database used to connect the applications is the Firebase host.


CONTRIBUTION GUIDELINES - moving forward
~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-
1. Add comments to all functions describing their purpose
2. Practice Test Driven Development
3.


Future Plans / DEVELOPMENT DIRECTION
~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-
1. Reducing Complexity
    a) Update usage of jdeferred;
         the current call initialization of DeferredObjects
         is confusing . . .
         private Deferred<Void, Void, Void> deferred;
    b) Review usage of and update greenDAO
2.Features to be added
    a) two_factor_auth_2
    b) offlineMode
    c) phone_book_contacts
3. Merge main project to firebase3 branch