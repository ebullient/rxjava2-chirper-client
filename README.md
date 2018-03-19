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
3. Open `src/main/java/demo/RestClient.java`

    Notes:
    * Lines 27-29 contain our test user data.
    * Lines 31-59 contain plain old java objects (POJOs) representing the remote API we'll be calling, and the data structures that will be passed back and forth
    * Lines 61-73 configure an outbound Http client that will manage http connections and data marshalling

4.

When all changes are ready:

        $ mvn -q exec:java

## Reactive Event Source


When all changes are ready:

        $ mvn -q exec:java

## Reactive Transformation


When all changes are ready:

        $ mvn -q exec:java
