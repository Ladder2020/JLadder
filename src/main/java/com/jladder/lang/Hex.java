package com.jladder.lang;

public class Hex {
    public  static  String byteToStr( byte [] byteArray) {
        String strDigest =  "" ;
        for  ( int  i =  0 ; i < byteArray.length; i++) {
            strDigest += byteToHexStr(byteArray[i]);
        }
        return  strDigest;
    }
    /**
     * 将字节转换为十六进制字符串
     * @param mByte
     * @return
     */
    private  static  String byteToHexStr( byte  mByte) {
        char [] Digit = {  '0' ,  '1' ,  '2' ,  '3' ,  '4' ,  '5' ,  '6' ,  '7' ,  '8' ,  '9' ,  'A' ,  'B' ,  'C' ,  'D' ,  'E' ,  'F'  };
        char [] tempArr =  new  char [ 2 ];
        tempArr[ 0 ] = Digit[(mByte >>>  4 ) &  0X0F ];
        tempArr[ 1 ] = Digit[mByte &  0X0F ];
        String s =  new  String(tempArr);
        return  s;
    }
}
