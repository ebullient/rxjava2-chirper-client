# Think 2018: Reactive with RxJava2

<!-- TOC depthFrom:2 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [REST can be Reactive, too](#rest-can-be-reactive-too)
- [Reactive Event Source](#reactive-event-source)
- [Reactive Transformation](#reactive-transformation)

<!-- /TOC -->

To make sure we're ready:

        $ mvn -q package exec:java

You should hopefully see the following:

        Ready to go with RxJava!

## REST can be Reactive, too!

1. Open VS Code
2. Find the `rxjava2-chirper-client` folder
3. Open `src/main/java/demo/ReactiveWorkshop.java`
4. **Uncomment** lines 8 & 9, to allow the RestClient to run; **Save**
5. Open `src/main/java/demo/RestClient.java`

    Notes:
    * Lines 27-29 contain our test user data.
    * Lines 31-59 contain plain old java objects (POJOs) representing the remote API we'll be calling, and the data structures that will be passed back and forth
    * Lines 61-73 configure an outbound Http client that will manage http connections and data marshalling

6. Observables can have many sources, including arrays! 

        $ cd ~/rxjava2-chirper-client
        
    Let's go ahead and run this, you should see what you expect:

        $ mvn -q package exec:java@events
        Ready to go with RxJava!
        0: Fred
        1: Barney
        2: Wilma
        3: Betty

    Looks a lot like a for loop, doesn't it? In this case, we created an `Observable` that emitted an item for each element in the `users` array.

    Let's uncomment some more lines to attempt adding users

7. **Uncomment** lines 88-92 **AND** lines 95-106; **Save**

        $ mvn -q package exec:java@events
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

8. **Uncomment** lines 117-129 to actually create the user; **Save**

        $ mvn -q package exec:java@events

    Sadly, the create operation doesn't return anything particularly interesting on its own. However, the users should be created now, which means if we run it again, we shouldn't see any 404 output:

        $ mvn -q package exec:java@events
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

9. Let's now give these user's some friends. **Uncomment** lines 93-94, and 122-123; **Save**.

        $ mvn -q package exec:java@events

    Again, the `addFriends` method doesn't return anything interesting, so let's run the command again:

        $ mvn -q package exec:java@events
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

    Let's take a look at that `addFriends` method: nested Observables are not the best plan. Something to fix in the future.

10. Go back to [the workshop](https://github.com/IBM/reactive-code-workshop/blob/master/ReactiveREST.md) to try this again Akka.

## Reactive Event Source

1. Open VS Code
2. Find the `rxjava2-chirper-client` folder
3. Open `src/main/java/demo/ReactiveWorkshop.java`
4. Comment out lines 8 & 9, as we don't need to work with users and groups anymore
5. Uncomment lines 12 & 13; **Save**
6. Open `src/main/java/demo/Chirper.java`

    Note that there are some familiar elements here:
    * Lines 27-29 contain our test user data.
    * Lines 31-51 contain plain old java objects (POJOs) representing the remote API we'll be calling, and the data structures that will be passed back and forth
    * Lines 51-65 configure an outbound Http client that will manage http connections and data marshalling

7. The `randomChirps` method on lines 67-98 is doing something interesting: it is using reactive programming to read lines from a file! For each line in the file, it then sends out a chirp.

        $ mvn -q package exec:java@events
        Ready to go with RxJava!
        Sending 1521465766230::Heyooo I'm on the Chirper::Fred
        Sending 1521465766329::Oh hey Fred::Barney
        Sending 1521465766350::Want to go grab some Pizza Barney?::Fred
        Sending 1521465766363::Ooooh Pizza, I'd love to!::Barney
        Sending 1521465766380::Don't forget what the doctor said!::Wilma
        Sending 1521465766399::Awww, Hey Barney, want to grab some salad?::Fred
        Sending 1521465766415::Salad? Whats that?::Barney
        Sending 1521465766435::It's like pizza, but..::Fred
        Sending 1521465766453::Good so far.. ::Barney
        Sending 1521465766462::Without the cheese::Fred
        Sending 1521465766479::Not so bad.::Barney
        Sending 1521465766494::Without the bread::Fred
        Sending 1521465766514::I'm not liking where this is going::Barney
        Sending 1521465766537::Without the toppings::Fred
        Sending 1521465766556::So, whats left? the box?::Barney
        Sending 1521465766566::Then you add lettuce, and vegetables::Fred
        Sending 1521465766579::Fred, you suck, I'm off to drink beer.::Barney
        Sending 1521465766620::Actually, Barney.. ::Betty
        Sending 1521465766633::Uhoh.::Barney
        Sending 1521465766655::Uhoh.::Fred
        Sending 1521465766668::*Chuckle*::Wilma

    Woohoo!!! Chirps! Headed out into the ether.

8. For the next step, we will want these chirps to keep on keepin' on. So let's flip the comments around (as indicated in the file) to make things go on forever. **Save**.

        $ mvn -q package exec:java@events
        Ready to go with RxJava!
        Sending 1521466006387::Fred::Heyooo I'm on the Chirper
        Sending 1521466010570::Barney::Oh hey Fred
        Sending 1521466012934::Fred::Want to go grab some Pizza Barney?
        ...

    Note the output is slower, and that it just keeps on going (and will do so, until you kill the process with good old **Ctrl-C** )

9. Go back to [the workshop](https://github.com/IBM/reactive-code-workshop/blob/master/ReactiveEventSource.md) to try this again Akka.

References for this exercise:

* [Rx: Just](http://reactivex.io/documentation/operators/just.html)
* [Rx: Repeat](http://reactivex.io/documentation/operators/repeat.html)
* [Rx: Switch](http://reactivex.io/documentation/operators/switch.html)
* [Rx: Zip](http://reactivex.io/documentation/operators/zip.html)
* [RxJava's repeat and retry explained](http://blog.danlew.net/2016/01/25/rxjavas-repeatwhen-and-retrywhen-explained/)
* [Understanding switchMap](https://github.com/mgp/effective-rxjava/blob/master/items/understand-switch-map.md)

## Reactive Transformation

1. Open VS Code
2. Find the `rxjava2-chirper-client` folder
3. Open `src/main/java/demo/WsClient.java`
4. Take a look at line 71. This is our starting point, essentially a println for the incoming stream of events.
5. Now we need a bit of setup. We need to open two terminal windows. In one window, turn on the stream of chirps again:

        $ mvn -q package exec:java@events

    We'll just leave this running. In the other terminal window, we'll get the websocket going:

        $ mvn -q package exec:java@stream

    You should see some initial log noise showing the websocket connection being established, followed by a parade of chirps in the second window that matches what is being emitted in the first. Hooray for streams!!

6. Use ctrl-c to stop the websocket process (the 2nd terminal window). Leave the  other running.

7. Let's go into `src/main/java/demo/WsClient.java` and **comment out** line 71, and **uncomment** the next block (lines 76-81). **Save**

        $ mvn -q package exec:java@stream

    On line 75, there is a `.take(3)`, which limits the number of events this Observable accepts to 3. So while the process doesn't quit, the output stops at 3.

    Also note that the first chirp shown skips quite a few chirps: older chirps are discarded by the filter on line 68.

8. Use ctrl-c to stop the websocket process. Go back to the code, **comment out** lines 76-81, and **uncomment** the next block (lines 83-84).

        $ mvn -q package exec:java@stream

    On line 83, we've used a `.map` operation to transform the text to uppercase.

9. Use ctrl-c to stop the websocket process. Go back to the code, **comment out** lines 83-84, and **uncomment** lines 87-88.

        $ mvn -q package exec:java@stream

    This looks like run-on sentences! The `.buffer` operation on line 87 is converting 3 lines into one line.

9. Use ctrl-c to stop the websocket process. Go back to the code, **comment out** lines 87-88, and **uncomment** lines 96-101.

        $ mvn -q package exec:java@stream

    Hey: we only see numbers now! This is the mother of all transforms: it uses `.map`, `.window`, `.flatMap`, and `.reduce` to produce a rolling average tweet length for the previous 3 tweets. Holy cow, batman!

10. Clean up the running processes, and go back to [the workshop](https://github.com/IBM/reactive-code-workshop/blob/master/ReactiveTransformation.md) to try this again Akka.


## General References for Rx

* [RxJava Walkthrough](https://balamaci.github.io/rxjava-walkthrough)
