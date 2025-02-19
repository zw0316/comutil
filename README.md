# comutil 项目说明

## MysqlTableScriptPrinter 使用说明

### 功能概述
`MysqlTableScriptPrinter` 类用于连接到 MySQL 数据库，获取指定表的 DDL 脚本，并生成对应的 Java Bean 和 Go Struct 表示。

### 使用方法
运行该类需要提供以下命令行参数：

`java com.comutil.sql.MysqlTableScriptPrinter <host> <port> <database> <user> <password> <tables...>`
参数说明：
- `<host>`: MySQL 数据库主机地址。
- `<port>`: MySQL 数据库端口号。
- `<database>`: 要连接的数据库名称。
- `<user>`: 数据库用户名。
- `<password>`: 数据库密码。
- `<tables...>`: 要获取 DDL 脚本的表名，多个表名之间用空格分隔。
### 示例
以下是一个示例命令行：
`java com.comutil.sql.MysqlTableScriptPrinter localhost 3306 mydatabase root mypassword table1 table2 table3`
该命令将连接到 `localhost` 上的 `3306` 端口，使用 `mydatabase` 数据库，使用 `root` 用户和 `mypassword` 密码，获取 `table1`、`table2` 和 `table3` 表的 DDL 脚本，并生成对应的 Java Bean 和 Go Struct 表示。

