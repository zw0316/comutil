package com.comutil.sql;

public class Sql2BeanConverter {
    public static void main(String[] args) {
        String type = args[0];
        String raw = args[1];
        if (type.equals("java")) {
            new SqlToJavaBeanConverter().convertToJavaBean(raw);
        } else if (type.equals("go")) {
            new SqlToGoBeanConverter().convertToGoBean(raw);
        }
    }
}
