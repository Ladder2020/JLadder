package com.jladder.lang;
import java.lang.reflect.Type;

public abstract class TypeReference<T> implements Type{


    private final Type type = Types.getTypeArgument(this.getClass());

    public TypeReference() {
//        Type superClass = this.getClass().getGenericSuperclass();
//        Type type = ((ParameterizedType)superClass).getActualTypeArguments()[0];
//        System.out.println(type);
////        Type cachedType = (Type)classTypeCache.get(type);
////        if (cachedType == null) {
////            classTypeCache.putIfAbsent(type, type);
////            cachedType = (Type)classTypeCache.get(type);
////        }
////
////        this.type = cachedType;
    }

    public Type getType() {
        return this.type;
    }


    public String getName(){
        return ((Class)this.type).getName();
    }

    public String toString() {
        return this.type.toString();
    }
}
