
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