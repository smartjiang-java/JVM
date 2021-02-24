# 使用JavaAgent测试Object的大小

作者：马士兵 http://www.mashibing.com


1:对象的创建过程

new关键字出现
1：检查符号所在的类是否被加载过
2：为新生对象分配内存：指针碰撞，JAVA堆中内存规整 ；空闲列表 ，不规整
3：将分配的内存空间初始化为零值（不包括对象头）：保证对象实例在不赋初始值就可以使用
4：对对象头进行必要设置
5：执行<init>构造方法


2:对象的内存布局
## 对象大小（64位机）
### 观察虚拟机配置
java -XX:+PrintCommandLineFlags -version

### 普通对象
1和2合称运行时元数据
1. 对象头：markword  8
2. ClassPointer指针：-XX:+UseCompressedClassPointers 为4字节 不开启为8字节
                 是对方法区中类元信息的引用,虚拟机通过这个指针来确定该对象是哪个类的实例,指向方法区
3. 实例数据：真实记录一个对象包含的数据,即程序代码中所定义的各种类型的字段内容，包括父类继承下来的和子类定义的
   1. 引用类型：-XX:+UseCompressedOops 为4字节 不开启为8字节 
      Oops Ordinary Object Pointers
   2:成员变量,8大基本类型变量
 注意：在这一步，有 alignment/padding gap,重排序，判断实例数据的长度必须是4的倍数，不是的话，会在最后做填充.
      一般做法是：
      如果成员变量中有一个四字节的基本类型和最少一个引用类型，那么基本类型加上剩下的成员变量的和不是4的倍数的话，才会发生间隙填充;
      全部都是基本类型是不会发生间隙填充的。
      八大基本类型：boolean(1),byte(1),short(2),char(2),int(4),long(8),float(4),double(8)
      ![binaryTree](tmp/image/padding%20gap.png)   
4. Padding对齐，8的倍数
        填充部分仅起到占位符的作用, 不是必然存在的，原因是HotSpot要求对象起始地址必须是8字节的整数倍。 
        假如不是，就采用对齐填充的方式将其补齐8字节整数倍，原因是64位机器能被8整除的效率是最高的

1：加锁之后，hashcode跑到哪里去了？？
 new对象出来的hashcode如果有调用，是identityHashcode,并不是我们重写的HashCode，存放到对象的markdown中
一旦加锁，hashcode存放到自己的线程栈的lr中，这个lr指向一个数据结构，指向前面用来做备份的markdown中，成为displacedHead
2：如果加了可重入锁，那么会在线程栈再生成一个lr，是空的，解锁的时候弹出，弹完lr锁就解了

### 数组对象
1. 对象头：markword 8
2. ClassPointer指针同上
3. 数组长度：4字节
4. 数组数据
5. 对齐 8的倍数

object对象16字节,8字节脑袋,本来point8字节,但是JVM默认开启指针压缩,变成4字节,此时是12字节，然后padding8字节对齐,16字节
数组对象16字节,8字节脑袋,4字节指针,4字节数组长度,没有padding了


## 实验
1. 新建项目ObjectSize （1.8）

2. 创建文件ObjectSizeAgent

   ```java
   package com.mashibing.jvm.agent;
   
   import java.lang.instrument.Instrumentation;
   
   public class ObjectSizeAgent {
       private static Instrumentation inst;
   
       public static void premain(String agentArgs, Instrumentation _inst) {
           inst = _inst;
       }
   
       public static long sizeOf(Object o) {
           return inst.getObjectSize(o);
       }
   }
   ```

3. src目录下创建META-INF/MANIFEST.MF

   ```java
   Manifest-Version: 1.0
   Created-By: mashibing.com
   Premain-Class: com.mashibing.jvm.agent.ObjectSizeAgent
   ```

   注意Premain-Class这行必须是新的一行（回车 + 换行），确认idea不能有任何错误提示

4. 打包jar文件

5. 在需要使用该Agent Jar的项目中引入该Jar包
   project structure - project settings - library 添加该jar包

6. 运行时需要该Agent Jar的类，加入参数：

   ```java
   -javaagent:C:\work\ijprojects\ObjectSize\out\artifacts\ObjectSize_jar\ObjectSize.jar
   ```

7. 如何使用该类：

   ```java
      package com.mashibing.jvm.c3_jmm;
      
      import com.mashibing.jvm.agent.ObjectSizeAgent;
      
      public class T03_SizeOfAnObject {
          public static void main(String[] args) {
              System.out.println(ObjectSizeAgent.sizeOf(new Object()));
              System.out.println(ObjectSizeAgent.sizeOf(new int[] {}));
              System.out.println(ObjectSizeAgent.sizeOf(new P()));
          }
      
          private static class P {
                              //8 _markword
                              //4 _oop指针
              int id;         //4
              String name;    //4
              int age;        //4
      
              byte b1;        //1
              byte b2;        //1
      
              Object o;       //4
              byte b3;        //1
      
          }
      }
   ```

## Hotspot开启内存压缩的规则（64位机）

1. 4G以下，直接砍掉高32位
2. 4G - 32G，默认开启内存压缩 ClassPointers Oops
3. 32G，压缩无效，使用64位
   内存并不是越大越好

## IdentityHashCode的问题

回答白马非马的问题：

当一个对象计算过identityHashCode之后，不能进入偏向锁状态

https://cloud.tencent.com/developer/article/1480590
 https://cloud.tencent.com/developer/article/1484167

https://cloud.tencent.com/developer/article/1485795

https://cloud.tencent.com/developer/article/1482500

## 对象定位    T  t =new T();  t是如何找到new出来的T的?

•https://blog.csdn.net/clover_lily/article/details/80095580

1. 句柄池 :间接指针,t指向两个指针,一个指向new出来的T对象,一个指向T.class
2. 直接指针:直接指向new出来的T对象,T对象有一个指针指向T.class  (Hospot使用此种方式)