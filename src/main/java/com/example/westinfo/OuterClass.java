package com.example.westinfo;

import org.apache.catalina.Executor;

import java.util.concurrent.Executors;

public class OuterClass {

    private String outerField = "内部类";

    // 1. 成员内部类
    class InnerClass {
        void display() {
            System.out.println("Accessing from InnerClass: " + outerField);
        }
    }

    // 2. 静态内部类
    static class StaticInnerClass {
        void display() {
            System.out.println("StaticInnerClass does not need an outer instance.");
        }
    }

    // 3. 局部内部类
    void methodWithLocalClass() {
        class LocalInnerClass {
            void display() {
                System.out.println("LocalInnerClass inside method."+outerField);
            }
        }
        LocalInnerClass localInner = new LocalInnerClass();
        //
        localInner.display();
    }

    // 4. 匿名内部类
    void createAnonymousInnerClass() {
        String str = "匿名内部类的参数";
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("匿名内部类."+outerField);
            }
//            void testa(){
//                System.out.println("str."+str);
//            }
        };
        runnable.run();
    }

    public static void main(String[] args) {
        // 访问成员内部类
        OuterClass outer = new OuterClass();
        OuterClass.InnerClass inner = outer.new InnerClass();
        inner.display();

        // 访问静态内部类
        OuterClass.StaticInnerClass staticInner = new OuterClass.StaticInnerClass();
        staticInner.display();

        // 访问局部内部类
        outer.methodWithLocalClass();

        // 使用匿名内部类
        outer.createAnonymousInnerClass();
    }
}