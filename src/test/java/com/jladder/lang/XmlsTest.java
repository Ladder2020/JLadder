package com.jladder.lang;

import junit.framework.TestCase;
import org.w3c.dom.Element;

import java.io.File;
import java.util.Map;

public class XmlsTest extends TestCase {

    public void testToMap() {

        Element element = Xmls.readXML(new File("D:\\1.xml")).getDocumentElement();

        Map<String, Object> map = Xmls.toMap(element);
        System.out.println(Json.toJson(map));
    }
}