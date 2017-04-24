# BaiduLBS
百度LBS

安装： 
cordova plugin add https://github.com/2026635978/cordova-plugin-BaiduLBS --variable AK=[BaiduLBS的访问应用（AK）]
卸载：
cordova plugin rm cordova-plugin-BaiduLBS
其中，
cordova-plugin-BaiduLBS是该插件的id，为保持一致性，github的路径也是用了该名称。
AK：BaiduLBS的访问应用（AK，即APP_KEY）

使用方法：

      cordova.plugins.BaiduLBS.getCurrentLocation(
        function (location) {
            // TODO
        },
        function (err) {
          // TODO
        }
      );