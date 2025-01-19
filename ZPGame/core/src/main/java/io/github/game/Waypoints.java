package io.github.game;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Waypoints {

    private ShapeRenderer shapeRenderer;

    private Array<RailwayPath> railwayPaths;


    private Array<Junction> junctions;


    public Waypoints(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
        this.junctions = new Array<>();
        this.railwayPaths = new Array<>();
        initializeJunctions();
        initializePaths();

    }

    private void initializePaths() {

        Array<Vector2> zeroOnePoints = new Array<>();
        zeroOnePoints.add(new Vector2(504.2126f, 1403.8019f));
        zeroOnePoints.add(new Vector2(561.4543f, 1371.0337f));
        zeroOnePoints.add(new Vector2(594.2479f, 1317.9905f));
        zeroOnePoints.add(new Vector2(636.7184f, 1287.8848f));
        zeroOnePoints.add(new Vector2(668.9744f, 1276.416f));
        zeroOnePoints.add(new Vector2(679.72644f, 1243.4431f));
        zeroOnePoints.add(new Vector2(718.9713f, 1226.9568f));
        zeroOnePoints.add(new Vector2(750.68976f, 1211.9039f));
        zeroOnePoints.add(new Vector2(777.56976f, 1183.9487f));
        zeroOnePoints.add(new Vector2(807.6754f, 1155.9934f));
        zeroOnePoints.add(new Vector2(819.5027f, 1138.0734f));
        zeroOnePoints.add(new Vector2(813.0514f, 1101.5166f));
        zeroOnePoints.add(new Vector2(814.86884f, 1079.654f));
        zeroOnePoints.add(new Vector2(809.1088f, 1052.8253f));
        zeroOnePoints.add(new Vector2(804.27045f, 1021.2861f));
        zeroOnePoints.add(new Vector2(798.3568f, 991.8973f));

        RailwayPath ZeroOne = new RailwayPath("0-1", zeroOnePoints);
        railwayPaths.add(ZeroOne);


        Array<Vector2> OneTwoPoints = new Array<>();

        OneTwoPoints.add(new Vector2(798.3568f, 991.8973f));
        OneTwoPoints.add(new Vector2(758.9588f, 958.00323f));
        OneTwoPoints.add(new Vector2(720.9428f, 899.43036f));
        OneTwoPoints.add(new Vector2(695.59875f, 839.73114f));
        OneTwoPoints.add(new Vector2(703.00006f, 728.0001f));
        OneTwoPoints.add(new Vector2(710.00006f, 673.0001f));


        RailwayPath oneTwo = new RailwayPath("1-2", OneTwoPoints);
        railwayPaths.add(oneTwo);


        Array<Vector2> oneThreePoints = new Array<>();
        oneThreePoints.add(new Vector2(798.3568f, 991.8973f));
        oneThreePoints.add(new Vector2(850.00507f, 954.93097f));
        oneThreePoints.add(new Vector2(893.7811f, 905.3182f));
        oneThreePoints.add(new Vector2(964.55237f, 884.88934f));

        RailwayPath oneThree = new RailwayPath("1-3", oneThreePoints);
        railwayPaths.add(oneThree);

        Array<Vector2> twoFourPoints = new Array<>(); //gre iz ljubljanaKoperPoints
        twoFourPoints.add(new Vector2(710.00006f, 673.0001f));
        twoFourPoints.add(new Vector2(667.00006f, 631.0001f));
        twoFourPoints.add(new Vector2(553.00006f, 672.0001f));
        twoFourPoints.add(new Vector2(520.00006f, 740.0001f));
        twoFourPoints.add(new Vector2(458.00006f, 828.0002f));
        twoFourPoints.add(new Vector2(393.00006f, 857.00024f));

        RailwayPath twoFourPath = new RailwayPath("2-4", twoFourPoints);
        railwayPaths.add(twoFourPath);

        Array<Vector2> twoFivePoints = new Array<>(); //gre iz ljubljanaKoperPoints
        twoFivePoints.add(new Vector2(710.00006f, 673.0001f));
        twoFivePoints.add(new Vector2(754.00006f, 669.0001f));
        twoFivePoints.add(new Vector2(835.0001f, 665.0001f));
        twoFivePoints.add(new Vector2(933.0001f, 636.0001f));
        twoFivePoints.add(new Vector2(972.0001f, 652.0001f));

        RailwayPath twoFivePath = new RailwayPath("2-5", twoFivePoints);
        railwayPaths.add(twoFivePath);


        Array<Vector2> fourSixPoints = new Array<>();
        fourSixPoints.add(new Vector2(393.00006f, 857.00024f));
        fourSixPoints.add(new Vector2(425.00006f, 897.0002f));
        fourSixPoints.add(new Vector2(432.00012f, 936.00024f));
        fourSixPoints.add(new Vector2(421.00006f, 990.00024f));
        fourSixPoints.add(new Vector2(379.00003f, 1018.0002f));
        fourSixPoints.add(new Vector2(345.99997f, 1048.0002f));
        fourSixPoints.add(new Vector2(338.00003f, 1089.0002f));
        fourSixPoints.add(new Vector2(358.99997f, 1129.0001f));
        fourSixPoints.add(new Vector2(398.00006f, 1146.0002f));
        fourSixPoints.add(new Vector2(446.00006f, 1151.0002f));
        fourSixPoints.add(new Vector2(487.00006f, 1153.0002f));
        fourSixPoints.add(new Vector2(529.00006f, 1121.0002f));
        fourSixPoints.add(new Vector2(594.00006f, 1103.0001f));
        fourSixPoints.add(new Vector2(628.00006f, 1113.0001f));

        RailwayPath fourSixPath = new RailwayPath("4-6", fourSixPoints);
        railwayPaths.add(fourSixPath);


        Array<Vector2> fourSevenPoints = new Array<>();
        fourSevenPoints.add(new Vector2(393.00006f, 857.00024f));
        fourSevenPoints.add(new Vector2(307.99997f, 830.0002f));
        fourSevenPoints.add(new Vector2(291.00003f, 785.0001f));
        fourSevenPoints.add(new Vector2(278.00003f, 741.0001f));
        fourSevenPoints.add(new Vector2(271.00003f, 698.0001f));
        fourSevenPoints.add(new Vector2(249.00003f, 659.00006f));
        fourSevenPoints.add(new Vector2(206.00003f, 640.0001f));
        fourSevenPoints.add(new Vector2(175.00002f, 637.0001f));

        RailwayPath fourSevenPath = new RailwayPath("4-7", fourSevenPoints);
        railwayPaths.add(fourSevenPath);
//
//        Array<Vector2> threeFivePoints = new Array<>();
//        threeFivePoints.add(new Vector2(964.55237f, 884.88934f));
//        threeFivePoints.add(new Vector2(986.0001f, 870.0002f));
//        threeFivePoints.add(new Vector2(997.0001f, 842.00024f));
//        threeFivePoints.add(new Vector2(996.0001f, 806.0001f));
//        threeFivePoints.add(new Vector2(996.0001f, 770.0002f));
////        threeFivePoints.add(new Vector2(994.0001f, 722.0001f));
////        threeFivePoints.add(new Vector2(986.0001f, 681.0001f));
////        threeFivePoints.add(new Vector2(972.0001f, 652.0001f));

     //   RailwayPath threeFivePath = new RailwayPath("3-5", threeFivePoints);
       // railwayPaths.add(threeFivePath);


        Array<Vector2> threeTwentyfourPoints = new Array<>();
        threeTwentyfourPoints.add(new Vector2(964.55237f, 884.88934f));
        threeTwentyfourPoints.add(new Vector2(986.0001f, 870.0002f));
        threeTwentyfourPoints.add(new Vector2(997.0001f, 842.00024f));
        threeTwentyfourPoints.add(new Vector2(996.0001f, 806.0001f));
        threeTwentyfourPoints.add(new Vector2(996.0001f, 770.0002f));

        RailwayPath threeTwentyfourPath = new RailwayPath("3-24", threeTwentyfourPoints);
        railwayPaths.add(threeTwentyfourPath);

        Array<Vector2> fiveTwentyfourPoints = new Array<>();
        fiveTwentyfourPoints.add(new Vector2(972.0001f, 652.0001f));
        fiveTwentyfourPoints.add(new Vector2(986.0001f, 681.0001f));
        fiveTwentyfourPoints.add(new Vector2(981.0001f, 756.0001f));
        fiveTwentyfourPoints.add(new Vector2(996.0001f, 770.0002f));
        RailwayPath fiveTwentyfourPath = new RailwayPath("5-24", fiveTwentyfourPoints);
        railwayPaths.add(fiveTwentyfourPath);


        Array<Vector2> twentyFourTwentyFivePoints = new Array<>();
        twentyFourTwentyFivePoints.add(new Vector2(996.0001f, 770.0002f));
        twentyFourTwentyFivePoints.add(new Vector2(1020.0001f, 772.0002f));
        twentyFourTwentyFivePoints.add(new Vector2(1056.0001f, 770.0002f));
        twentyFourTwentyFivePoints.add(new Vector2(1101.0001f, 766.0001f));
        twentyFourTwentyFivePoints.add(new Vector2(1128.0001f, 764.00006f));
        twentyFourTwentyFivePoints.add(new Vector2(1159.0001f, 757.0001f));

        RailwayPath twentyFourTwentyFivePath = new RailwayPath("24-25", twentyFourTwentyFivePoints);
        railwayPaths.add(twentyFourTwentyFivePath);



        Array<Vector2> threeEightPoints = new Array<>();
        threeEightPoints.add(new Vector2(964.55237f, 884.88934f));
        threeEightPoints.add(new Vector2(1021.0001f, 875.0002f));
        threeEightPoints.add(new Vector2(1066.0001f, 888.0002f));
        threeEightPoints.add(new Vector2(1105.0001f, 923.0002f));
        threeEightPoints.add(new Vector2(1128.0001f, 952.00024f));
        threeEightPoints.add(new Vector2(1145.0001f, 976.0002f));
        threeEightPoints.add(new Vector2(1163.0001f, 1008.0002f));
        threeEightPoints.add(new Vector2(1179.0001f, 1027.0002f));

        RailwayPath threeEightPath = new RailwayPath("3-8", threeEightPoints);
        railwayPaths.add(threeEightPath);


        Array<Vector2> fiveNinePoints = new Array<>();
        fiveNinePoints.add(new Vector2(972.0001f, 652.0001f));
        fiveNinePoints.add(new Vector2(992.0001f, 639.0001f));
        fiveNinePoints.add(new Vector2(1004.00006f, 603.0001f));
        fiveNinePoints.add(new Vector2(1005.0001f, 546.00006f));
        fiveNinePoints.add(new Vector2(998.0001f, 505.00012f));
        fiveNinePoints.add(new Vector2(989.0001f, 464.0001f));


        RailwayPath fiveNinePath = new RailwayPath("5-9", fiveNinePoints);
        railwayPaths.add(fiveNinePath);


        Array<Vector2> nineTenPoints = new Array<>();
        nineTenPoints.add(new Vector2(989.0001f, 464.0001f));
        nineTenPoints.add(new Vector2(955.0001f, 438.0001f));
        nineTenPoints.add(new Vector2(919.0001f, 419.0001f));
        nineTenPoints.add(new Vector2(849.0001f, 411.0001f));
        nineTenPoints.add(new Vector2(810.0001f, 417.0001f));
        nineTenPoints.add(new Vector2(774.0001f, 420.0001f));
        nineTenPoints.add(new Vector2(714.00006f, 407.0001f));


        RailwayPath nineTenPath = new RailwayPath("9-10", nineTenPoints);
        railwayPaths.add(nineTenPath);


        Array<Vector2> nineElevenPoints = new Array<>();
        nineElevenPoints.add(new Vector2(989.0001f, 464.0001f));
        nineElevenPoints.add(new Vector2(1024.0001f, 427.0001f));
        nineElevenPoints.add(new Vector2(1100.0001f, 388.0001f));
        nineElevenPoints.add(new Vector2(1127.0001f, 385.0001f));
        nineElevenPoints.add(new Vector2(1163.0001f, 386.0001f));
        nineElevenPoints.add(new Vector2(1208.0001f, 399.0001f));
        nineElevenPoints.add(new Vector2(1248.0001f, 403.00006f));


        RailwayPath nineElevenPath = new RailwayPath("9-11", nineElevenPoints);
        railwayPaths.add(nineElevenPath);


        Array<Vector2> tenTwelvePoints = new Array<>();
        tenTwelvePoints.add(new Vector2(714.00006f, 407.0001f));
        tenTwelvePoints.add(new Vector2(659.00006f, 419.0001f));
        tenTwelvePoints.add(new Vector2(614.00006f, 436.0001f));
        tenTwelvePoints.add(new Vector2(575.00006f, 458.00012f));
        tenTwelvePoints.add(new Vector2(544.00006f, 475.0001f));
        tenTwelvePoints.add(new Vector2(510.00006f, 474.00012f));
        tenTwelvePoints.add(new Vector2(469.00006f, 468.00012f));
        tenTwelvePoints.add(new Vector2(437.00012f, 455.00012f));
        tenTwelvePoints.add(new Vector2(418.00006f, 438.0001f));

        RailwayPath tenTwelvePath = new RailwayPath("10-12", tenTwelvePoints);
        railwayPaths.add(tenTwelvePath);


        Array<Vector2> tenThirteenPoints = new Array<>();
        tenThirteenPoints.add(new Vector2(714.00006f, 407.0001f));
        tenThirteenPoints.add(new Vector2(685.00006f, 376.0001f));
        tenThirteenPoints.add(new Vector2(664.00006f, 330.00006f));
        tenThirteenPoints.add(new Vector2(653.00006f, 286.00006f));
        tenThirteenPoints.add(new Vector2(654.00006f, 257.00003f));
        tenThirteenPoints.add(new Vector2(657.00006f, 212.00003f));
        tenThirteenPoints.add(new Vector2(653.00006f, 170.00008f));
        tenThirteenPoints.add(new Vector2(640.00006f, 132.00002f));

        RailwayPath tenThirteenPath = new RailwayPath("10-13", tenThirteenPoints);
        railwayPaths.add(tenThirteenPath);


        Array<Vector2> elevenFourteenPoints = new Array<>();
        elevenFourteenPoints.add(new Vector2(1248.0001f, 403.00006f));
        elevenFourteenPoints.add(new Vector2(1284.0001f, 444.0001f));
        elevenFourteenPoints.add(new Vector2(1307.0001f, 495.00012f));
        elevenFourteenPoints.add(new Vector2(1319.0001f, 527.00006f));
        elevenFourteenPoints.add(new Vector2(1330.0001f, 563.0001f));
        elevenFourteenPoints.add(new Vector2(1359.0001f, 592.0001f));
        elevenFourteenPoints.add(new Vector2(1405.0001f, 609.00006f));
        elevenFourteenPoints.add(new Vector2(1442.0001f, 618.0001f));
        elevenFourteenPoints.add(new Vector2(1478.0001f, 625.0001f));

        RailwayPath elevenFourteenPath = new RailwayPath("11-14", elevenFourteenPoints);
        railwayPaths.add(elevenFourteenPath);


        Array<Vector2> elevenFifteenPoints = new Array<>();
        elevenFifteenPoints.add(new Vector2(1248.0001f, 403.00006f));
        elevenFifteenPoints.add(new Vector2(1286.0001f, 393.0001f));
        elevenFifteenPoints.add(new Vector2(1335.0001f, 375.00006f));
        elevenFifteenPoints.add(new Vector2(1416.0001f, 358.00003f));
        elevenFifteenPoints.add(new Vector2(1493.0001f, 346.00006f));
        elevenFifteenPoints.add(new Vector2(1522.0001f, 361.0001f));
        elevenFifteenPoints.add(new Vector2(1558.0002f, 379.00003f));
        elevenFifteenPoints.add(new Vector2(1611.0002f, 423.0001f));
        elevenFifteenPoints.add(new Vector2(1647.0002f, 463.00012f));
        elevenFifteenPoints.add(new Vector2(1676.0002f, 505.00012f));
        elevenFifteenPoints.add(new Vector2(1695.0002f, 539.0001f));

        RailwayPath elevenFifteenPath = new RailwayPath("11-15", elevenFifteenPoints);
        railwayPaths.add(elevenFifteenPath);


        Array<Vector2> fifteenSixteenPoints = new Array<>();
        fifteenSixteenPoints.add(new Vector2(1695.0002f, 539.0001f));
        fifteenSixteenPoints.add(new Vector2(1715.0002f, 571.0001f));
        fifteenSixteenPoints.add(new Vector2(1753.0002f, 586.0001f));
        fifteenSixteenPoints.add(new Vector2(1802.0002f, 588.00006f));
        fifteenSixteenPoints.add(new Vector2(1847.0002f, 586.0001f));
        fifteenSixteenPoints.add(new Vector2(1883.0002f, 581.0001f));
        fifteenSixteenPoints.add(new Vector2(1903.0002f, 581.0001f));

        RailwayPath fifteenSixteenPath = new RailwayPath("15-16", fifteenSixteenPoints);
        railwayPaths.add(fifteenSixteenPath);


        Array<Vector2> fifteenFourteenPoints = new Array<>();
        fifteenFourteenPoints.add(new Vector2(1695.0002f, 539.0001f));
        fifteenFourteenPoints.add(new Vector2(1718.0002f, 601.00006f));
        fifteenFourteenPoints.add(new Vector2(1716.0002f, 635.0001f));
        fifteenFourteenPoints.add(new Vector2(1659.0002f, 649.0001f));
        fifteenFourteenPoints.add(new Vector2(1604.0002f, 639.0001f));
        fifteenFourteenPoints.add(new Vector2(1552.0002f, 634.0001f));
        fifteenFourteenPoints.add(new Vector2(1516.0001f, 633.0001f));
        fifteenFourteenPoints.add(new Vector2(1496.0001f, 631.0001f));
        fifteenFourteenPoints.add(new Vector2(1478.0001f, 625.0001f));

        RailwayPath fifteenFourteenPath = new RailwayPath("15-14", fifteenFourteenPoints);
        railwayPaths.add(fifteenFourteenPath);


        Array<Vector2> fourteenEighteenPoints = new Array<>();
        fourteenEighteenPoints.add(new Vector2(1478.0001f, 625.0001f));
        fourteenEighteenPoints.add(new Vector2(1489.0001f, 655.0001f));
        fourteenEighteenPoints.add(new Vector2(1483.0001f, 692.0001f));
        fourteenEighteenPoints.add(new Vector2(1482.0001f, 723.0001f));
        fourteenEighteenPoints.add(new Vector2(1482.0001f, 750.0001f));
        fourteenEighteenPoints.add(new Vector2(1486.0001f, 784.00024f));
        fourteenEighteenPoints.add(new Vector2(1489.0001f, 811.0001f));
        fourteenEighteenPoints.add(new Vector2(1494.0001f, 851.0002f));

        RailwayPath fourteenEighteenPath = new RailwayPath("14-18", fourteenEighteenPoints);
        railwayPaths.add(fourteenEighteenPath);


        Array<Vector2> eighteenTwentyPoints = new Array<>();
        eighteenTwentyPoints.add(new Vector2(1494.0001f, 851.0002f));
        eighteenTwentyPoints.add(new Vector2(1506.0001f, 881.0002f));
        eighteenTwentyPoints.add(new Vector2(1515.0001f, 907.0002f));
        eighteenTwentyPoints.add(new Vector2(1544.0002f, 936.00024f));
        eighteenTwentyPoints.add(new Vector2(1565.0002f, 966.0002f));
        eighteenTwentyPoints.add(new Vector2(1582.0002f, 1007.0002f));
        eighteenTwentyPoints.add(new Vector2(1597.0002f, 1045.0001f));
        eighteenTwentyPoints.add(new Vector2(1604.0002f, 1107.0001f));
        eighteenTwentyPoints.add(new Vector2(1595.0002f, 1134.0001f));
        eighteenTwentyPoints.add(new Vector2(1581.0002f, 1163.0002f));
        eighteenTwentyPoints.add(new Vector2(1556.0002f, 1192.0001f));
        eighteenTwentyPoints.add(new Vector2(1545.0002f, 1206.0002f));

        RailwayPath eighteenTwentyPath = new RailwayPath("18-20", eighteenTwentyPoints);
        railwayPaths.add(eighteenTwentyPath);


        Array<Vector2> eighteenNineteenPoints = new Array<>();
        eighteenNineteenPoints.add(new Vector2(1494.0001f, 851.0002f));
        eighteenNineteenPoints.add(new Vector2(1492.0001f, 908.0002f));
        eighteenNineteenPoints.add(new Vector2(1487.0001f, 939.0002f));
        eighteenNineteenPoints.add(new Vector2(1487.0001f, 975.0002f));
        eighteenNineteenPoints.add(new Vector2(1473.0001f, 1009.00024f));
        eighteenNineteenPoints.add(new Vector2(1456.0001f, 1047.0002f));
        eighteenNineteenPoints.add(new Vector2(1432.0001f, 1062.0002f));
        eighteenNineteenPoints.add(new Vector2(1413.0001f, 1071.0001f));

        RailwayPath eighteenNineteenPath = new RailwayPath("18-19", eighteenNineteenPoints);
        railwayPaths.add(eighteenNineteenPath);


        Array<Vector2> nineteenEightPoints = new Array<>();
        nineteenEightPoints.add(new Vector2(1413.0001f, 1071.0001f));
        nineteenEightPoints.add(new Vector2(1380.0001f, 1072.0002f));
        nineteenEightPoints.add(new Vector2(1354.0001f, 1072.0002f));
        nineteenEightPoints.add(new Vector2(1317.0001f, 1065.0001f));
        nineteenEightPoints.add(new Vector2(1276.0001f, 1053.0002f));
        nineteenEightPoints.add(new Vector2(1240.0001f, 1041.0002f));
        nineteenEightPoints.add(new Vector2(1197.0001f, 1026.0002f));
        nineteenEightPoints.add(new Vector2(1179.0001f, 1027.0002f));

        RailwayPath nineteenEightPath = new RailwayPath("19-8", nineteenEightPoints);
        railwayPaths.add(nineteenEightPath);


        Array<Vector2> nineteenTwentyPoints = new Array<>();
        nineteenTwentyPoints.add(new Vector2(1413.0001f, 1071.0001f));
        nineteenTwentyPoints.add(new Vector2(1450.0001f, 1086.0001f));
        nineteenTwentyPoints.add(new Vector2(1456.0001f, 1115.0002f));
        nineteenTwentyPoints.add(new Vector2(1474.0001f, 1149.0001f));
        nineteenTwentyPoints.add(new Vector2(1497.0001f, 1177.0002f));
        nineteenTwentyPoints.add(new Vector2(1522.0001f, 1197.0001f));
        nineteenTwentyPoints.add(new Vector2(1545.0002f, 1206.0002f));

        RailwayPath nineteenTwentyPath = new RailwayPath("19-20", nineteenTwentyPoints);
        railwayPaths.add(nineteenTwentyPath);

        Array<Vector2> eightTwentyTwoPoints = new Array<>();
        eightTwentyTwoPoints.add(new Vector2(1179.0001f, 1027.0002f));
        eightTwentyTwoPoints.add(new Vector2(1177.0001f, 1050.0001f));
        eightTwentyTwoPoints.add(new Vector2(1181.0001f, 1071.0001f));
        eightTwentyTwoPoints.add(new Vector2(1181.0001f, 1114.0002f));
        eightTwentyTwoPoints.add(new Vector2(1184.0001f, 1152.0002f));
        eightTwentyTwoPoints.add(new Vector2(1203.0001f, 1187.0001f));
        eightTwentyTwoPoints.add(new Vector2(1224.0001f, 1213.0001f));
        eightTwentyTwoPoints.add(new Vector2(1241.0001f, 1257.0002f));
        eightTwentyTwoPoints.add(new Vector2(1240.0001f, 1293.0001f));
        eightTwentyTwoPoints.add(new Vector2(1235.0001f, 1315.0002f));
        eightTwentyTwoPoints.add(new Vector2(1221.0001f, 1338.0002f));


        RailwayPath eightTwentyTwoPath = new RailwayPath("8-22", eightTwentyTwoPoints);
        railwayPaths.add(eightTwentyTwoPath);


        Array<Vector2> twentyTwentyThreePoints = new Array<>();
        twentyTwentyThreePoints.add(new Vector2(1545.0002f, 1206.0002f));
        twentyTwentyThreePoints.add(new Vector2(1549.0002f, 1225.0002f));
        twentyTwentyThreePoints.add(new Vector2(1557.0002f, 1250.0001f));
        twentyTwentyThreePoints.add(new Vector2(1579.0002f, 1279.0002f));
        twentyTwentyThreePoints.add(new Vector2(1618.0002f, 1301.0002f));
        twentyTwentyThreePoints.add(new Vector2(1649.0002f, 1315.0002f));
        twentyTwentyThreePoints.add(new Vector2(1703.0002f, 1329.0001f));
        twentyTwentyThreePoints.add(new Vector2(1737.0002f, 1337.0002f));
        twentyTwentyThreePoints.add(new Vector2(1766.0002f, 1337.0002f));
        twentyTwentyThreePoints.add(new Vector2(1791.0002f, 1337.0002f));

        RailwayPath twentyTwentyThreePath = new RailwayPath("20-23", twentyTwentyThreePoints);
        railwayPaths.add(twentyTwentyThreePath);


    }

    private void initializeJunctions() {
        // Ustvarimo ljubljansko križišče
        Junction junctionOne = new Junction(new Vector2(798.3568f, 991.8973f));

        // Iz Jesenic v obe smeri
        junctionOne.addConnection(new PathConnection("0-1", "1-2", false, false));
        // fromReversed = false -> vlak pride iz Jesenic v normalni smeri
        // toReversed = false -> vlak bo šel proti Kopru v normalni smeri
        junctionOne.addConnection(new PathConnection("0-1", "1-3", false, false));
        // fromReversed = false -> vlak pride iz Jesenic v normalni smeri
        // toReversed = false -> vlak bo šel proti Novemu mestu v normalni smeri

        // Iz Kopra v obe smeri
        junctionOne.addConnection(new PathConnection("1-2", "1-3", true, false));
        junctionOne.addConnection(new PathConnection("1-2", "0-1", true, true));

        // Iz Novega mesta v obe smeri
        junctionOne.addConnection(new PathConnection("1-3", "0-1", true, true));
        junctionOne.addConnection(new PathConnection("1-3", "1-2", true, false));


        junctions.add(junctionOne);


        Junction junctionTwo = new Junction(new Vector2(710.00006f, 673.0001f));

        junctionTwo.addConnection(new PathConnection("1-2", "2-4", false, false));
        junctionTwo.addConnection(new PathConnection("1-2", "2-5", false, false));

        junctionTwo.addConnection(new PathConnection("2-4", "2-5", true, false));
        junctionTwo.addConnection(new PathConnection("2-4", "1-2", true, true));

        junctionTwo.addConnection(new PathConnection("2-5", "1-2", true, true));
        junctionTwo.addConnection(new PathConnection("2-5", "2-4", true, false));


        junctions.add(junctionTwo);

        Junction junctionFour = new Junction((new Vector2(393.00006f, 857.00024f)));

        junctionFour.addConnection(new PathConnection("2-4", "4-6", false, false));
        junctionFour.addConnection(new PathConnection("2-4", "4-7", false, false));

        junctionFour.addConnection(new PathConnection("4-6", "4-7", true, false));
        junctionFour.addConnection(new PathConnection("4-6", "2-4", true, true));
        //Desna naslednja pot zgornja
        junctionFour.addConnection(new PathConnection("4-7", "2-4", true, true));
        junctionFour.addConnection(new PathConnection("4-7", "4-6", true, false));


        junctions.add(junctionFour);


        Junction junctionThree = new Junction(new Vector2(964.55237f, 884.88934f));

        junctionThree.addConnection(new PathConnection("1-3", "3-24", false, false));
        junctionThree.addConnection(new PathConnection("1-3", "3-8", false, false));

        junctionThree.addConnection(new PathConnection("3-8", "1-3", true, true));
        junctionThree.addConnection(new PathConnection("3-8", "3-24", true, false));

        junctionThree.addConnection(new PathConnection("3-24", "3-8", true, false));
        junctionThree.addConnection(new PathConnection("3-24", "1-3", true, true));


        junctions.add(junctionThree);


        Junction junctionFive = new Junction(new Vector2(972.0001f, 652.0001f));

        junctionFive.addConnection(new PathConnection("2-5", "5-9", false, false));
        junctionFive.addConnection(new PathConnection("2-5", "5-24", false, false));

        junctionFive.addConnection(new PathConnection("5-24", "2-5", true, true));
        junctionFive.addConnection(new PathConnection("5-24", "5-9", true, false));
//        junctionFive.addConnection(new PathConnection("3-5", "2-5", false, true));
//        junctionFive.addConnection(new PathConnection("3-5", "5-9", false, false));

        junctionFive.addConnection(new PathConnection("5-9", "5-24", true, false));
        junctionFive.addConnection(new PathConnection("5-9", "2-5", true, true));
        junctions.add(junctionFive);


        Junction junctionNine = new Junction(new Vector2(989.0001f, 464.0001f));


        junctionNine.addConnection(new PathConnection("5-9", "9-10", false, false));
        junctionNine.addConnection(new PathConnection("5-9", "9-11", false, false));

        junctionNine.addConnection(new PathConnection("9-10", "9-11", true, false));
        junctionNine.addConnection(new PathConnection("9-10", "5-9", true, true));

        junctionNine.addConnection(new PathConnection("9-11", "5-9", true, true));
        junctionNine.addConnection(new PathConnection("9-11", "9-10", true, false));
        junctions.add(junctionNine);


        Junction junctionTen = new Junction(new Vector2(714.00006f, 407.0001f));

        junctionTen.addConnection(new PathConnection("9-10", "10-12", false, false));
        junctionTen.addConnection(new PathConnection("9-10", "10-13", false, false));

        junctionTen.addConnection(new PathConnection("10-12", "10-13", true, false));
        junctionTen.addConnection(new PathConnection("10-12", "9-10", true, true));

        junctionTen.addConnection(new PathConnection("10-13", "9-10", true, true));
        junctionTen.addConnection(new PathConnection("10-13", "10-12", true, false));
        junctions.add(junctionTen);


        Junction junctionEleven = new Junction(new Vector2(1248.0001f, 403.00006f));

        junctionEleven.addConnection(new PathConnection("9-11", "11-15", false, false));
        junctionEleven.addConnection(new PathConnection("9-11", "11-14", false, false));

        junctionEleven.addConnection(new PathConnection("11-14", "9-11", true, true));
        junctionEleven.addConnection(new PathConnection("11-14", "11-15", true, false));

        junctionEleven.addConnection(new PathConnection("11-15", "11-14", true, false));
        junctionEleven.addConnection(new PathConnection("11-15", "9-11", true, true));
        junctions.add(junctionEleven);


        Junction junctionFifteen = new Junction(new Vector2(1695.0002f, 539.0001f));

        junctionFifteen.addConnection(new PathConnection("11-15", "15-16", false, false));
        junctionFifteen.addConnection(new PathConnection("11-15", "15-14", false, false));

        junctionFifteen.addConnection(new PathConnection("15-16", "15-14", true, false));
        junctionFifteen.addConnection(new PathConnection("15-16", "11-15", true, true));

        junctionFifteen.addConnection(new PathConnection("15-14", "11-15", true, true));
        junctionFifteen.addConnection(new PathConnection("15-14", "15-16", true, false));
        junctions.add(junctionFifteen);


        Junction junctionFourteen = new Junction(new Vector2(1478.0001f, 625.0001f));

        junctionFourteen.addConnection(new PathConnection("11-14", "15-14", false, true));
        junctionFourteen.addConnection(new PathConnection("11-14", "14-18", false, false));

        junctionFourteen.addConnection(new PathConnection("14-18", "11-14", true, true));
        junctionFourteen.addConnection(new PathConnection("14-18", "15-14", true, true));

        junctionFourteen.addConnection(new PathConnection("15-14", "14-18", false, false));
        junctionFourteen.addConnection(new PathConnection("15-14", "11-14", false, true));
        junctions.add(junctionFourteen);


        Junction junctionEighteen = new Junction(new Vector2(1494.0001f, 851.0002f));

        junctionEighteen.addConnection(new PathConnection("14-18", "18-20", false, false));
        junctionEighteen.addConnection(new PathConnection("14-18", "18-19", false, false));

        junctionEighteen.addConnection(new PathConnection("18-19", "14-18", true, true));
        junctionEighteen.addConnection(new PathConnection("18-19", "18-20", true, false));

        junctionEighteen.addConnection(new PathConnection("18-20", "18-19", true, false));
        junctionEighteen.addConnection(new PathConnection("18-20", "14-18", true, true));
        junctions.add(junctionEighteen);

        Junction junctionNineteen = new Junction(new Vector2(1413.0001f, 1071.0001f));

        junctionNineteen.addConnection(new PathConnection("18-19", "19-20", false, false));
        junctionNineteen.addConnection(new PathConnection("18-19", "19-8", false, false));

        junctionNineteen.addConnection(new PathConnection("19-20", "19-8", true, false));
        junctionNineteen.addConnection(new PathConnection("19-20", "18-19", true, true));

        junctionNineteen.addConnection(new PathConnection("19-8", "18-19", true, true));
        junctionNineteen.addConnection(new PathConnection("19-8", "19-20", true, false));
        junctions.add(junctionNineteen);


        Junction junctionEight = new Junction(new Vector2(1179.0001f, 1027.0002f));

        junctionEight.addConnection(new PathConnection("3-8", "19-8", false, true));
        junctionEight.addConnection(new PathConnection("3-8", "8-22", false, false));

        junctionEight.addConnection(new PathConnection("8-22", "3-8", true, true));
        junctionEight.addConnection(new PathConnection("8-22", "19-8", true, true));

        junctionEight.addConnection(new PathConnection("19-8", "8-22", false, false));
        junctionEight.addConnection(new PathConnection("19-8", "3-8", false, true));
        junctions.add(junctionEight);


        Junction junctionTwenty = new Junction(new Vector2(1545.0002f, 1206.0002f));

        junctionTwenty.addConnection(new PathConnection("18-20", "20-23", false, false));
        junctionTwenty.addConnection(new PathConnection("18-20", "19-20", false, true));

        junctionTwenty.addConnection(new PathConnection("19-20", "18-20", false, true));
        junctionTwenty.addConnection(new PathConnection("19-20", "20-23", false, false));

        junctionTwenty.addConnection(new PathConnection("20-23", "19-20", true, true));
        junctionTwenty.addConnection(new PathConnection("20-23", "18-20", true, true));
        junctions.add(junctionTwenty);




        Junction junctionTwentyFour = new Junction(new Vector2(996.0001f, 770.0002f));


        junctionTwentyFour.addConnection(new PathConnection("3-24", "5-24", false, true));
        junctionTwentyFour.addConnection(new PathConnection("3-24", "24-25", false, false));

        junctionTwentyFour.addConnection(new PathConnection("5-24", "24-25", false, false));
        junctionTwentyFour.addConnection(new PathConnection("5-24", "3-24", false, true));

        junctionTwentyFour.addConnection(new PathConnection("24-25", "3-24", true, true));
        junctionTwentyFour.addConnection(new PathConnection("24-25", "5-24", true, true));


        junctions.add(junctionTwentyFour);




    }

    public RailwayPath getPathById(String pathId) {
        if (pathId == null || railwayPaths == null) return null;

        for (RailwayPath path : railwayPaths) {
            if (path.getId().equals(pathId)) {
                return path;
            }
        }
        return null;
    }

    public Junction getJunctionNearPoint(Vector2 point, float threshold) {
        for (Junction junction : junctions) {
            if (junction.getPosition().dst(point) < threshold) {
                return junction;
            }
        }
        return null;
    }


    public void drawPath(OrthographicCamera camera) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setColor(0.68f, 0.85f, 0.90f, 1f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float lineThickness = 5f;


        for (RailwayPath path : railwayPaths) {
            Array<Vector2> points = path.getWaypoints();
            for (int i = 0; i < points.size - 1; i++) {
                Vector2 start = points.get(i);
                Vector2 end = points.get(i + 1);
                shapeRenderer.rectLine(start.x, start.y, end.x, end.y, lineThickness);
            }
        }

        shapeRenderer.end();
    }


    public Vector2 getStartWaypoint(com.badlogic.gdx.utils.Array<Vector2> path) {
        if (path == null || path.size == 0) {
            throw new IllegalArgumentException("Path is null or empty");
        }
        return path.get(0);
    }


}
