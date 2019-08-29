(function() {
	var _BARCODE = 'gatewayCmd',
		B = window.plus.bridge;
	var gatewayCmd = {
		getCurrentGatewayID: function(onsucceed) {
			var callback = function(bridge) {
				bridge.callHandler("getCurrentGatewayID", null, onsucceed);
			}
			return B.exec(callback, _BARCODE, "getCurrentGatewayID");
		},
		getCurrentAppID: function(onsucceed) {
			var callback = function(bridge) {
				bridge.callHandler("getCurrentAppID", onsucceed);
			}
			return B.exec(callback, _BARCODE, "getCurrentAppID");
		},
        getDeviceInfo: function(param, onsucceed) {
            var callback = function(bridge) {
                if(typeof onsucceed == "function") {
                    bridge.callHandler("getDeviceInfo", param, onsucceed);
                } else {
                    console.log("fail");
                    return;
                }
            }
            return B.exec(callback, _BARCODE, "getDeviceInfo");
        },
		controlDevice: function(params,onsucceed) {
			var callback = function(bridge) {
				bridge.callHandler("controlDevice", params, onsucceed)
			}
			return B.exec(callback, _BARCODE, "controlDevice");
		},
		newDataRefresh: function(onsucceed) {
			var callback = function(bridge) {
				if(typeof onsucceed == "function") {
					bridge.registerHandler("newDataRefresh",onsucceed)
				} else {
					console.log("fail");
					return;
				}
			}
			return B.exec(callback, _BARCODE, "newDataRefresh");
		},
        getStatistic: function (params, onsucceed) {
            var callback = function(bridge) {
                bridge.callHandler("getStatistic", params, onsucceed)
            }
            return B.exec(callback, _BARCODE, "getStatistic");
        }
	};
	window.plus.gatewayCmd = gatewayCmd;
})();

