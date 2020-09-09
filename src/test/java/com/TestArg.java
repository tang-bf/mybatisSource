package com;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 *
 */
public class TestArg {


  public void test(String name,String age){

  }

  public static void main(String[] args) throws Exception{
    /**
     * 1 由于jdk的反射方式，未提供获取参数名的方法
     *
     *  2 Java的字节码文件默认不存储参数名称。在使用javac编译时，如果开启-g:{vars}选项，
     *  可以增加Local variable debugging information。对java方法，
     *  参数实际是按照局部变量来存储的，所以可以获取参数名称；
     *  但对于java接口中的方法声明，这种方法就无法获取参数名称。
     *   Paranamer专门用来解决获取参数名的问题。其原理是在编译阶段，
     *   修改.class文件，在类或接口的字节码文件中增加一个字符串常量，
     *   这个常量保存了所有的方法声明信息，包括方法名、参数类型、参数名称。
     *   这样在运行时，class loader加载类文件以后，使用Paranamer的api去读取这个字符串，就可以获取参数名称。
     *   加上java反射，实际上可以把源代码重现出来。
     */
    Method test = TestArg.class.getMethod("test", String.class, String.class);
    // Method test = Test1.class.getMethod("hello", String.class, String.class); 接口也能拿到
    for (Parameter parameter : test.getParameters()) {
      System.out.println(parameter.getName());
    }
  }
}
