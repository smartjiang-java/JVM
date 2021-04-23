# Runtime Data Area and Instruction Set

jvms 2.4 2.5

## 指令集分类

1. 基于寄存器的指令集
2. 基于栈的指令集
   Hotspot中的Local Variable Table = JVM中的寄存器

## Runtime Data Area

PC 程序计数器:存放指令位置
>
> 虚拟机的运行，类似于这样的循环：
> while( not end ) { }
> 取PC中的位置，找到对应位置的指令；
> 执行该指令；
> PC ++;
> }
>
>问题:为什么每个线程都要有自己的PC?
>方便线程上下文切换,记录线程执行到那条指令

JVM Stack:写的每一个线程对应一个栈,一个方法对应一个栈帧
 Frame - 每个方法对应一个栈帧
   1. Local Variable Table:本地变量(局部变量)表,方法内部使用的,参数也算在内
   2. Operand Stack:操作数栈
      对于long的处理（store and load），多数虚拟机的实现都是原子的
      jls 17.7，没必要加volatile
   3. Dynamic Linking:动态链接,指向常量池的符号链接,如果没有解析,就去动态解析
       https://blog.csdn.net/qq_41813060/article/details/88379473 
      jvms 2.6.3
   4. return address:返回值地址
      a() -> b()，方法a调用了方法b, b方法的返回值放在什么地方

Heap

Method Area(各种各样的class,常量池)
1. Perm Space(永久代) (<1.8)
   字符串常量位于PermSpace
   FGC不会清理
   大小启动的时候指定，不能变
2. Meta Space (>=1.8)
   字符串常量位于堆
   会触发FGC清理
   不设定的话，最大就是物理内存

Runtime Constant Pool:运行时数据区,扔在常量池

Native Method Stack:本地方法栈,C和C++写的方法

Direct Memory:直接内存:用户空间可以访问os内存
> JVM可以直接访问的内核空间的内存 (OS 管理的内存)
> NIO ， 提高效率，实现zero copy,以前需要将网络传到内存中的东西拷贝到JVM内存,现在省去拷贝时间,直接去访问

思考：
> 如何证明1.7字符串常量位于Perm，而1.8位于Heap？
> 提示：结合GC， 一直创建字符串常量，观察堆，和Metaspace


## 常用指令(栈消耗内存,所以栈深度有限)
注意:如果方法是非static,局部变量表的0号位都是this,如果是静态方法,那么0号位一般都是参数
bipush 压栈
istore_1:将栈顶的值pop出来,放到局部变量表的1号位置
iload_1:将局部变量表1号位的值拿出来压栈
iinc 1 by 1:把局部变量表1号位置值加1
iadd 在栈里拿出两条int值相加,结果放在栈顶
dup:复制一份,一般执行构造方法的时候会消耗一个
invokespecial:执行特殊的方法,一般是init,构造方法,private 方法---可以直接定位，不需要多态的方法
invokevirtual:调用方法,执行另一个栈帧,另一个栈帧执行结束,有返回值,会在栈顶放入这个值
invokeStatic:调用静态方法
invokeInterface:new对象时上转型,通过接口去调用方法
InvokeDynamic:JVM最难的指令,1.7之后java支持动态语言后加入lambda表达式或者反射或者其他动态语言scala kotlin，或者CGLib ASM，动态产生的class，会用到的指令
iconst_1 :把1这个立即数压栈
imul:相乘
clinit:类在初始化阶段调用的方法,将static变量赋定义值
init::类构造方法
sub:减法

