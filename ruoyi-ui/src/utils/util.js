let util = {};

util.isFunction = function(o) {
    return Object.prototype.toString.call(o) === '[object Function]';
}

util.isArray = function(o) {
    return Object.prototype.toString.call(o) === '[object Array]';
}

util.isNone = function(o) {
    return o == undefined || o == null;
}


util.join = function(separator, ...strs) {
    let joinStr = "";
    for(let str of strs) {
        joinStr += separator + str;
    }

    if(joinStr.length > 0) {
        return joinStr.substring(separator.length);
    }

    return joinStr;
}

// 驼峰转换下划线
util.toLine = function (name) {
    return name.replace(/([A-Z])/g, "_$1").toLowerCase();
}

util.deepCopy = function (value) {
    return JSON.parse(JSON.stringify(value));
}

util.trim = function (value) {
    return (value.toString() || "").replace(/(^\s*)|(\s*$)/g, "");
}

util.isBlank = function (value) {
    return(value == undefined || value == null || util.trim(value) === "");
}

util.startsWith = function (content, str) {
    let reg = new RegExp("^" + str);
    return reg.test(content);
}

util.endsWith = function (content, str) {
    let reg = new RegExp(str + "$");
    return reg.test(content);
}

util.concatTips = function(tips) {
    let content = "";
    let numArgs = arguments.length; // 获取实际被传递参数的数值。
    for (let i = 0 ; i < numArgs; i++){ // 获取参数内容。
        content += " " + arguments[i];
    }
    return content;
}

util.simpleClone = function (obj) {
    let simpleCloneObj = {};
    for (let item in obj) {
        simpleCloneObj[item] = obj[item];
    }
    return simpleCloneObj
}

util.enhanceDate = function () {
    // 对Date的扩展，将 Date 转化为指定格式的String   
    // 月(M)、日(d)、小时(H)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符，   
    // 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字)   
    // 例子：   
    // (new Date()).Format("yyyy-MM-dd HH:mm:ss.S") ==> 2006-07-02 08:09:04.423   
    // (new Date()).Format("yyyy-M-d H:m:s.S")      ==> 2006-7-2 8:9:4.18   
    Date.prototype.format = function (fmt) { //author: meizz
        var o = {
            "M+": this.getMonth() + 1, //月份   
            "d+": this.getDate(), //日   
            "h+": this.getHours(), //小时   
            "m+": this.getMinutes(), //分   
            "s+": this.getSeconds(), //秒   
            "q+": Math.floor((this.getMonth() + 3) / 3), //季度   
            "S": this.getMilliseconds() //毫秒   
        };
        if (/(y+)/.test(fmt))
            fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
        for (var k in o)
            if (new RegExp("(" + k + ")").test(fmt))
                fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        return fmt;
    }
}

util.todayZero = function () {
    return new Date(new Date().toLocaleDateString()).getTime();
}

util.formatCurrency = function (num) {
    num = num.toString().replace(/\$|,/g, '');
    if (isNaN(num))
        num = "0";
    let sign = (num == (num = Math.abs(num)));
    num = Math.floor(num * 100 + 0.50000000001);
    let cents = num % 100;
    num = Math.floor(num / 100).toString();
    if (cents < 10)
        cents = "0" + cents;
    for (var i = 0; i < Math.floor((num.length - (1 + i)) / 3); i++)
        num = num.substring(0, num.length - (4 * i + 3)) + ',' +
            num.substring(num.length - (4 * i + 3));
    return (((sign) ? '' : '-') + num + '.' + cents);
}

util.validateCommon = function(oHxValidator, validatorKey, rule, value, callback) {
    let errorCode = oHxValidator.validate(validatorKey, value);
    if (!callback) {
        return errorCode;
    }

    if (errorCode) {
        callback(new Error(errorCode));
    }
    callback();
}


util.arrayBufferToBase64 = function (buffer) {
    let binary = '';
    let bytes = new Uint8Array(buffer);
    let len = bytes.byteLength;
    for (let i = 0; i < len; i++) {
        binary += String.fromCharCode(bytes[i]);
    }
    return window.btoa(binary);
}

export default util;