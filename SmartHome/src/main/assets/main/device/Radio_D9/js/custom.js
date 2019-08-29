function inputDialog(titleTxt, btnTxtArray, onSubmit, onCancle) {
	var self = this;
	var pageCover = null;
	var that = null; //表示dialog最外层控件
	var imgClose = null; //关闭按钮
	var title = null; //对话框标题
	var content = null; //对话框内容部分
	var dlgInput = null;
	var okBtn = null;
	var canclebtn = null;

	//设置节点属性
	inputDialog.prototype.setItem = function(item, className, id, display, src, content) {
		if (className) item.className = className;
		if (id) item.id = id;
		if (display) item.style.display = display;
		if (src) item.src = src;
		if (content) item.innerHTML = content;
	};
	//创建节点
	inputDialog.prototype.createItem = function() {
		var body = document.getElementsByTagName("body")[0];
		//遮罩
		pageCover = document.createElement("div");
		pageCover.className = "dialog-cover";
		pageCover.onclick = this.close;
		//最外层
		that = document.createElement("div");
		this.setItem(that, 'dialog', 'dlgTest', 'none', null, null);

		//标题
		title = document.createElement("div");
		this.setItem(title, 'title', null, null, null, "请输入内容");
		//dialog 内容区域
		content = document.createElement("div");
		this.setItem(content, 'content', null, null, null, null);

		var inputDiv = document.createElement("div");
		inputDiv.style.height = "50%";
		inputDiv.style.textAlign = "center";
		inputDiv.style.verticalAlign = "middle";
		//输入框	                
		dlgInput = document.createElement("input");
		dlgInput.className = "txtInput";
		dlgInput.type = "text";
		inputDiv.appendChild(dlgInput);

		var btnDiv = document.createElement("div");
		btnDiv.style.height = "50%";
		btnDiv.style.textAlign = "center";
		btnDiv.style.verticalAlign = "bottom";
		//确认按钮	       
		okBtn = document.createElement("input");
		okBtn.className = "rightBtn";
		okBtn.type = "button";
		okBtn.value = "确认"
			//取消按钮
		canclebtn = document.createElement("input");
		canclebtn.className = "leftBtn";
		canclebtn.type = "button";
		canclebtn.value = "取消";

		btnDiv.appendChild(okBtn);
		btnDiv.appendChild(canclebtn);

		content.appendChild(inputDiv);
		content.appendChild(btnDiv);

		that.appendChild(title);
		that.appendChild(content);
		body.appendChild(pageCover);
		body.appendChild(that);
		//设置按钮事件
		setEvent();
	};
	//获取页面顶部蜷去的高度
	inputDialog.prototype.getScrollTop = function() {
		return document.documentElement.scrollTop || document.body.scrollTop || window.pageYOffset || window.scrollY || 0;
	};
	//获得当前控件距可视区域顶部的距离
	inputDialog.prototype.getToClientTop = function(item) {
		return parseInt(item.offsetTop - this.getScrollTop());
	};
	//获取初始化时x坐标
	inputDialog.prototype.getInitLeft = function() {
		return parseInt((document.body.clientWidth - that.offsetWidth) / 2);
	};
	//获取初始化时y坐标
	inputDialog.prototype.getInitTop = function() {
		return parseInt(this.getScrollTop() + (document.body.clientHeight - that.offsetHeight) / 2);
	};
	//设置坐标位置
	inputDialog.prototype.setLocation = function(left, top) {
		if (that) {
			that.style.left = left + 'px';
			that.style.top = top + 'px';
		}
	};
	//显示dialog
	inputDialog.prototype.show = function() {
		this.createItem();
		if (that) {
			if (typeof(titleTxt) == "string") {
				title.innerHTML = titleTxt;
			}
			if (btnTxtArray.length >= 2) {
				okBtn.value = btnTxtArray[0];
				canclebtn.value = btnTxtArray[1];
			}
			that.style.display = 'block';
			this.setLocation(this.getInitLeft(), this.getInitTop());
			dlgInput.focus();
		}
	};

	//设置按钮事件
	function setEvent() {
		if (typeof(onCancle) == "function") {
			canclebtn.onclick = onCancle;
		} else {
			canclebtn.onclick = self.close;
		}
		if (typeof(onSubmit) == "function") {
			okBtn.onclick = function() {
				var result = dlgInput.value;
				self.close();
				onSubmit(result);
			};
		} else {
			okBtn.onclick = self.close;
		}
	}
	//关闭
	inputDialog.prototype.close = function() {
		var body = document.getElementsByTagName("body")[0];
		body.removeChild(that);
		body.removeChild(pageCover);
		pageCover = null;
		offset = null;
		that = null;
		imgClose = null;
		title = null;
		content = null;
		dlgInput = null;
		okBtn = null;
		canclebtn = null;
	};
}

