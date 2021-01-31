package com.jladder.lang;

import com.jladder.data.Record;
import junit.framework.TestCase;

public class JsonTest extends TestCase {

    public void testToJson() {




    }

    public void testToObject() {
        String json = "{sql: '(select Max(id)+1 from jtabletest)'}";

        Record record= Json.toObject(json, Record.class);


    }
}