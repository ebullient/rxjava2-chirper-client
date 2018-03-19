package demo;

public final class ReactiveWorkshop {

    public static void main(String[] args) throws Exception {
        System.out.println("Ready to go with RxJava!");
        // #4 in REST can be reactive, too: uncomment the next two lines
        // RestClient rc = new RestClient();
        // rc.checkAndCreateUsers();

        // #5 in Reactive Events: uncomment the next two lines
        Chirper crc = new Chirper();
        crc.randomChirps(); //currently blockingSubscribe, will never return.
    }
}
