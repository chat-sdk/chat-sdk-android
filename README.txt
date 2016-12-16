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
1. Add comments to all functions describing their purpose (Doxygen)
2. Merge master changes into feature branches whenever possible. 
3. Changes to feature branches should always be double checked if they belong in master.


Future Plans / DEVELOPMENT DIRECTION
~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-
1. Reducing Complexity
    a) jDeferred is being phased out in favour of rxJava.
    b) Greendao2 is being replaced with GreenDao3 which uses annotations.
    c) Cleaning up stale branches.
