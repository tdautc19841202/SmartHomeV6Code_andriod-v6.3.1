function initlan() {
	try {
		var iphoneX = {sysFunc: "getItem:", room: "iphoneX", id: "iphoneX", data: ""};
		var iStr ='';
		if(!window.v6sysfunc) {
             iStr = prompt(JSON.stringify(iphoneX))
        }
		if(iStr == "iphoneX") {
			var offArr = document.getElementsByClassName("mask_layer")
			if(offArr != '' && offArr.length != 0){
				offArr[0].style.top = "8.8rem";
                offArr[0].style.height = "calc(100% - 8.8rem)";
			}
			var body = document.getElementsByClassName("header")[0];
			body.style.paddingTop = "2.4rem"
			var top3 = document.getElementsByTagName("body")[0].children[1].style.paddingTop;
			if(top3 == "6.4rem") {
				document.getElementsByTagName("body")[0].children[1].style.paddingTop = "8.8rem";
			} else if(top3 == "9.8rem") {
				document.getElementsByTagName("body")[0].children[1].style.paddingTop = "12.2rem";
			}
			var historyCon = document.getElementsByClassName("historyCon");
            if(historyCon != '' && historyCon.length != 0){
                historyCon[0].style.top = "13.8rem";
                historyCon[0].style.height = "calc(100% - 13.8rem)";
            }
            var date_title = document.getElementsByClassName("date_title");
            if(date_title != '' && date_title.length != 0){
                date_title[0].style.top = "13.8rem";
            }
            var data = document.getElementsByClassName("data");
            if(data != '' && data.length != 0){
                data[0].style.paddingTop = "7.4rem";
            }
		}
	} catch(e) {
		//TODO handle the exception
	}
    var languageCode = window.sysfun.getLang();
    languageUtil.onJsLoaded = function() {
        initLanguage();
    }
    languageUtil.init(languageCode, "lang/");
}

/*
 * 处理标签中的国际化
 */
function initLanguage() {
    globalLang();
    var item = document.getElementsByClassName("autoSwitchLanguager");
    for(var i = 0; i < item.length; i++) {
        var languagerText
        if(!(item[i].getAttribute("key") == null) || item[i].getAttribute("key") == "") {
            languagerText = languageUtil.getResource(item[i].getAttribute("key"));
        } else {
            languagerText = languageUtil.getResource(item[i].id);
        }
        if(item[i].placeholder != "" && item[i].localName == "input") {
            if(languagerText) {
                item[i].placeholder = languagerText;
            }
        } else {
            if(languagerText) {
                if(item[i].type == "button") {
                    item[i].value = languagerText;
                } else {
                    item[i].innerHTML = languagerText;
                }
            }
        }
    }
}

var languageUtil = function() {};

