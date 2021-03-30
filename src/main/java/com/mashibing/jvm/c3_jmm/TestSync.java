package com.mashibing.jvm.c3_jmm;

public class TestSync {

    //加在方法m上
    synchronized  void m(){}

    //加在方法n上的代码块上
    void n( int j) {
        synchronized (this) {
            System.out.println(1/0);
        }
    }
    public static void main(String[] args) {
    }
}
