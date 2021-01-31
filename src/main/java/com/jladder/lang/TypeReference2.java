package com.jladder.lang;
import java.lang.reflect.Type;


public class TypeReference2<T> implements Type {


    private final Type type = Types.getTypeArgument(this.getClass());

    public TypeReference2() {
    }

    public Type getType() {
        return this.type;
    }

    public String toString() {
        return this.type.toString();
    }
}