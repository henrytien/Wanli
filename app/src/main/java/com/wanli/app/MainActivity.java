package com.wanli.app;

        import com.baidu.location.BDLocation;
        import com.baidu.location.BDLocationListener;
        import com.baidu.location.LocationClient;
        import com.baidu.location.LocationClientOption;
        import com.baidu.mapapi.SDKInitializer;
        import com.baidu.mapapi.map.BaiduMap;
        import com.baidu.mapapi.map.BitmapDescriptor;
        import com.baidu.mapapi.map.BitmapDescriptorFactory;
        import com.baidu.mapapi.map.CircleOptions;
        import com.baidu.mapapi.map.MapStatusUpdate;
        import com.baidu.mapapi.map.MapStatusUpdateFactory;
        import com.baidu.mapapi.map.MapView;
        import com.baidu.mapapi.map.MarkerOptions;
        import com.baidu.mapapi.map.MyLocationConfiguration;
        import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
        import com.baidu.mapapi.map.MyLocationData;
        import com.baidu.mapapi.map.OverlayOptions;
        import com.baidu.mapapi.map.Stroke;
        import com.baidu.mapapi.model.LatLng;
        import android.app.Activity;
        import android.os.Bundle;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.Button;
        import android.widget.Toast;

public class MainActivity extends Activity {

    //百度地图控件
    private MapView mMapView = null;
    //百度地图对象
    private BaiduMap mBaiduMap;
    //按钮 添加覆盖物
    private Button addOverlayBtn;
    //是否显示覆盖物 1-显示 0-不显示
    private int isShowOverlay = 1;
    //按钮 定位当前位置
    private Button locCurplaceBtn;
    //是否首次定位
    private boolean isFirstLoc = true;
    //定位SDK的核心类
    private LocationClient mLocClient;
    //定位图层显示模式 (普通-跟随-罗盘)
    private LocationMode mCurrentMode;
    //定位图标描述
    private BitmapDescriptor mCurrentMarker = null;
    //当前位置经纬度
    private double latitude;
    private double longitude;
    //定位SDK监听函数
    public MyLocationListenner locListener = new MyLocationListenner();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        //获取地图控件
        mMapView = (MapView) findViewById(R.id.map_view);
        addOverlayBtn = (Button) findViewById(R.id.btn_add_overlay);
        locCurplaceBtn = (Button) findViewById(R.id.btn_cur_place);
        addOverlayBtn.setEnabled(false);

        //设置地图缩放级别16 类型普通地图
        mBaiduMap = mMapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(16.0f);
        mBaiduMap.setMapStatus(msu);
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);

        //开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        //定位初始化
        //注意: 实例化定位服务 LocationClient类必须在主线程中声明 并注册定位监听接口
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(locListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);              //打开GPS
        option.setCoorType("bd09ll");        //设置坐标类型
        option.setScanSpan(5000);            //设置发起定位请求的间隔时间为5000ms
        mLocClient.setLocOption(option);     //设置定位参数
        mLocClient.start();                  //调用此方法开始定位

        //Button 添加覆盖物
        addOverlayBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addCircleOverlay();
            }
        });

        //Button 定位当前位置
        locCurplaceBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addMyLocation();
            }
        });

    }

    /**
     * 定位SDK监听器 需添加locSDK jar和so文件
     */
    public class MyLocationListenner implements BDLocationListener {

        public void onReceivePoi(BDLocation location) {
        }

        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapview 销毁后不在处理新接收的位置
            if (location == null || mBaiduMap == null) {
                return;
            }
            //MyLocationData.Builder定位数据建造器
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(100)
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();
            //设置定位数据
            mBaiduMap.setMyLocationData(locData);
            mCurrentMode = LocationMode.NORMAL;
            //获取经纬度
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            //Toast.makeText(getApplicationContext(), String.valueOf(latitude), Toast.LENGTH_SHORT).show();
            //第一次定位的时候，那地图中心点显示为定位到的位置
            if (isFirstLoc) {
                isFirstLoc = false;
                //地理坐标基本数据结构
                LatLng loc = new LatLng(location.getLatitude(),location.getLongitude());
                //MapStatusUpdate描述地图将要发生的变化
                //MapStatusUpdateFactory生成地图将要反生的变化
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(loc);
                mBaiduMap.animateMapStatus(msu);
                Toast.makeText(getApplicationContext(), location.getAddrStr(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 定位并添加标注
     */
    private void addMyLocation() {
        //更新
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                mCurrentMode, true, mCurrentMarker));
        mBaiduMap.clear();
        addOverlayBtn.setEnabled(true);
        //定义Maker坐标点
        LatLng point = new LatLng(latitude, longitude);
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);
    }

    /**
     * 添加覆盖物
     */
    private void addCircleOverlay() {
        if(isShowOverlay == 1) {  //点击显示
            mBaiduMap.clear();
            isShowOverlay = 0;
            //DotOptions 圆点覆盖物
            LatLng pt = new LatLng(latitude, longitude);
            CircleOptions circleOptions = new CircleOptions();
            //circleOptions.center(new LatLng(latitude, longitude));
            circleOptions.center(pt);                          //设置圆心坐标
            circleOptions.fillColor(0xAAFFFF00);               //圆填充颜色
            circleOptions.radius(250);                         //设置半径
            circleOptions.stroke(new Stroke(5, 0xAA00FF00));   // 设置边框
            mBaiduMap.addOverlay(circleOptions);
        }
        else {
            mBaiduMap.clear();
            isShowOverlay = 1;
        }
    }


    protected void onDestroy() {
        mLocClient.stop();                       //退出时销毁定位
        mBaiduMap.setMyLocationEnabled(false);   //关闭定位图层
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }
}