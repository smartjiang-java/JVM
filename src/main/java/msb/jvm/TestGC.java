package msb.jvm;

public class TestGC {
    public static void main(String[] args) {
        for(;;) {
            new Object();
        }
    }
}
