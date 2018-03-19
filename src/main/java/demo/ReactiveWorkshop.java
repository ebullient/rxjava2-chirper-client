package demo;

public final class ReactiveWorkshop {

    public static void main(String[] args) throws Exception {
        System.out.println("Ready to go with RxJava!");
        // #4, uncomment the next two lines
        RestClient rc = new RestClient();
        rc.checkAndCreateUsers();

        // Chirper crc = new Chirper();
        // crc.randomChirps(); //currently blockingSubscribe, will never return.
    }
}
