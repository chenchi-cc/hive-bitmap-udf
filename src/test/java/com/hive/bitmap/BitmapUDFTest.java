package com.hive.bitmap;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.junit.Before;
import org.junit.Test;


public class BitmapUDFTest {

    private SparkConf sparkConf = new SparkConf().setAppName("build job").set("log.level", "ERROR");

    private SparkSession spark = SparkSession.builder().enableHiveSupport()
            .master("local")
            .config(sparkConf).getOrCreate();

    @Before
    public void init() {

        spark.sql("CREATE TEMPORARY FUNCTION to_bitmap AS 'com.hive.bitmap.udf.ToBitmapUDAF'");
        spark.sql("CREATE TEMPORARY FUNCTION bitmap_union AS 'com.hive.bitmap.udf.BitmapUnionUDAF'");
        spark.sql("CREATE TEMPORARY FUNCTION bitmap_count AS 'com.hive.bitmap.udf.BitmapCountUDF'");
        spark.sql("CREATE TEMPORARY FUNCTION bitmap_and AS 'com.hive.bitmap.udf.BitmapAndUDF'");
        spark.sql("CREATE TEMPORARY FUNCTION bitmap_or AS 'com.hive.bitmap.udf.BitmapOrUDF'");
        spark.sql("CREATE TEMPORARY FUNCTION bitmap_xor AS 'com.hive.bitmap.udf.BitmapXorUDF'");
        spark.sql("CREATE TEMPORARY FUNCTION bitmap_to_array AS 'com.hive.bitmap.udf.BitmapToArrayUDF'");
        spark.sql("CREATE TEMPORARY FUNCTION bitmap_from_array AS 'com.hive.bitmap.udf.BitmapFromArrayUDF'");
        spark.sql("CREATE TEMPORARY FUNCTION bitmap_contains AS 'com.hive.bitmap.udf.BitmapContainsUDF'");
        spark.sql("CREATE TEMPORARY FUNCTION bitmap_intersect as 'com.hive.bitmap.udf.BitmapIntersectUDAF'");
    }

    @Test
    public void bitmapToArrayUDFTest() {
        spark.sql("select bitmap_count(bitmap_from_array(array(1,2,3,4,5))) AS `cnt=5`").show();
        spark.sql("select bitmap_to_array(bitmap_from_array(array(1,2,3,4,5)))").show();
        spark.sql("select bitmap_to_array(bitmap_and(bitmap_from_array(array(1,2,3,4,5)),bitmap_from_array(array(1,2))))").show();
        spark.sql("select bitmap_to_array(bitmap_or(bitmap_from_array(array(1,2,3)),bitmap_from_array(array(5))))").show();
        spark.sql("select bitmap_to_array(bitmap_xor(bitmap_from_array(array(1,2,3)),bitmap_from_array(array(3))))").show();
    }

    @Test
    public void bitmapContainsUDFTest() {
        spark.sql("select bitmap_contains(bitmap_from_array(array(1,2,3)),2)").show();
        spark.sql("select bitmap_contains(bitmap_from_array(array(1,2,3)),bitmap_from_array(array(1,2,3)))").show();
        spark.sql("select bitmap_contains(bitmap_from_array(array(1,2,3)),bitmap_from_array(array(1,2,3,4)))").show();
        spark.sql("select bitmap_contains(bitmap_from_array(array(1,2,3)),cast( null as binary))").show();
    }

    @Test
    public void bitmapUnionIntersectUDAFTest() {
        String s = "select\n" +
                "\tbitmap_to_array(bitmap_intersect(val)) as `r1=[3]`,\n" +
                "\tbitmap_to_array(bitmap_union(val)) as `r2=[1,2,3,5,6]`\n" +
                "from\n" +
                "(\n" +
                "\tselect bitmap_from_array(array(1,2,3)) as val\n" +
                "\tunion all\n" +
                "\tselect bitmap_from_array(array(1,3,5)) as val\n" +
                "\tunion all\n" +
                "\tselect bitmap_from_array(array(2,3,6)) as val\n" +
                ")t";
        spark.sql(s).show();
    }
}