/**
 * 自定义触摸移动事件处理工具类
 */
var pullUtil = function() {};

pullUtil.prototype = {
	up: "up",
	down: "down",
	left: "left",
	right: "right",
	addPullEvent: function(contentId, way, onPullCallBack, onReleaseCallBack) {
		var listener = new pullEventListener();
		listener.setEvent(contentId, way, onPullCallBack, onReleaseCallBack);
	},
}

var pullEventListener = function() {
	var start = 0;
	var dist = 0;
	var listernWay = null;
	var onPull = null;
	var onRelease = null;
	var content = null;

	pullEventListener.prototype.setEvent = function(contentId, way, onPullCallBack, onReleaseCallBack) {
		start = 0;
		dist = 0;
		listernWay = way;
		onPull = onPullCallBack;
		onRelease = onReleaseCallBack;
		content = document.getElementById(contentId);
		content.addEventListener("touchstart", this.touchStart, false);
		content.addEventListener("touchmove", this.touchMove, false);
		content.addEventListener("touchend", this.touchEnd, false);
	};

	pullEventListener.prototype.touchStart = function(event) {
		var touch = event.targetTouches[0];
		if (listernWay == pullUtil.left || listernWay == pullUtil.right) {
			start = touch.clientX;
		} else if (listernWay == pullUtil.up || listernWay == pullUtil.down) {
			start = touch.clientY;
		}
	};

	pullEventListener.prototype.touchMove = function(event) {
		var touch = event.targetTouches[0];
		if (listernWay == pullUtil.left || listernWay == pullUtil.right) {
			dist = (start - touch.clientX);
			console.log("移动 水平");
		} else if (listernWay == pullUtil.up || listernWay == pullUtil.down) {
			dist = (start - touch.clientY);
			console.log("移动竖直");
		}

		if (typeof(onPull) == "function") onPull(dist);
	};

	pullEventListener.prototype.touchEnd = function(event) {
		if (listernWay == pullUtil.left || listernWay == pullUtil.right) {
			if (dist > 0) {
				console.log("右滑" + dist);
			} else {
				console.log("左滑" + dist);
			}
		} else {
			if (dist > 0) {
				console.log("上拉" + dist);
			} else {
				console.log("下拉" + dist);
			}
		}
		if (typeof(onRelease) == "function") onRelease();
	};
};

window.pullUtil = new pullUtil();

//添加滚动事件监听
function addScrollListener(onScrollCallBack) {
	if(typeof(onScrollCallBack)=="function")window.addEventListener("scroll", onScrollCallBack);
}

/**
 * 添加滚动到页面底部的事件监听
 * @param {Object} space 距底部的距离还有多少时执行 listener
 * @param {Object} listener 监听函数
 */
function onScroll2Bottom(space,listener) {
	if(space==null||typeof(space)!="number")space=10;
	addScrollListener(function() {
		if ((document.body.scrollHeight - document.documentElement.clientHeight - document.body.scrollTop) <= space) {
			if(typeof(listener)=="function")listener();
		}
	});
}

function foreach(items, action) {
	if (items == null || typeof(items) == "undefined") return;
	if (typeof(action) == "function") {
		for (var i = 0; i < items.length; i++) {
			action(items[i]);
		}
	}
}

//	给控件添加触摸效果
function btnOnTouch(buttons) {
	foreach(buttons, function(item) {
		var bgColor = item.style.backgroundColor;
		item.ontouchstart = function() {
			item.style.backgroundColor = "gray";
		}
		item.ontouchend = function() {
			item.style.backgroundColor = bgColor
		}
	});
}

window.addEventListener("load", function() {
	var registBtns = document.getElementsByClassName("register_btn");
	var logoutBtns = document.getElementsByClassName("logout-btn");
	btnOnTouch(registBtns);
	btnOnTouch(logoutBtns);
});

//获取随url传过来的参数，没有时返回空
function getUrlParam(key) {
	if (key == null || typeof(key) == "undefined" || key.length == 0) return "";
	var reg = new RegExp(key + "=([{}\\[\\]\"'a-zA-Z0-9%:,\\.\\-_]+)&{0,1}");
	var value = window.location.search;
	if (reg.test(value)) {
		value = RegExp.$1;
	} else {
		value = "";
	}
	return value;
}