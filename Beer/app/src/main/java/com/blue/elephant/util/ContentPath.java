package com.blue.elephant.util;


import java.lang.ref.PhantomReference;

/***
 * 保存当前的请求路径
 */
public class ContentPath {
      public static final String prefix = "http://192.168.123.200:8081/beer";
//    public static final String prefix = "http://120.195.219.108:8081/beer";
//    public static final String prefix ="http://app.elephant.rentals";
    public static final String card_public_key = "pk_test_6uR6mJeVmESBwtCMtcevFLeX";

//    private static final String prefix = "http://192.168.123.200:8081/beer/appcustomerapi";  //test

    public static final String login =prefix +  "/android/appcustomer/user/login.json";

    public static final String quickLogin = prefix + "/android/appcustomer/user/quickLogin.json";

    public static final String loginOut = prefix + "/android/appcustomer/user/logout.json";

    public static final String queryBike = prefix + "/android/appcustomer/bike/queryBike.json";

    public static final String bikeInfo = prefix + "/android/appcustomer/bike/getBikeById.json";

    public static final String bikeSerial = prefix + "/android/appcustomer/bike/getBikeByCode.json";

    public static final String income = prefix + "/android/appcustomer/order/statisticsOrder.json";

    public static final String incomPeriod = prefix + "/android/appcustomer/order/summaryOrder.json";

    public static final String uploadPic = prefix + "/android/appcustomer/order/startOrder.json";
//    public static final String uploadPic = prefix + "/ios/appcustomer/order/startOrder.json";
    public static final String stopOrder = prefix + "/android/appcustomer/order/stopOrder.json";

//    public static final String bikeRent = prefix + "/android/appcustomer/bike/getBikeByCode.json";

    public static final String orderList = prefix + "/android/appcustomer/order/queryOrder.json";

    public static final String orderDetail = prefix + "/android/appcustomer/order/getOrder.json";

    public static final String addBike = prefix + "/android/appcustomer/bike/addBike.json";

    public static final String queryMaintenance = prefix + "/android/appcustomer/maintenance/queryMaintenance.json";

    public static final String getMaintenance = prefix + "/android/appcustomer/maintenance/getMaintenance.json";

    public static final String startMaintenance = prefix + "/android/appcustomer/maintenance/startMaintenance.json";

    public static final String stopMaintenance = prefix + "/android/appcustomer/maintenance/stopMaintenance.json";


    public static final String disableBike = prefix + "/android/appcustomer/bike/disableBike.json";

    public static final String activeBike = prefix + "/android/appcustomer/bike/enableBike.json";

    public static final String editBike = prefix + "/android/appcustomer/bike/editBike.json";

    public static final String unlockBike = prefix + "/android/appcustomer/bike/unlockBike.json";

    public static final String lockBike = prefix + "/android/appcustomer/bike/lockBike.json";


    public static final String getCardList = prefix + "/android/appcustomer/customer/stripe/getCustomer.json";

    public static final String unBindCard = prefix + "/android/appcustomer/customer/stripe/unbindCard.json";

    public static final String bindCard = prefix + "/android/appcustomer/customer/stripe/bindCard.json";

    public static final String setDefaultCard = prefix + "/android/appcustomer/customer/stripe/setDefaultCard.json";


    public static final String personInfo= prefix + "/android/appcustomer/user/getUserInfo.json";

    public static final String payList = prefix + "/android/appcustomer/payment/queryPayment.json";

    public static final String historyTrack = prefix + "/android/appcustomer/bike/locationHistory.json";

    public static final String updateStatue = prefix + "/android/appcustomer/bike/updateBikeInfo.json";

}
