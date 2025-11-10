package com.example.controller;

import java.util.*;

class Item {
    String name;
    int price;

    Item(String name, int price) {
        this.name = name;
        this.price = price;
    }
}

class Combo {
    String name;
    List<Item> items;
    int comboPrice;

    Combo(String name, List<Item> items, int comboPrice) {
        this.name = name;
        this.items = items;
        this.comboPrice = comboPrice;
    }
}

class Order {
    List<Item> itemList = new ArrayList<>();
    List<Combo> comboList = new ArrayList<>();

    void addItem(Item item) {
        itemList.add(item);
    }

    void addCombo(Combo combo) {
        comboList.add(combo);
    }

    int calculateTotal() {
        int total = 0;
        for (Item item : itemList) {
            total += item.price;
        }
        for (Combo combo : comboList) {
            total += combo.comboPrice;
        }
        return total;
    }
}

class Customer {
    String name;
    Order order;

    Customer(String name) {
        this.name = name;
        this.order = new Order();
    }

    int checkout() {
        return order.calculateTotal();
    }
}

public class InterviewTest {
    public static void main(String[] args) {
        // 单品
        Item beefNoodleL = new Item("大碗牛肉面", 18);
        Item beefNoodleM = new Item("中碗牛肉面", 16);
        Item beefNoodleS = new Item("小碗牛肉面", 14);

        Item intestineNoodleL = new Item("大碗肥肠面", 20);
        Item intestineNoodleM = new Item("中碗肥肠面", 18);
        Item intestineNoodleS = new Item("小碗肥肠面", 16);

        Item beefPie = new Item("牛肉饼", 10);
        Item milkTea = new Item("奶茶", 12);

        // 套餐
        Combo set1 = new Combo("套餐1",
                Arrays.asList(beefNoodleL, beefPie, milkTea), 38);
        Combo set2 = new Combo("套餐2",
                Arrays.asList(intestineNoodleL, beefPie, milkTea), 40);

        // 张三订单
        Customer zhangSan = new Customer("张三");
        zhangSan.order.addCombo(set1);
        zhangSan.order.addItem(beefPie);

        // 李四订单
        Customer liSi = new Customer("李四");
        liSi.order.addItem(intestineNoodleM);
        liSi.order.addItem(milkTea);
        liSi.order.addItem(milkTea);

        // 输出账单
        System.out.println(zhangSan.name + "需要付款：" + zhangSan.checkout() + "元");
        System.out.println(liSi.name + "需要付款：" + liSi.checkout() + "元");
    }
}