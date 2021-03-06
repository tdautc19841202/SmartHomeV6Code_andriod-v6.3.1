/**
 * Created by Veev on 2017/6/9.
 */

var leaveHomeButton = "离家按钮";

function getMoreConfig(deviceID, gwID) {
    var config = {
        "deviceID": deviceID,
        "data": [
            {
                "group": "admin",
                "offLineDisable": true,
                "item": [
                    {
                        "type": "custom",
                        "name": moreSettingUserManger,
                        "action": "custom:DevBc_userManager",
                        "param": [
                            {
                                "key": "gwID",
                                "type": "string",
                                "value": gwID
                            },
                            {
                                "key": "devId",
                                "type": "string",
                                "value": deviceID
                            }
                        ]
                    },
                    {
                        "offLineDisable": true,
                        "type": "jump",
                        "name": moreSettingPush,
                        "action": "jump:DevBc_AlarmSetting",
                        "enableWithEnterType": "account",
                        "param": [
                            {
                                "key": "gwID",
                                "type": "string",
                                "value": gwID
                            },
                            {
                                "key": "devId",
                                "type": "string",
                                "value": deviceID
                            },
                            {
                                "key": "deviceID",
                                "type": "string",
                                "value": deviceID
                            }
                        ]
                    },
                    {
                        "type": "jump",
                        "name": "短信通知", /*短信通知*/
                        "action": "jump:Lock_Message",
                        "showWithEnterType": "account",
                        "showInLocale": "zh",
                        "param": [
                            {
                                "key": "url",
                                "type": "string",
                                "value": "SMSNotification/smsDist/sms.html"
                            },
                            {
                                "key": "gwID",
                                "type": "string",
                                "value": gwID
                            },
                            {
                                "key": "devId",
                                "type": "string",
                                "value": deviceID
                            }
                        ]
                    }
                ]
            },
            {
                "group": "bf",
                "param": [
                    {
                        "key": "gwID",
                        "type": "string",
                        "value": gwID
                    },
                    {
                        "key": "devId",
                        "type": "string",
                        "value": deviceID
                    },
                    {
                        "key": "deviceId",
                        "type": "string",
                        "value": deviceID
                    }
                ],
                "offLineDisable": true,
                "item": [
                    {
                        "type": "custom",
                        "name": moreSettingDoorBoard,
                        "action": "custom:Bf_Door_Panel"
                    },
                    {
                        "type": "custom",
                        "name": moreSettingLeaveHomeBtn,
                        "action": "custom:LeaveHomeBtn"
                    }
                ]
            }
        ]
    };

    return config;
}