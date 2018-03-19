# Think 2018: Reactive with RxJava2

<!-- TOC depthFrom:2 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [REST can be Reactive, too](#rest-can-be-reactive-too)
- [Reactive Event Source](#reactive-event-source)
- [Reactive Transformation](#reactive-transformation)

<!-- /TOC -->

To make sure we're ready:

        $ mvn -q exec:java

You should hopefully see the following:

        Ready to go with RxJava!

## REST can be Reactive, too!

1. Open VS Code
2. Find the `rxjava2-chirper-client` folder
3. Open `src/main/java/demo/ReactiveWorkshop.java`
4. Uncomment lines 8 & 9, to allow the RestClient to run
5. Open `src/main/java/demo/RestClient.java`

    Notes:
    * Lines 27-29 contain our test user data.
    * Lines 31-59 contain plain old java objects (POJOs) representing the remote API we'll be calling, and the data structures that will be passed back and forth
    * Lines 61-73 configure an outbound Http client that will manage http connections and data marshalling

6. Observables can have many sources, including arrays! Let's go ahead and run this, you should see what you expect:

        $ mvn -q package exec:java
        Ready to go with RxJava!
        0: Fred
        1: Barney
        2: Wilma
        3: Betty

    Looks a lot like a for loop, doesn't it? In this case, we created an `Observable` that emitted an item for each element in the `users` array.

    Let's uncomment some more lines to attempt adding users

7. Remove the comments on lines 87-91 AND lines 94-105

        $ mvn -q package exec:java
        Ready to go with RxJava!
        0: Fred
        User not found: retrofit2.adapter.rxjava2.HttpException: HTTP 404 Not Found
        1: Barney
        User not found: retrofit2.adapter.rxjava2.HttpException: HTTP 404 Not Found
        2: Wilma
        User not found: retrofit2.adapter.rxjava2.HttpException: HTTP 404 Not Found
        3: Betty
        User not found: retrofit2.adapter.rxjava2.HttpException: HTTP 404 Not Found

    As we would expect, the users don't exist yet.

    Note: The error handling for the `Observable` looks suspiciously similar to the error handling for `Promises`.

8. Uncomment lines 79-118 to actually create the user

        $ mvn -q package exec:java

    Sadly, the create operation doesn't return anything particularly interesting on its own. However, the users should be created now, which means if we run it again, we shouldn't see any 404 output:

        $ mvn -q package exec:java
        Ready to go with RxJava!
        0: Fred
        Fred::Fred Flatstone::[]
        1: Barney
        Barney::Barney Robble::[]
        2: Wilma
        Wilma::Wilma Flatstone::[]
        3: Betty
        Betty::Betty Robble::[]

    Woo-hoo! We have users!

9. Let's now give these user's some friends. Uncomment lines 93-94, and 122-123.

        $ mvn -q package exec:java

    Again, the `addFriends` method doesn't return anything interesting, so let's run the command again:

        $ mvn -q package exec:java
        Ready to go with RxJava!
        0: Fred
        Fred::Fred Flatstone::[Barney, Wilma]
        1: Barney
        Barney::Barney Robble::[Betty, Fred, Wilma]
        2: Wilma
        Wilma::Wilma Flatstone::[Betty, Fred]
        3: Betty
        Betty::Betty Robble::[Wilma, Barney]

    Friends!

    Let's take a look at that `addFriends` method: nested Observables are not the best plan. Perhaps by the end of this lab, you'll have some ideas about how to fix it.


When all changes are ready:

        $ mvn -q exec:java

## Reactive Event Source


When all changes are ready:

        $ mvn -q exec:java

## Reactive Transformation


When all changes are ready:

        $ mvn -q exec:java
