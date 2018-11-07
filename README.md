# hadoop-final-project

2 . **Flume**

```
flume-ng agent -c ```conf ./conf/ ```conf-file ./conf/flume_vnykytenko.properties ```name vnykytenko -Dflume.root.logger=INFO,console
```
3 . **Flume event producer**

In module **FlumeRandomEventProducer** compile and assembly project.

After that 
```bash
java -jar FlumeRandomEventProducer-2.11.12-0.0.1-SNAPSHOT.jar
```
4 . **Hive**

4.0.1. set flags for further operations
```
SET mapred.input.dir.recursive=true;
SET hive.mapred.supports.subdirectories=true;
SET hive.input.dir.recursive=true;
SET hive.supports.subdirectories=true;
SET mapreduce.input.fileinputformat.input.dir.recursive=true;
```
4.1. create external table
```
CREATE DATABASE IF NOT EXISTS vn;
CREATE EXTERNAL TABLE purchases(`product` string,`price` int, `purchase_date` date, `category` string,`ip` string) PARTITIONED BY (`pd` string) ROW FORMAT serde 'org.apache.hadoop.hive.serde2.OpenCSVSerde' with serdeproperties ("separatopChar"=",") STORED AS TEXTFILE;
```

4.1.1. load data from hdfs

```
LOAD DATA INPATH '/user/vnykytenko/events/2018/01/01/*' INTO TABLE purchases PARTITION (pd = '2018-01-01');
LOAD DATA INPATH '/user/vnykytenko/events/2018/01/02/*' INTO TABLE purchases PARTITION (pd = '2018-01-02');
LOAD DATA INPATH '/user/vnykytenko/events/2018/01/03/*' INTO TABLE purchases PARTITION (pd = '2018-01-03');
LOAD DATA INPATH '/user/vnykytenko/events/2018/01/04/*' INTO TABLE purchases PARTITION (pd = '2018-01-04');
LOAD DATA INPATH '/user/vnykytenko/events/2018/01/05/*' INTO TABLE purchases PARTITION (pd = '2018-01-05');
LOAD DATA INPATH '/user/vnykytenko/events/2018/01/06/*' INTO TABLE purchases PARTITION (pd = '2018-01-06');
LOAD DATA INPATH '/user/vnykytenko/events/2018/01/07/*' INTO TABLE purchases PARTITION (pd = '2018-01-07');
```

or

```
ALTER TABLE purchases ADD PARTITION (pd='2018-01-01') LOCATION '/user/vnykytenko/events/2018/01/01';
ALTER TABLE purchases ADD PARTITION (pd='2018-01-02') LOCATION '/user/vnykytenko/events/2018/01/02';
ALTER TABLE purchases ADD PARTITION (pd='2018-01-03') LOCATION '/user/vnykytenko/events/2018/01/03';
ALTER TABLE purchases ADD PARTITION (pd='2018-01-04') LOCATION '/user/vnykytenko/events/2018/01/04';
ALTER TABLE purchases ADD PARTITION (pd='2018-01-05') LOCATION '/user/vnykytenko/events/2018/01/05';
ALTER TABLE purchases ADD PARTITION (pd='2018-01-06') LOCATION '/user/vnykytenko/events/2018/01/06';
ALTER TABLE purchases ADD PARTITION (pd='2018-01-07') LOCATION '/user/vnykytenko/events/2018/01/07';
```

5 . **Hive QUERIES**

5.1. top 10 categories

```
CREATE TABLE step51 ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
AS
SELECT * FROM (SELECT category, count(*) as purchase_count
FROM purchases
GROUP BY category
SORT BY purchase_count desc) a
LIMIT 10;
```

5.2. top 10 products by category

```
CREATE TABLE step52 ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
AS
SELECT * from (
SELECT category, product, rank() over ( partition by category order by value desc) as purchase_count
FROM (SELECT category, product, count(*) as value
FROM purchases
GROUP BY category,product
SORT BY value desc
) a
) t WHERE purchase_count < 10;
```

6 . **Hive 5 + GEO**

6.1 add UDF

In module **udf_ip** compile and assembly project.

```
add jar /home/vnykytenko/data/hive_udf_ip-2.11.12-0.1.jar;
CREATE temporary function is_in_range as 'com.nykytenko.IpToGeo';
```

6.1.1. check UDF

```
SELECT is_in_range('192.168.56.25', '192.168.56.0/24');
```

6.1.2. create tables

```
CREATE EXTERNAL TABLE GEO_BLOCKS (network string,geoname_id string,registered_country_geoname_id string,represented_country_geoname_id string) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE LOCATION '/user/vnykytenko/data/blocks' tblproperties ("skip.header.line.count"="1");

