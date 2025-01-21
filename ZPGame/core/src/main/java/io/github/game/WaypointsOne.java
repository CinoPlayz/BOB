package io.github.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class WaypointsOne extends Waypoints {

    public WaypointsOne(ShapeRenderer shapeRenderer) {
        super(shapeRenderer);
        initializePaths();
        initializeJunctions();
    }


    @Override
    public void initializePaths() {
        railwayPaths.clear();
        Array<Vector2> zeroOnePoints = new Array<>();
        zeroOnePoints.add(new Vector2(504.2126f, 1403.8019f));
        zeroOnePoints.add(new Vector2(520.0000f, 1380.0000f));
        zeroOnePoints.add(new Vector2(540.0000f, 1340.0000f));
        zeroOnePoints.add(new Vector2(560.0000f, 1300.0000f));
        zeroOnePoints.add(new Vector2(580.0000f, 1260.0000f));
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
        zeroOnePoints.add(new Vector2(798.3568f, 991.8973f));

        RailwayPath ZeroOne = new RailwayPath("0-1", zeroOnePoints);
        railwayPaths.add(ZeroOne);


        Array<Vector2> OneTwoPoints = new Array<>();

        OneTwoPoints.add(new Vector2(798.3568f, 991.8973f));
        OneTwoPoints.add(new Vector2(810.0000f, 970.0000f));
        OneTwoPoints.add(new Vector2(830.0000f, 940.0000f));
        OneTwoPoints.add(new Vector2(840.0000f, 900.0000f));
        OneTwoPoints.add(new Vector2(820.0000f, 780.0000f));
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
        twoFivePoints.add(new Vector2(770.0001f, 606.0001f));
        twoFivePoints.add(new Vector2(795.0001f, 584.0001f));
        twoFivePoints.add(new Vector2(833.0001f, 581.0001f));
        twoFivePoints.add(new Vector2(877.0001f, 582.0001f));
        twoFivePoints.add(new Vector2(906.0001f, 595.0001f));
        twoFivePoints.add(new Vector2(937.0001f, 621.0001f));
        twoFivePoints.add(new Vector2(945.0001f, 628.0001f));
        twoFivePoints.add(new Vector2(972.0001f, 652.0001f));

        RailwayPath twoFivePath = new RailwayPath("2-5", twoFivePoints);
        railwayPaths.add(twoFivePath);


        Array<Vector2> fourSixPoints = new Array<>();
        fourSixPoints.add(new Vector2(393.00006f, 857.00024f));
        fourSixPoints.add(new Vector2(376.00003f, 886.0002f));
        fourSixPoints.add(new Vector2(353.00003f, 919.0002f));
        fourSixPoints.add(new Vector2(368.00003f, 972.0002f));
        fourSixPoints.add(new Vector2(393.00006f, 999.00024f));
        fourSixPoints.add(new Vector2(432.00012f, 1023.0002f));
        fourSixPoints.add(new Vector2(457.00006f, 1058.0002f));
        fourSixPoints.add(new Vector2(462.00006f, 1096.0001f));
        fourSixPoints.add(new Vector2(454.00006f, 1141.0002f));
        fourSixPoints.add(new Vector2(440.00006f, 1182.0002f));
        fourSixPoints.add(new Vector2(422.00006f, 1233.0002f));

        RailwayPath fourSixPath = new RailwayPath("4-6", fourSixPoints);
        railwayPaths.add(fourSixPath);


        Array<Vector2> fourSevenPoints = new Array<>();
        fourSevenPoints.add(new Vector2(393.00006f, 857.00024f));
        fourSevenPoints.add(new Vector2(307.99997f, 830.0002f));
        fourSevenPoints.add(new Vector2(291.00003f, 785.0001f));
        fourSevenPoints.add(new Vector2(278.00003f, 741.0001f));
        fourSevenPoints.add(new Vector2(271.00003f, 698.0001f));
        fourSevenPoints.add(new Vector2(314.00003f, 626.0001f));
        fourSevenPoints.add(new Vector2(330.0001f, 598.0001f));
        fourSevenPoints.add(new Vector2(366.00003f, 566.0001f));


        RailwayPath fourSevenPath = new RailwayPath("4-7", fourSevenPoints);
        railwayPaths.add(fourSevenPath);

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
       twentyFourTwentyFivePoints.add(new Vector2(1043.0001f, 761.0001f));
        twentyFourTwentyFivePoints.add(new Vector2(1091.0001f, 730.0001f));
        twentyFourTwentyFivePoints.add(new Vector2(1106.0001f, 694.0001f));
        twentyFourTwentyFivePoints.add(new Vector2(1117.0001f, 660.0001f));
        twentyFourTwentyFivePoints.add(new Vector2(1129.0001f, 626.0001f));
        twentyFourTwentyFivePoints.add(new Vector2(1136.0001f, 606.0001f));


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
        tenThirteenPoints.add(new Vector2(676.00006f, 227.00003f));
        tenThirteenPoints.add(new Vector2(710.00006f, 210.00003f));
        tenThirteenPoints.add(new Vector2(754.00006f, 195.00003f));
        tenThirteenPoints.add(new Vector2(797.0001f, 190.00002f));
        tenThirteenPoints.add(new Vector2(842.0001f, 190.00002f));
        tenThirteenPoints.add(new Vector2(886.0001f, 188.00002f));
        tenThirteenPoints.add(new Vector2(917.0001f, 183.00002f));


        RailwayPath tenThirteenPath = new RailwayPath("10-13", tenThirteenPoints);
        railwayPaths.add(tenThirteenPath);


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


    }


    protected void initializeJunctions() {
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


//        Junction junctionEleven = new Junction(new Vector2(1248.0001f, 403.00006f));
//
//        junctionEleven.addConnection(new PathConnection("9-11", "11-15", false, false));
//        junctionEleven.addConnection(new PathConnection("9-11", "11-14", false, false));
//
//        junctionEleven.addConnection(new PathConnection("11-14", "9-11", true, true));
//        junctionEleven.addConnection(new PathConnection("11-14", "11-15", true, false));
//
//        junctionEleven.addConnection(new PathConnection("11-15", "11-14", true, false));
//        junctionEleven.addConnection(new PathConnection("11-15", "9-11", true, true));
//        junctions.add(junctionEleven);


//        Junction junctionFifteen = new Junction(new Vector2(1695.0002f, 539.0001f));
//
//        junctionFifteen.addConnection(new PathConnection("11-15", "15-16", false, false));
//        junctionFifteen.addConnection(new PathConnection("11-15", "15-14", false, false));
//
//        junctionFifteen.addConnection(new PathConnection("15-16", "15-14", true, false));
//        junctionFifteen.addConnection(new PathConnection("15-16", "11-15", true, true));
//
//        junctionFifteen.addConnection(new PathConnection("15-14", "11-15", true, true));
//        junctionFifteen.addConnection(new PathConnection("15-14", "15-16", true, false));
//        junctions.add(junctionFifteen);


//        Junction junctionFourteen = new Junction(new Vector2(1478.0001f, 625.0001f));
//
//        junctionFourteen.addConnection(new PathConnection("11-14", "15-14", false, true));
//        junctionFourteen.addConnection(new PathConnection("11-14", "14-18", false, false));
//
//        junctionFourteen.addConnection(new PathConnection("14-18", "11-14", true, true));
//        junctionFourteen.addConnection(new PathConnection("14-18", "15-14", true, true));
//
//        junctionFourteen.addConnection(new PathConnection("15-14", "14-18", false, false));
//        junctionFourteen.addConnection(new PathConnection("15-14", "11-14", false, true));
//        junctions.add(junctionFourteen);


        Junction junctionEighteen = new Junction(new Vector2(1494.0001f, 851.0002f));

        junctionEighteen.addConnection(new PathConnection("14-18", "18-20", false, false));
        junctionEighteen.addConnection(new PathConnection("14-18", "18-19", false, false));

        junctionEighteen.addConnection(new PathConnection("18-19", "14-18", true, true));
        junctionEighteen.addConnection(new PathConnection("18-19", "18-20", true, false));

        junctionEighteen.addConnection(new PathConnection("18-20", "18-19", true, false));
        junctionEighteen.addConnection(new PathConnection("18-20", "14-18", true, true));
        junctions.add(junctionEighteen);

//        Junction junctionNineteen = new Junction(new Vector2(1413.0001f, 1071.0001f));
//
//        junctionNineteen.addConnection(new PathConnection("18-19", "19-20", false, false));
//        junctionNineteen.addConnection(new PathConnection("18-19", "19-8", false, false));
//
//        junctionNineteen.addConnection(new PathConnection("19-20", "19-8", true, false));
//        junctionNineteen.addConnection(new PathConnection("19-20", "18-19", true, true));
//
//        junctionNineteen.addConnection(new PathConnection("19-8", "18-19", true, true));
//        junctionNineteen.addConnection(new PathConnection("19-8", "19-20", true, false));
//        junctions.add(junctionNineteen);


        Junction junctionEight = new Junction(new Vector2(1179.0001f, 1027.0002f));

        junctionEight.addConnection(new PathConnection("3-8", "19-8", false, true));
        junctionEight.addConnection(new PathConnection("3-8", "8-22", false, false));

        junctionEight.addConnection(new PathConnection("8-22", "3-8", true, true));
        junctionEight.addConnection(new PathConnection("8-22", "19-8", true, true));

        junctionEight.addConnection(new PathConnection("19-8", "8-22", false, false));
        junctionEight.addConnection(new PathConnection("19-8", "3-8", false, true));
        junctions.add(junctionEight);


//        Junction junctionTwenty = new Junction(new Vector2(1545.0002f, 1206.0002f));
//
//        junctionTwenty.addConnection(new PathConnection("18-20", "20-23", false, false));
//        junctionTwenty.addConnection(new PathConnection("18-20", "19-20", false, true));
//
//        junctionTwenty.addConnection(new PathConnection("19-20", "18-20", false, true));
//        junctionTwenty.addConnection(new PathConnection("19-20", "20-23", false, false));
//
//        junctionTwenty.addConnection(new PathConnection("20-23", "19-20", true, true));
//        junctionTwenty.addConnection(new PathConnection("20-23", "18-20", true, true));
//        junctions.add(junctionTwenty);


        Junction junctionTwentyFour = new Junction(new Vector2(996.0001f, 770.0002f));


        junctionTwentyFour.addConnection(new PathConnection("3-24", "5-24", false, true));
        junctionTwentyFour.addConnection(new PathConnection("3-24", "24-25", false, false));

        junctionTwentyFour.addConnection(new PathConnection("5-24", "24-25", false, false));
        junctionTwentyFour.addConnection(new PathConnection("5-24", "3-24", false, true));

        junctionTwentyFour.addConnection(new PathConnection("24-25", "3-24", true, true));
        junctionTwentyFour.addConnection(new PathConnection("24-25", "5-24", true, true));


        junctions.add(junctionTwentyFour);


    }
}
