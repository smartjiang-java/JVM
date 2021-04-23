package msb.jvm.c2_classloader;

import msb.jvm.Hello;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * 自定义类加载器
 */
public class T006_MSBClassLoader extends ClassLoader {
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        File f = new File("c:/test/", name.replace(".", "/").concat(".class"));
        try {
            FileInputStream fis = new FileInputStream(f);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int b = 0;
          //将字节码通过流加载
            while ((b=fis.read()) !=0) {
                baos.write(b);
            }
          //将流转化为二进制字节数组
            byte[] bytes = baos.toByteArray();
            baos.close();
            fis.close();//可以写的更加严谨
          //通过ClassLoader里面的defineClass()方法将流转化为class对象
            return defineClass(name, bytes, 0, bytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.findClass(name); //throws ClassNotFoundException
    }

    public static void main(String[] args) throws Exception {
        ClassLoader l = new T006_MSBClassLoader();
        Class clazz = l.loadClass("msb.jvm.Hello");
        Class clazz1 = l.loadClass("msb.jvm.Hello");

        System.out.println(clazz == clazz1);
        Hello h = (Hello)clazz.newInstance();
        h.m();

        System.out.println(l.getClass().getClassLoader());
        System.out.println(l.getParent());
        System.out.println(getSystemClassLoader());
    }
}
