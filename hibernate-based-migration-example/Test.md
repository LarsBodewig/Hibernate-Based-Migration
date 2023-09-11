# Steps to test on a local mariadb database

## Start and connect to database:

```sh
mariadbd.exe --console
mariadb.exe --user=root
```


## Create new database

```sql
create database test;
create table Fruit (name VARCHAR(20) primary key, weight integer, colorHex VARCHAR(6));
insert into Fruit (name, weight, colorHex) values ("orange", 5, "ffa500");
```


## Execute flyway

```sh
mvn flyway:baseline
mvn -P migrate test
```


## Inspect migration

```sql
select * from flyway_schema_history;
select * from Fruit;
```


## Result

```
MariaDB [test]> select version, success, description from flyway_schema_history;
+---------+---------+-----------------------+
| version | success | description           |
+---------+---------+-----------------------+
| 0       |       1 | << Flyway Baseline >> |
| 1       |       1 | Alter Fruit Color     |
+---------+---------+-----------------------+

MariaDB [test]> select * from Fruit;
+--------+--------+-----------+
| name   | weight | colorRgb  |
+--------+--------+-----------+
| orange |      5 | 255,165,0 |
+--------+--------+-----------+
```
