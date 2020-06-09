
----------------------------------------------------------------------------------------
http://localhost:port/traceability/addUser  post向userinfo表插入数据

----------------------------------------------------------------------------------------
http://localhost:port/traceability/getPatientData   get查询userinfo表是否存在某个用户的Flag==false，如果存在返回该用户的mac地址，否则返回null
http://localhost:port/traceability/getLocationInfo/{userMACAddress}/{MACAddress}     get查询Locationinfo表中mac地址为{MACAddress}的所有信息,{userMACAddress}是请求人的mac地址
http://localhost:port/traceability/getReportInfo/{userMACAddress}/{MACAddress}   get查询Reportinfo表中mac地址为{MACAdress}的所有信息,{userMACAddress}是请求人的mac地址
http://localhost:port/traceability/getTransportationinfo/{userMACAddress}/{MACAddress}   get查询Transportainfo表中mac地址为{MACAddress}的所有信息,{userMACAddress}是请求人的mac地址
三个信息都查询后，向pushinfo表插入一条数据，包含{userMACAddress}/{MACAddress}以及插入数据库的时间。
----------------------------------------------------------------------------------------
http://localhost:port/traceability/addLocationInfo/  post向Locationinfo表插入数据
http://localhost:port/traceability/addReportInfo/   post向Reportinfo表插入数据
http://localhost:port/traceability/addTransportationinfo/   post向Transportainfo表插入数据
以上三个信息上传完成后更改userinfo表中该用户（用户的唯一标识是MacAddress）的Flag=false
----------------------------------------------------------------------------------------
定时服务事件（当相所有用户推送完成后，设置推送Flag=true）
userinfo表中的MACAddress和pushinfo表中patientMAC相连接，
select count(*) from userinfo
{count1}//中间变量
select  MacAddress from userinfo where Flag==false
{MacAddress}//中间变量
select count(*) from pushinfo where patientMAC =={MacAddress}
{count2}//中间变量
if {count1}=={count2}
update userinfo set Flag=true where MacAddress={MacAddress}
-------------------------------------------------------------------------------------------
json数据示例
// transportationInfo// userInfo//reportInfo
{
    "MACAddress": "D8:CE:3A:86:DA:27",
    "Type": "train",
    "NO": "G123",
    "Seat":"056",
    "Date":"2020/06/07 14:22:05"
},
// LocationInfo
{
    "Location": [
        {
            "MACAddress": "D8:CE:3A:86:DA:27",
            "Longitude": "120.098212",
            "Latitude": "32.227005",
            "Date": "2020/06/07 14:22:05"
        },
        {
            "MACAddress": "D8:CE:3A:86:DA:27",
            "Longitude": "120.098212",
            "Latitude": "32.227005",
            "Date": "2020/06/07 14:22:05"
        },
        {
            "MACAddress": "D8:CE:3A:86:DA:27",
            "Longitude": "120.098212",
            "Latitude": "32.227005",
            "Date": "2020/06/07 14:22:05"
        },
        {
            "MACAddress": "D8:CE:3A:86:DA:27",
            "Longitude": "120.098212",
            "Latitude": "32.227005",
            "Date": "2020/06/07 14:22:05"
        }
    ]
}

 public static void addUser(LocalDevice device) {
        String url = "http://" + IP + ":8080/TraceabilityServer/user/add";
        JSONObject requestContent = new JSONObject();

        try {
            requestContent.put("macAddress", device.getMac());
            requestContent.put("deviceId", device.getDeviceId());
            requestContent.put("flag", "true");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestContent.toString());

        sendByOKHttp(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    public static void addLocationInfoList(List<LocationEntity> locationEntityList) {
        String url = "http://" + IP + ":8080/TraceabilityServer/addLocationInfo";

//        getDataFromServer("http://192.168.1.6:8080/TraceabilityServer/test", new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                //获取数据
//                Log.e("test",response.body().string());
//                Log.e("test", String.valueOf(response));
//                Log.e("test", "成功");
//
//            }
//        });
        JSONArray jsonArray = new JSONArray();
        JSONObject requestContent = new JSONObject();
        String macAddress = OneNetDeviceUtils.macAddress;

        try {
            for (LocationEntity location : locationEntityList) {
                JSONObject obj = new JSONObject();
                obj.put("macAddress", macAddress);
                obj.put("latitude", location.getLatitude());
                obj.put("longitude", location.getLongitude());
                obj.put("date", location.getDate());
                jsonArray.put(obj);
            }

            requestContent.put("data", jsonArray);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestContent.toString());
            Log.e("request", requestContent.toString());
            sendByOKHttp(url, requestBody, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("addLocationInfoList", String.valueOf(e));
                    Log.e("addLocationInfoList", "失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.e("addLocationInfoList", String.valueOf(response));
                    Log.e("addLocationInfoList", "成功");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public void addReportInfoList(List<ReportInfoEntity> reportInfoEntityList) {
        String url = "http://" + IP + ":8080/TraceabilityServer/addReportInfo";

        JSONArray jsonArray = new JSONArray();
        JSONObject requestContent = new JSONObject();
        String macAddress = OneNetDeviceUtils.macAddress;

        try {
            for (ReportInfoEntity report : reportInfoEntityList) {
                JSONObject obj = new JSONObject();
                obj.put("macAddress", macAddress);
                obj.put("description", report.getText());
                obj.put("date", report.getDate());
                jsonArray.put(obj);
            }

            requestContent.put("data", jsonArray);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestContent.toString());
            sendByOKHttp(url, requestBody, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("addLocationInfoList", String.valueOf(e));
                    Log.e("addLocationInfoList", "失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.e("addLocationInfoList", String.valueOf(response));
                    Log.e("addLocationInfoList", "成功");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void addTransportationinfo(List<TransportationEntity> transportationEntityList) {
        String url = "http://" + IP + ":8080/TraceabilityServer/addTransportationinfo";

        JSONArray jsonArray = new JSONArray();
        JSONObject requestContent = new JSONObject();
        String macAddress = OneNetDeviceUtils.macAddress;

        try {
            for (TransportationEntity transportation : transportationEntityList) {
                JSONObject obj = new JSONObject();
                obj.put("macAddress", macAddress);
                obj.put("No", transportation.getNO());
                obj.put("seat", transportation.getSeat());
                obj.put("type", transportation.getType());
                obj.put("date", transportation.getDate());
                jsonArray.put(obj);
            }

            requestContent.put("data", jsonArray);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestContent.toString());
            sendByOKHttp(url, requestBody, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("addLocationInfoList", String.valueOf(e));
                    Log.e("addLocationInfoList", "失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.e("addLocationInfoList", String.valueOf(response));
                    Log.e("addLocationInfoList", "成功");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getPatientMacAddress(Callback callback) {
        String url = "http://" + IP + ":8080/TraceabilityServer/getPatientData";
        getDataFromServer(url, callback);
    }

    public void queryPatientLocationInfo(String patientMac, Callback callback) {
        String macAddress = OneNetDeviceUtils.macAddress;
        String url = "http://" + IP + ":8080/TraceabilityServer/getPatientData" + macAddress + "/" + patientMac;
        getDataFromServer(url, callback);
    }

    public void queryPatientReportInfo(String patientMac, Callback callback) {
        String macAddress = OneNetDeviceUtils.macAddress;
        String url = "http://" + IP + ":8080/TraceabilityServer/getReportInfo" + macAddress + "/" + patientMac;
        getDataFromServer(url, callback);
    }

    public void queryPatientTransportationinfo(String patientMac, Callback callback) {
        String macAddress = OneNetDeviceUtils.macAddress;
        String url = "http://" + IP + ":8080/TraceabilityServer/getTransportationinfo" + macAddress + "/" + patientMac;
        getDataFromServer(url, callback);
    }