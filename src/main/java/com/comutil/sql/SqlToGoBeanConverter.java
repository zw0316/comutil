package com.comutil.sql;

import java.util.ArrayList;
import java.util.List;

// 列信息类
class GoColumn {
    String name;
    String type;

    GoColumn(String name, String type) {
        this.name = name;
        this.type = type;
    }
}

// 工具类
public class SqlToGoBeanConverter {

    // 将 SQL 类型映射到 Go 类型
    private static String mapSqlTypeToGoType(String sqlType) {
        sqlType = sqlType.toLowerCase();
        if (sqlType.contains("int")) {
            return "int";
        } else if (sqlType.contains("varchar") || sqlType.contains("char")) {
            return "string";
        } else if (sqlType.contains("datetime") || sqlType.contains("timestamp") || sqlType.contains("date")) {
            return "time.Time";
        } else if (sqlType.contains("decimal")) {
            return "float64";
        }
        return "interface{}";
    }

    // 提取表名
    private static String extractTableName(String sql) {
        int startIndex = sql.indexOf("CREATE TABLE ") + "CREATE TABLE ".length();
        int endIndex = sql.indexOf("(", startIndex);
        if (startIndex != -1 && endIndex != -1) {
            return sql.substring(startIndex, endIndex).trim();
        }
        return null;
    }

    // 提取列信息
    private static List<GoColumn> extractColumns(String sql) {
        List<GoColumn> columns = new ArrayList<>();
        int startIndex = sql.indexOf("(") + 1;
        int endIndex = sql.lastIndexOf(")");
        if (startIndex != -1 && endIndex != -1) {
            String columnsPart = sql.substring(startIndex, endIndex);
            String[] columnDefs = columnsPart.split(",");
            for (String columnDef : columnDefs) {
                if (columnDef.toUpperCase().trim().startsWith("PRIMARY") || columnDef.toUpperCase().trim().startsWith("UNIQUE") || columnDef.toUpperCase().trim().startsWith("KEY")) {
                    continue;
                }
                String[] parts = columnDef.trim().split("\\s+");
                if (parts.length >= 2) {
                    if (parts[0].contains(")") || parts[0].contains("(")) {
                        continue;
                    }
                    columns.add(new GoColumn(parts[0], parts[1]));
                }
            }
        }
        return columns;
    }

    // 将 SQL 转换为 Go 结构体代码
    public static String convertToGoBean(String sql) {
        sql = sql.replaceAll("`", "");
        String tableName = extractTableName(sql);
        if (tableName == null) {
            return null;
        }
        String structName = toTitleCase(tableName);
        List<GoColumn> columns = extractColumns(sql);

        StringBuilder goCode = new StringBuilder();
        goCode.append("package main\n\n");
        goCode.append("import \"time\"\n\n");
        goCode.append("type ").append(structName).append(" struct {\n");

        // 生成成员变量
        for (GoColumn column : columns) {
            String fieldName = toTitleCase(column.name);
            String goType = mapSqlTypeToGoType(column.type);
            goCode.append("    ").append(fieldName).append(" ").append(goType).append(" `json:\"").append(fieldName).append("\"`\n");
        }

        goCode.append("}\n");
        return goCode.toString();
    }

    // 将字符串首字母大写
    private static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder camelCase = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : input.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    camelCase.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    camelCase.append(Character.toLowerCase(c));
                }
            }
        }
        return camelCase.toString();
    }

    public static void main(String[] args) {
        String sql = """
                CREATE TABLE `TM_PLAN` (
                          `ORG` char(12) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '机构号',
                          `PLAN_ID` bigint NOT NULL COMMENT '信用计划ID',
                          `ACCT_NO` bigint NOT NULL COMMENT '账户编号',
                          `PRODUCT_CD` varchar(6) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '产品编号',
                          `CARD_NO` varchar(19) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '卡号',
                          `PLAN_TYPE` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '信用计划类型 : ///@cn.webank.cnc.cps.core.param.def.enums.CorePlanType',
                          `PLAN_NBR` char(6) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '信用计划号 : 来自信用计划模板中的PLAN_ID，用来表示信用计划类型',
                          `REF_NBR` varchar(23) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '交易参考号',
                          `PLAN_ADD_DATE` date NOT NULL COMMENT '信用计划建立日期',
                          `PAID_OUT_DATE` date DEFAULT NULL COMMENT '还清日期',
                          `USE_PLAN_RATE` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '是否使用plan的利率 : ///@cn.webank.cnc.cps.core.param.def.enums.CoreIndicatorEnum',
                          `TERM` int DEFAULT NULL COMMENT '期数',
                          `INT_CALC_BASE` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '计息基数 : ///@cn.webank.cnc.cps.core.param.def.enums.CoreInterestCalcBase',
                          `CREATED_DATETIME` datetime DEFAULT NULL COMMENT '创建时间 : ///@create',
                          `LAST_MODIFIED_DATETIME` datetime DEFAULT NULL COMMENT '修改时间 : ///@update',
                          `JPA_VERSION` int NOT NULL COMMENT '乐观锁版本号'
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='信用计划表'
                """;
        String raw = convertToGoBean(sql);
        System.out.println(raw);
    }
}
