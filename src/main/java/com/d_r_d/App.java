package com.d_r_d;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args){
        LinklessTree<Integer> tree = new LinklessTree<>();
        for (int i = 0; i < 100; i++) {
            tree.insert(i);
        }
        System.out.println(tree.contains(50));
        System.out.println(tree.findIndex(42));
        System.out.println(tree.delete(21));
    }
}