CREATE EXTERNAL TABLE GEO_LOCATIONS (geoname_id string,locale_code string,continent_code string,continent_name string,country_iso_code string,country_name string,is_in_european_union string) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE location '/user/vnykytenko/data/countries' tblproperties ("skip.header.line.count"="1");
```

6.2. get full joined geo_table

6.2.1.

```hql
CREATE TABLE vn.geo_joined ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' LOCATION '/user/vnykytenko/data/geo_joined/'
 as
select b.network, l.country_name, l.country_iso_code
from GEO_BLOCKS b
  left outer join GEO_LOCATIONS l
  on b.geoname_id = l.geoname_id;
```

6.2.2. get final table

```hql
CREATE TABLE vnykytenko_geo ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' as select p.product, p.price,p.category, p.ip, g.country_name, g.country_iso_code from vnykytenko p left outer join geo_joined g on true where is_in_range(p.ip, g.network);
```

6.3.

```hql
CREATE TABLE step6 ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
as
select country_name as country, sum(price) total_money_sum
from vnykytenko_geo
group by country_name
order by total_money_sum desc
limit 10;
```

7 . **Put Hive queries result to RDBMS via SQOOP**

7.1. Install any RDBMS (e.g. MySQL, PostgreSQL)

7.1.1 create tables

```mysql-psql
mysql -u root -p
CREATE database vnykytenko_db;
use vnykytenko_db;
CREATE TABLE step51 (category varchar(40), purchase_count integer);
CREATE TABLE step52 (category varchar(40), product varchar(100), purchase_count integer);
CREATE TABLE step6 (country varchar(40), total_money_sum DOUBLE);
```

7.2 Install and configure SQOOP
```
already installed
```
7.3 Put queries result from step 5 and 6 result to RDBMS. (write down RDBMS tables creation scripts and sqoop commands to export data)
```
hive -S -e "describe formatted step51 ;" | grep 'Location' | awk '{ print $NF }'
```
```
sqoop export ```connect jdbc:mysql://10.0.0.21:3306/vnykytenko_db ```username root ```password root ```table step51 ```export-dir /user/hive/warehouse/step51
sqoop export ```connect jdbc:mysql://10.0.0.21:3306/vnykytenko_db ```username root ```password root ```table step52 ```export-dir /user/hive/warehouse/step52
sqoop export ```connect jdbc:mysql://10.0.0.21:3306/vnykytenko_db ```username root ```password root ```table step6 ```export-dir /user/hive/warehouse/step6
```

8 . **Spark**

In module **Spark-Final-Project** compile and assembly project.

After that:
Use first parameter **"ds"/"rdd"** to switch between Dataset and RDD spark interfaces.

8.1 . Task 5.1 

```scala
java -jar data/Spark-Final-Project-2.12.7-0.0.1-SNAPSHOT.jar "ds" "_1" 
 
```

8.2 . Task 5.2 

```scala
java -jar data/Spark-Final-Project-2.12.7-0.0.1-SNAPSHOT.jar "ds" "_2" 
```

8.3 . Task 6 

```scala
java -jar data/Spark-Final-Project-2.12.7-0.0.1-SNAPSHOT.jar "ds" "_3" 
```

