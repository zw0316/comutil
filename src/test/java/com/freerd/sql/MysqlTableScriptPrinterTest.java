package com.freerd.sql;

import org.junit.Test;

import com.comutil.sql.MysqlTableScriptPrinter;

public class MysqlTableScriptPrinterTest {


    @Test
    public void testGetAllTables() {
        String[] args = {"localhost", "3306", "bcpsdb", "root", "12345678"};
        MysqlTableScriptPrinter.main(args);
    }
}
