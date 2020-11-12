## 3：类加载-初始化
一个class被加载到内存中,有两块内容:一块用来保存class的二进制文件,
一块是class类对象(堆内存中),通过访问方法区的家口指向哪些二进制内容,通过访问class对象去访问二进制文件
 
1:Loading
2:Linking
    1:Verficetio:校验
    2:Preparation:准备
    3:Resolution:解析
3:Initilizing:初始化

1. 加载过程
   1. Loading  加载(类加载器ClassLoader)
   1.1: 顶级父类ClassLoader
   类加载器就是普通的class,JVM中所有的class都是被类加载器加载到内存的
   如何判断一个类是被那个类加载器加载到内存中的?  类名.class.getClassLoader()
   最顶层的加载器Bootstrap,输出加载器的结果为null,由c++实现,加载jdk核心类,例如String,Object,charset
   其次是ExtClassLoader,Lanucher类的内部类,加载扩展jar包下的ext中的类
   其次是AppClassLoader,Lanucher类的内部类,加载我们写的classpath下的类
   最后是自定义加载器CutstomClassLoader,Lanucher类的内部类,自定义想加载的类
     比如先去自定义加载器缓存里面找是否被加载过,如果没有就往上面类加载器找,都找不到,
     会从顶级加载器依次向下委托进行加载.如果加载不成功,会抛出ClassNotFound异常.
       1. 双亲委派，主要出于安全来考虑,防止核心类被覆盖,次要是防止重复加载,防止资源浪费
       2.子加载器与父加载器不是继承关系,而是有一个成员变量是上一级加载器      
       问题：parent是如何指定的？
       答：在自定义classLoader时会调用父亲默认的classLoader()方法，源码中是通过 ClassLoader.getSystemClassLoader() 
          获取默认的classLoader当做parent（在没有手动指定的情况下）。
       
       问题：如何打破双亲委派？
       答：重写loadClass()，而不是重写findClass()，因为loadClass()方法写好了双亲委派的过程。
       问题:何时打破?
       1:jdk1.2之前,自定义的ClassLoader都必须重写LoaderClass
       2:在线程里面设定线程的上下文的ClassLoader,去加载各种的class
       3:热启动,热部署:
       tomcat中每一个webApplication中都有不同的classLoader，因为不同上下文中可能加载了同名的类的不同版本。
       jsp等位什么能热加载，因为重写了loadClass()，打破了双亲委派，每次给了需要加载的类名就把已有的自定义classLoader整个干掉，
       重新new 一个classLoader然后加载，这样每次加载的就不一样了，若补充协议loadClass()打破双亲委派，则第二次加载会找加载过的内容直接返回。
       load的类都放在metaspace方法区里，原来load的老的类，如果没有指向他的引用就会被GC回收。
       
   1.2: LazyLoading,JVM规范并没有规定何时加载 但是规定了必须初始化的五种情况
         1. –new getstatic putstatic invokestatic指令，访问final变量除外
            –java.lang.reflect对类进行反射调用时
            –初始化子类的时候，父类首先初始化
            –虚拟机启动时，被执行的主类必须初始化
            –动态语言支持java.lang.invoke.MethodHandle解析的结果为
            REF_getstatic REF_putstatic REF_invokestatic的方法句柄时，该类必须初始化    
   1.3: ClassLoader的源码
        加载器的loadclass()方法去加载类
         1. findInCache -> parent.loadClass
         (检测是否加载,没有的话调用父加载器去加载,加入一直没找到,假如没找到,父类也没去加载,只能自己去加载) 
          -> URLClasLoader的findClass() (抛出了异常,所以自定义类加载器需要实现这个方法)  
   1.4: 自定义类加载器(勾子函数,模板方法)
         1. extends ClassLoader
         2. overwrite findClass() -> defineClass(byte[] -> Class clazz)
         3. 加密
         4. <font color=red>第一节课遗留问题：parent是如何指定的，打破双亲委派，学生问题桌面图片</font>
            1. 用super(parent)指定
            2. 双亲委派的打破  
               1. 如何打破：重写loadClass（）
               2. 何时打破过？
                  1.JDK1.2之前，自定义ClassLoader都必须重写loadClass()
                  2.ThreadContextClassLoader可以实现基础类调用实现类代码，通过thread.setContextClassLoader指定
                  3.热启动，热部署
                     1.osgi tomcat 都有自己的模块指定classloader（可以加载同一类库的不同版本）  
   1.5: 混合执行 编译执行 解释执行   
         JIT:把JAVA代码编译成本地编码,c语言编译完的代码.exe叫做本地代码,native
         默认是混合模式-Xmixed,混合使用解释器+热点代码编译 ,开始解释执行,启动速度较快,对热点代码实现检测和编译 
         起始阶段采用解释执行-Xint,启动很快,解释很慢        
         -Xcomp 使用纯编译模式,执行很快,启动很慢 
         热点代码检测:-XX:CompileThreshold = 10000
         多次被调用的方法(方法计数器:检测方法执行频率)
         多次被调用的循环(循环计数器:检测循环执行频率)
         进行编译
      
2. Linking 
      1. Verification 校验
         1. 验证文件是否符合JVM规定,魔术值校验等等
      2. Preparation  准备
         1. 静态成员变量赋默认值 
         2.final修饰的直接赋初值
      3. Resolution    
         1. 将类、方法、属性等符号引用解析为直接引用,内存地址,直接能访问的
            常量池中的各种符号引用解析为指针、偏移量等内存地址的直接引用
            （符号引用：常量池中有一个类的名字，实际上这个类可能是在内存中的另一个片区域，被这个名字指向，
            当使用的时候我们写的字符指向这个名字，这个名字指向那一片真正的区域）。解析就是让写的字符直接指向那一片类真正存在的内存空间。
      
3. Initializing    初始化
      1. 调用类初始化代码 <clinit>，给静态成员变量赋初始值
   
2. 小总结：

   1. load - 默认值 - 初始值
   2. new - 申请内存 - 默认值 - 初始值