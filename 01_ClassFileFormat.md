# JVM

## 1：JVM基础知识

1. 什么是JVM
    JVM：跨语言的平台。JVM与java无关，只认识字节码文件（.class），是一种规范
   Java Virtual Machine:Java虚拟机，java程序的运行环境(二进制字节码的运行环境)
2. 常见的JVM
   Hotspot(oracle官方)    J9(IBM)     Microsoft VM(微软)     
   TaobaoVM(淘宝，Hotspot定制版)    azul zing(商业版本收费，垃圾回收最快)
   
## 2：ClassFileFormat

1. class文件二进制字节流,由JVM解释执行
2. 数据类型:u1 u2 u4 u8 和_info(表类型)    uX:表示X字节的无符号整数
  _info的来园时hotspot源码中的写法
3. 查看16进制格式的ClassFile
  sublime / notepad
  IDEA插件 -BinEd
4. 有很多观察ByteCode的方法
  javap:java自带,显示class文件的信息
  JBE -可以直接修改
  JClassLib -IDEA插件之一
5. classFile构成
     classFile{
         u4 magic;
         u2 minor_version;
         u2 major_version;
         U2 constant_pool_count;
         Cp_info constant_pool[constant_pool_count-1]
         U2... 
    }
6. 解释:
    1:Magic Number:CA FE BA BE指的是文件统一的标识符,是Class文件,不是png文件等等
    2:Minor Version:00 00,小版本号,例如3.4 ,这里表示0
    3:Major Version:00 34,大版本号,1,8版本编译后是52
    4:constant_pool_count: 00 10,表示常量池中的常量数16-1=15,常量池编号从1开始
    5:constant_pool表示长度为constant_pool_count-1的表
    6:access_flags:修饰符(piblic/..),interface..
    7:this_class:当前类指到常量池的位置
    8:super_class:父类指到常量池的位置
    9:interface_count:接口数量
    10:interfaces:
    11:fields_count:属性属性
    12:fields:
    13:methods_count:方法数量
    14:methods: 
    15:attributes_count -u2:附加属性数量
    16:attributes:
注意:code是方法的具体实现
## 3：类加载-连接-初始化

hashcode
锁的信息（2位 四种组合）
GC信息（年龄）
如果是数组，数组的长度

## 4：JMM

new Cat()
pointer -> Cat.class
寻找方法的信息

## 5：对象

1：句柄池 （指针池）间接指针，节省内存
2：直接指针，访问速度快

## 6：GC基础知识

栈上分配
TLAB（Thread Local Allocation Buffer）
Old
Eden
老不死 - > Old

## 7：GC常用垃圾回收器

new Object()
markword          8个字节
类型指针           8个字节
实例变量           0
补齐                  0		
16字节（压缩 非压缩）
Object o
8个字节 
JVM参数指定压缩或非压缩

