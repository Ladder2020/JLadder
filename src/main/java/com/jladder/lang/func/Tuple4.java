package com.jladder.lang.func;

public class Tuple4<A,B,C,D> {
    public A item1;
    public B item2;
    public C item3;
    public D item4;

    public Tuple4(A item1){
        this.item1 = item1;
    }
    public Tuple4(A item1, B item2){
        this.item1 = item1;
        this.item2 = item2;
    }
    public Tuple4(A item1, B item2, C item3,D item4) {
        this.item1 = item1;
        this.item2 = item2;
        this.item3 = item3;
        this.item4 = item4;
    }
    public String toString(){
        return "(" + item1 + "," + item2 + "," + item3  + "," + item4 + ")";
    }

}


