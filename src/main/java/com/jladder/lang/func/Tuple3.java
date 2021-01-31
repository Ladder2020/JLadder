package com.jladder.lang.func;

public class Tuple3<A,B,C> {
    public A item1;
    public B item2;
    public C item3;

    public Tuple3(A item1){
        this.item1 = item1;
    }
    public Tuple3(A item1, B item2){
        this.item1 = item1;
        this.item2 = item2;
    }
    public Tuple3(A item1, B item2, C item3) {
        this.item1 = item1;
        this.item2 = item2;
        this.item3 = item3;
    }
    public String toString(){
        return "(" + item1 + "," + item2 + "," + item3  + ")";
    }

}


