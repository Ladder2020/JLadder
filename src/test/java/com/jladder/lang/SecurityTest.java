package com.jladder.lang;

import junit.framework.TestCase;

public class SecurityTest extends TestCase {

    public void testEncryptByBase2() {

        for (int i=0;i<1000;i++){
            String str = Core.genUuid();
            String str1 = Security.encryptByBase2(str);
            //System.out.println(str1);
            String str2 = Security.encryptByBase2(str1);
            //System.out.println(str2);
            if(!str.equals(str2)){
                System.out.println("错误");
            }
        }


    }

    public void testDecryptByBase2() {
        System.out.println(Security.decryptByBase2("1259eoiR5piv6IKW5pit6Ziz"));
    }
}