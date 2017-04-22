package BaiduLBS;

import java.util.List;
import java.util.Set;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
//import com.baidu.location.BDNotifyListener; //假如用到位置提醒功能，需要import该类
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.location.Poi;

/**
 * This class echoes a string called from JavaScript.
 */
public class BaiduLBS extends CordovaPlugin {

	private static final String TAG = BaiduLBS.class.getSimpleName();

	private void checkAPIKEY() {
		Activity activity = this.cordova.getActivity();
		android.content.pm.PackageManager pm = activity.getPackageManager();
		String pn = activity.getPackageName();
		android.content.pm.ApplicationInfo ai;
		try {
			ai = pm.getApplicationInfo(pn,
					android.content.pm.PackageManager.GET_META_DATA);
			Set<String> keys = ai.metaData.keySet();
			StringBuilder sb = new StringBuilder();
			for (String key : keys) {
				sb.append(key).append(":").append(ai.metaData.get(key))
						.append("\n");
			}
			android.widget.Toast.makeText(activity,
					"metaData = " + sb.toString(),
					android.widget.Toast.LENGTH_LONG).show();
			String API_KEY = ai.metaData.getString("com.baidu.lbsapi.API_KEY");
			Log.i(TAG, "checkAPIKEY. api_key = " + API_KEY);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {

		checkAPIKEY();

		Log.i(TAG, "execute. action = " + action);
		if (action.equals("getCurrentLocation")) {

			// 第一步，初始化LocationClient类
			if (null == mLocationClient) {
				Context context = this.cordova.getActivity()
						.getApplicationContext();
				mLocationClient = new LocationClient(context);
				// 声明LocationClient类
			}
			if (null == myListener) {
				myListener = new MyLocationListener(callbackContext);
				mLocationClient.registerLocationListener(myListener);
				// 注册监听函数
			}

			// 第二步，配置定位SDK参数
			initLocation(mLocationClient);

			// 第四步，开始定位
			// 开启：
			Log.i(TAG, "execute. location client stop");
			mLocationClient.start();
			return true;
		}
		return false;
	}

	// ------------------------------------------------------------------------------------------------------------------------------------------------

	private LocationClient mLocationClient;
	private BDLocationListener myListener;

	// 第二步，配置定位SDK参数
	// 设置定位参数包括：定位模式（高精度定位模式、低功耗定位模式和仅用设备定位模式），返回坐标类型，是否打开GPS，是否返回地址信息、位置语义化信息、POI信息等等。
	// 返回坐标类型包括：
	// 1. gcj02：国测局坐标；
	// 2. bd09：百度墨卡托坐标；
	// 3. bd09ll：百度经纬度坐标；
	// 注意：海外地区定位结果默认、且只能是WGS84类型坐标。
	private void initLocation(LocationClient mLocationClient) {
		Log.i(TAG, "initLocation.");
		// LocationClientOption类，该类用来设置定位SDK的定位方式
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);
		// 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
		// 高精度定位模式：这种定位模式下，会同时使用网络定位和GPS定位，优先返回最高精度的定位结果；
		// 低功耗定位模式：这种定位模式下，不会使用GPS进行定位，只会使用网络定位（WiFi定位和基站定位）；
		// 仅用设备定位模式：这种定位模式下，不需要连接网络，只使用GPS进行定位，这种模式下不支持室内环境的定位。

		option.setCoorType("bd09ll");
		// 可选，默认gcj02，设置返回的定位结果坐标系

		int span = 1000;
		option.setScanSpan(span);
		// 可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

		option.setIsNeedAddress(true);
		// 可选，设置是否需要地址信息，默认不需要

		option.setOpenGps(true);
		// 可选，默认false,设置是否使用gps

		option.setLocationNotify(true);
		// 可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

		option.setIsNeedLocationDescribe(true);
		// 可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

		option.setIsNeedLocationPoiList(true);
		// 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

		option.setIgnoreKillProcess(false);
		// 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

		option.SetIgnoreCacheException(false);
		// 可选，默认false，设置是否收集CRASH信息，默认收集

		option.setEnableSimulateGps(false);
		// 可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

		mLocationClient.setLocOption(option);
	}

	// 第三步，实现BDLocationListener接口
	// BDLocationListener为结果监听接口，异步获取定位结果，实现方式如下：
	public class MyLocationListener implements BDLocationListener {

		public MyLocationListener(CallbackContext callbackContext) {
			setCallbackContext(callbackContext);
		}

		private CallbackContext callbackContext;

		public void setCallbackContext(CallbackContext callbackContext) {
			this.callbackContext = callbackContext;
		}

		@Override
		public void onReceiveLocation(BDLocation location) {
			Log.i(TAG, "BDLocationListener.onReceiveLocation. location = "
					+ location);

			//
			JSONObject json = new JSONObject();

			// 获取定位结果
			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime()); // 获取定位时间
			sb.append("\nerror code : ");
			sb.append(location.getLocType()); // 获取类型类型
			sb.append("\nlatitude : ");
			sb.append(location.getLatitude()); // 获取纬度信息
			sb.append("\nlontitude : ");
			sb.append(location.getLongitude()); // 获取经度信息
			sb.append("\nradius : ");
			sb.append(location.getRadius()); // 获取定位精准度
			//
			try {
				json.put("time", location.getTime());
				json.put("locType", location.getLocType());
				json.put("latitude", location.getLatitude());
				json.put("lontitude", location.getLongitude());
				json.put("radius", location.getRadius());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//
			boolean successful = false;
			if (location.getLocType() == BDLocation.TypeGpsLocation) {
				//
				successful = true;

				// GPS定位结果
				sb.append("\nLocType : ");
				sb.append(location.getLocType()); // 获取定位返回码
				sb.append("\nspeed : ");
				sb.append(location.getSpeed()); // 单位：公里每小时
				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber()); // 获取卫星数
				sb.append("\nheight : ");
				sb.append(location.getAltitude()); // 获取海拔高度信息，单位米
				sb.append("\ndirection : ");
				sb.append(location.getDirection()); // 获取方向信息，单位度
				sb.append("\naddr : ");
				sb.append(location.getAddrStr()); // 获取地址信息
				sb.append("\ndescribe : ");
				sb.append("gps定位成功");
				//
				try {
					json.put("LocType", location.getLocType());
					json.put("speed", location.getSpeed());
					json.put("satellite", location.getSatelliteNumber());
					json.put("height", location.getAltitude());
					json.put("direction", location.getDirection());
					json.put("addr", location.getAddrStr());
					json.put("describe", "gps定位成功");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				//
				successful = true;

				// 网络定位结果
				sb.append("\nLocType : ");
				sb.append(location.getLocType()); // 获取定位返回码
				sb.append("\naddr : ");
				sb.append(location.getAddrStr()); // 获取地址信息
				sb.append("\noperationers : ");
				sb.append(location.getOperators()); // 获取运营商信息
				sb.append("\ndescribe : ");
				sb.append("网络定位成功");
				//
				try {
					json.put("LocType", location.getLocType());
					json.put("addr", location.getAddrStr());
					json.put("operationers", location.getOperators());
					json.put("describe", "网络定位成功");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
				//
				successful = true;

				// 离线定位结果
				sb.append("\nLocType : ");
				sb.append(location.getLocType()); // 获取定位返回码
				sb.append("\ndescribe : ");
				sb.append("离线定位成功，离线定位结果也是有效的");
				//
				try {
					json.put("LocType", location.getLocType());
					json.put("describe", "离线定位成功，离线定位结果也是有效的");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (location.getLocType() == BDLocation.TypeServerError) {
				//
				successful = false;

				//
				sb.append("\nLocType : ");
				sb.append(location.getLocType()); // 获取定位返回码
				sb.append("\ndescribe : ");
				sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
				//
				try {
					json.put("LocType", location.getLocType());
					json.put("describe",
							"服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (location.getLocType() == BDLocation.TypeNetWorkException) {
				//
				successful = false;

				//
				sb.append("\nLocType : ");
				sb.append(location.getLocType()); // 获取定位返回码
				sb.append("\ndescribe : ");
				sb.append("网络不同导致定位失败，请检查网络是否通畅");
				//
				try {
					json.put("LocType", location.getLocType());
					json.put("describe", "网络不同导致定位失败，请检查网络是否通畅");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (location.getLocType() == BDLocation.TypeCriteriaException) {
				//
				successful = false;

				//
				sb.append("\nLocType : ");
				sb.append(location.getLocType()); // 获取定位返回码
				sb.append("\ndescribe : ");
				sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
				//
				try {
					json.put("LocType", location.getLocType());
					json.put("describe",
							"无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			sb.append("\nlocationdescribe : ");
			sb.append(location.getLocationDescribe()); // 位置语义化信息
			//
			try {
				json.put("locationdescribe", location.getLocationDescribe());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			List<Poi> list = location.getPoiList(); // POI数据
			if (list != null) {
				JSONArray pois = new JSONArray();
				sb.append("\npoilist size = : ");
				sb.append(list.size());
				for (Poi p : list) {
					sb.append("\npoi= : ");
					sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
					//
					JSONObject poi = new JSONObject();
					try {
						poi.put("id", p.getId());
						poi.put("name", p.getName());
						poi.put("rank", p.getRank());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					pois.put(poi);
				}
				try {
					json.put("pois", pois);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			Log.i(TAG, "BDLocationListener.onReceiveLocation. " + sb.toString());

			// 第五步，开始定位
			// 关闭：
			if (null != mLocationClient) {
				Log.i(TAG,
						"BDLocationListener.onReceiveLocation. location client stop");
				mLocationClient.stop();
				mLocationClient = null;
			}

			// 提交数据结果
			Log.i(TAG,
					"BDLocationListener.onReceiveLocation. data = "
							+ json.toString());
			if (successful) {
				callbackContext.success(json);
			} else {
				callbackContext.error(json);
			}
		}

		@Override
		public void onConnectHotSpotMessage(String arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		// BDLocation类，封装了定位SDK的定位结果，在BDLocationListener的onReceive方法中获取。通过该类用户可以获取错误码，位置的坐标，精度半径等信息。具体方法请参考类参考。
		// 获取定位返回错误码：：
		// public int getLocType ( )
		// 返回值：
		// 61 ： GPS定位结果，GPS定位成功。
		// 62 ： 无法获取有效定位依据，定位失败，请检查运营商网络或者WiFi网络是否正常开启，尝试重新请求定位。
		// 63 ： 网络异常，没有成功向服务器发起请求，请确认当前测试手机网络是否通畅，尝试重新请求定位。
		// 65 ： 定位缓存的结果。
		// 66 ： 离线定位结果。通过requestOfflineLocaiton调用时对应的返回结果。
		// 67 ： 离线定位失败。通过requestOfflineLocaiton调用时对应的返回结果。
		// 68 ： 网络连接失败时，查找本地离线定位时对应的返回结果。
		// 161： 网络定位结果，网络定位成功。
		// 162： 请求串密文解析失败，一般是由于客户端SO文件加载失败造成，请严格参照开发指南或demo开发，放入对应SO文件。
		// 167： 服务端定位失败，请您检查是否禁用获取位置信息权限，尝试重新请求定位。
		// 502： AK参数错误，请按照说明文档重新申请AK。
		// 505：AK不存在或者非法，请按照说明文档重新申请AK。
		// 601： AK服务被开发者自己禁用，请按照说明文档重新申请AK。
		// 602： key
		// mcode不匹配，您的AK配置过程中安全码设置有问题，请确保：SHA1正确，“;”分号是英文状态；且包名是您当前运行应用的包名，请按照说明文档重新申请AK。
		// 501～700：AK验证失败，请按照说明文档重新申请AK。
		// 如果不能定位，请记住这个返回值，并到百度LBS开放平台论坛Andriod定位SDK版块中进行交流，网址：http://bbs.lbsyun.baidu.com/forum.php?mod=forumdisplay&fid=10
		// 。若返回值是162~167，请将错误码、IMEI和定位时间反馈至邮箱loc-bugs@baidu.com，以便我们跟进追查问题。

		

	}
}