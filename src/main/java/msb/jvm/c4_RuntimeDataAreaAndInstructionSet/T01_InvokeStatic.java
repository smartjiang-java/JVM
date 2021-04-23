package msb.jvm.c4_RuntimeDataAreaAndInstructionSet;

public class T01_InvokeStatic {

    public static void main(String[] args) {
        m();
    }

    public static void m() {
        m2(1,2);
    }

    public static int m2(int a ,int b) {
       int c=a+b;
       return c;
    }
}
