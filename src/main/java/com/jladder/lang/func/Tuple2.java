package com.jladder.lang.func;

public class Tuple2<A,B> {
    public A item1;
    public B item2;
    public Tuple2(A item1){
        this.item1 = item1;
    }


    public Tuple2(A item1, B item2) {
        this.item1 = item1;
        this.item2 = item2;
    }
    public String toString(){
        return "(" + item1 + "," + item2 + ")";
    }

}