languageUtil.prototype = {
    
    /**
     * 自定义html控件属性，例如 langKey="Key_hello"; 指明语言资源的键 为  Key_hello
     */
langKey: "langKey",
    /**
     * 语言文件(例如 zh.js)是否正在加载
     */
isLoading: false,
    /**
     * 语言文件(例如 zh.js)是否加载完成
     */
isLoaded: false,
    /**
     * 当前语言  "zh"   "en" ....
     */
currentlangCode: "",
    /**
     * 当前的语言文件对象
     */
currentJsObj: null,
    
    /**
     *  初始化 加载 langCode对应的js
     * @param {Object} langCode 例如 "zh" "en"
     * @param {Object} langSrcFolder 语言文件所在位置的相对路径
     */
init: function(langCode, langSrcFolder) {
    try {
        if(this.currentlangCode != langCode) {
            this.removeJs(this.currentJsObj);
            this.currentlangCode = langCode;
            var that = this;
            this.currentJsObj = this.loadJs(langSrcFolder + langCode + ".js");
        }
    } catch(e) {
        console.log(e);
    }
    return this.currentJsObj;
},
    /**
     * 转换语言
     * @param {Object} node 需要转换的节点
     */
doSwitch: function(node) {
    var children = node.children;
    if(children.length > 0) {
        for(var i = 0; i < children.length; i++) {
            this.doSwitch(children[i]);
        }
    } else {
        var key = node.attributes[this.langKey];
        if(typeof key != "undefined") {
            switch(node.type) {
                case "button":
                    node.value = $lang[key.value];
                    break;
                    
                default:
                    node.textContent = $lang[key.value];
                    break;
            }
            
        }
    }
},
    /**
     * 动态装载js
     */
loadJs: function(jsPath) {
    this.isLoaded = false;
    this.isLoading = true;
    var mHead = document.getElementsByTagName("head")[0];
    var jsObj = document.createElement("script");
    jsObj.src = jsPath;
    jsObj.type = "text/javascript";
    mHead.appendChild(jsObj);
    jsObj.onload = function() {
        languageUtil.onJsLoaded();
    };
    jsObj.onerror = function() {
        if(this.currentlangCode != "en") {
            languageUtil.init("en", "lang/");
        }
    }
    return jsObj;
},
    /**
     * 当动态装载的js准备完毕调用
     */
onJsLoaded: function() {
    this.isLoaded = true;
    this.isLoading = false;
    window.all = document.all;
    for(var i = 0; i < all.length; i++) {
        var node = all[i];
        var key = node.attributes[this.langKey];
        if(typeof key != "undefined") {
            switch(node.type) {
                case "button":
                    node.value = $lang[key.value];
                    break;
                    
                default:
                    node.textContent = $lang[key.value];
                    break;
            }
            
        }
    }
    
    /*
     var mBody = document.getElementsByTagName("body")[0];
     if (mBody == null) return;
     this.doSwitch(mBody);
     */
},
    /**
     * 移除
     */
removeJs: function(jsObj) {
    if(jsObj != null) {
        var mHead = document.getElementsByTagName("head")[0];
        mHead.removeChild(jsObj);
    }
    this.isLoaded = false;
    this.isLoading = false;
},
    /**
     * 根据 key 获取语言资源文件
     * @param {Object} key
     */
getResource: function(key) {
    try {
        return $lang[key];
    } catch(e) {
        return "";
    }
}
    
};

window.languageUtil = new languageUtil();
/*
 * 处理js中的国际化
 */
function globalLang() {
    window.Sphygmomanometer_name = languageUtil.getResource("Sphygmomanometer_name_04")//智能血压计";
    window.Sphygmomanometer_text_1 = languageUtil.getResource("Sphygmomanometer_text_1")//高压";
    window.Sphygmomanometer_text_2 = languageUtil.getResource("Sphygmomanometer_text_2")//低压";
    window.Sphygmomanometer_text_3 = languageUtil.getResource("Sphygmomanometer_text_3")//平均";
    window.Sphygmomanometer_text_4 = languageUtil.getResource("Sphygmomanometer_text_4")//脉率";
    window.Sphygmomanometer_text_5 = languageUtil.getResource("Sphygmomanometer_text_5")//血氧";
    window.Sphygmomanometer_Name_1 = languageUtil.getResource("Sphygmomanometer_Name_1")//用户1";
    window.Sphygmomanometer_Name_2 = languageUtil.getResource("Sphygmomanometer_Name_2")//用户2";
    window.Sphygmomanometer_Name_3 = languageUtil.getResource("Sphygmomanometer_Name_3")//用户3";
    window.Sphygmomanometer_normal = languageUtil.getResource("Sphygmomanometer_Level_1")//正常;
    window.Sphygmomanometer_high = languageUtil.getResource("Sphygmomanometer_Level_2")//偏高;
    window.Sphygmomanometer_low = languageUtil.getResource("Sphygmomanometer_Level_3")//偏低;
    window.Sphygmomanometer_high1 = languageUtil.getResource("Sphygmomanometer_Level_4")//高";
    window.Sphygmomanometer_low1 = languageUtil.getResource("Sphygmomanometer_Level_5")//低;
    window.lang_sms = languageUtil.getResource("Sphygmomanometer_48_log")//日志;
    window.lang_timeout = languageUtil.getResource("Sphygmomanometer_text_31")//加载超时

}


