package com.example.lenovo_g50_70.baidumap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //基础地图
    private MapView mMapView;
    //使用定位
    private BaiduMap mBaiduMap;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    //城市检索
    public PoiSearch mPoiSearch;
    //基础控件
    private Button btnPoiSearch;
    private EditText edtPoiSearch;
    private String NowCity;
    private String NowPoi;
    private LatLng mLatLng;
    //地图覆盖物
    private List<OverlayOptions> mOptionses=new ArrayList<>();
    //清除覆盖物
    private List<Marker> mMarkers=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        btnPoiSearch = (Button) findViewById(R.id.btnPoiSearch);
        edtPoiSearch = (EditText) findViewById(R.id.edtPoiSearch);
        mMapView = (MapView) findViewById(R.id.bmapView);

        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        //注册监听函数
        mBaiduMap = mMapView.getMap();
        //设置地图显示的类型
        //mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        initLocation();
        mLocationClient.start();

        mPoiSearch = PoiSearch.newInstance();
        OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {

            public void onGetPoiResult(PoiResult result) {
                //获取POI检索结果
                if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {// 没有找到检索结果
                    Toast.makeText(MainActivity.this, "未找到结果", Toast.LENGTH_LONG).show();
                    return;
                }

                else {
                    if (result.getAllPoi().size()>0) {
                        mOptionses.clear();
                        mBaiduMap.clear();
                        Toast.makeText(MainActivity.this,result.getAllPoi().get(0).name, Toast.LENGTH_SHORT).show();
                        for (int i = 0; i < result.getAllPoi().size(); i++) {
                            Log.e("TAG", result.getAllPoi().get(i).name);
                            Log.e("TAG", result.getAllPoi().get(i).address);
                            //定义Maker坐标点
                            //LatLng point = new LatLng();
                            //构建Marker图标
                            BitmapDescriptor bitmap = BitmapDescriptorFactory
                                .fromResource(R.drawable.icon_eat);
                            //构建MarkerOption，用于在地图上添加Marker
                            OverlayOptions option = new MarkerOptions()
                                .position(result.getAllPoi().get(i).location)
                                .icon(bitmap);
                            //在地图上添加Marker，并显示
                            mOptionses.add(option);//覆盖物List，添加到地图
                            Marker marker = (Marker)mBaiduMap.addOverlay(option);
                            mMarkers.add(marker);
                        }
                        mBaiduMap.addOverlays(mOptionses);
                    }
                }
            }

            public void onGetPoiDetailResult(PoiDetailResult result) {
                //获取Poi详情页检索结果
                if (result.error != SearchResult.ERRORNO.NO_ERROR) {
                    //详情检索失败,result.error请参考SearchResult.ERRORNO
                } else {
                    //检索成功
                }
            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
                //获取Poi户内结果
            }
        };

        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
        btnPoiSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NowPoi =edtPoiSearch.getText().toString().trim();//编辑框的内容
                //城市内检索
//                mPoiSearch.searchInCity((new PoiCitySearchOption())
//                        .city(NowCity)
//                        .keyword(NowPoi)
//                        .pageNum(0));
                //获取检索结果的第几页，假如结果只有2页，你输入9，它从第10页找，那么结果则返回空
                //每页存储10条信息
                mPoiSearch.searchNearby(getSearchParams());
            }
        });

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return false;
            }
        });
    }

    private PoiNearbySearchOption getSearchParams() {
        PoiNearbySearchOption params =new PoiNearbySearchOption();
        params.location(mLatLng);//指定当前位置搜索
        params.radius(1000);//指定半径为1000米
        params.keyword(NowPoi);
        //设置每一页搜索结果的个数，默认为10个
        params.pageCapacity(10);
        return params;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        // 当不需要定位图层时关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mPoiSearch.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系

        int span = 1000;
        option.setScanSpan(0);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps

        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集

        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        mLocationClient.setLocOption(option);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            NowCity = location.getCity();
            Log.e("NowCity", NowCity);
            // 开启定位图层
            mBaiduMap.setMyLocationEnabled(true);
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);
            // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
            //mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
            //MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
            mBaiduMap.setMyLocationData(locData);


            mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(mLatLng);
            mMapView.getMap().setMapStatus(status);//直接到中间
            mMapView.getMap().setMapStatus(MapStatusUpdateFactory.zoomTo(16));
            mLocationClient.stop();

            //获取定位结果
            StringBuffer sb = new StringBuffer(256);

            sb.append("time : ");
            sb.append(location.getTime());    //获取定位时间

            sb.append("\nerror code : ");
            sb.append(location.getLocType());    //获取类型类型

            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());    //获取纬度信息

            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());    //获取经度信息

            sb.append("\nradius : ");
            sb.append(location.getRadius());    //获取定位精准度

            if (location.getLocType() == BDLocation.TypeGpsLocation) {

                // GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());    // 单位：公里每小时

                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());    //获取卫星数

                sb.append("\nheight : ");
                sb.append(location.getAltitude());    //获取海拔高度信息，单位米

                sb.append("\ndirection : ");
                sb.append(location.getDirection());    //获取方向信息，单位度

                sb.append("\naddr : ");
                sb.append(location.getAddrStr());    //获取地址信息

                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {

                // 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());    //获取地址信息

                sb.append("\noperationers : ");
                sb.append(location.getOperators());    //获取运营商信息

                sb.append("\ndescribe : ");
                sb.append("网络定位成功");

            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {

                // 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");

            } else if (location.getLocType() == BDLocation.TypeServerError) {

                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");

            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {

                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");

            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {

                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");

            }

            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());    //位置语义化信息

            List<Poi> list = location.getPoiList();    // POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }

            Log.i("BaiduLocationApiDem", sb.toString());
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }
}