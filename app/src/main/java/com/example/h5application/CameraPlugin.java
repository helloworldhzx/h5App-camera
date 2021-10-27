package com.example.h5application;

import android.content.Intent;
import android.util.Log;
import org.json.JSONArray;

import io.dcloud.common.DHInterface.IApp;
import io.dcloud.common.DHInterface.ISysEventListener;
import io.dcloud.common.DHInterface.IWebview;
import io.dcloud.common.DHInterface.StandardFeature;
import io.dcloud.common.util.JSUtil;

import com.example.h5application.utils.AppConstant;


public class CameraPlugin extends StandardFeature {

    public void OpenCamera(IWebview pWebview, JSONArray array){
        final IWebview iWebview = pWebview;
        final String CallBackID = array.optString(0);
        //添加监听
        final IApp _app = pWebview.obtainFrameView().obtainApp();
        _app.registerSysEventListener(new ISysEventListener() {
            @Override
            public boolean onExecute(SysEventType pEventType, Object pArgs) {

                Object[] _args = (Object[]) pArgs;
                int requestCode = (Integer) _args[0];
                int resultCode = (Integer) _args[1];

                Intent data = (Intent) _args[2];

                //[Ljava.lang.Object;@e89a7c8
                Log.d("aaa", "onExecute: _args " + _args);
                //1
                Log.d("aaa", "onExecute: requestCode " + requestCode);
                //-1
                Log.d("aaa", "onExecute: resultCode " + resultCode);
                //Intent { (has extras) }
                Log.d("aaa", "onExecute: data " + data);

                if (pEventType == SysEventType.onActivityResult) {
                    //用完给取消注册监听
                    _app.unregisterSysEventListener(this, SysEventType.onActivityResult);

                    //判断请求码
                    if (requestCode == 0) {
                        //获取返回值
                        String returnData = data.getStringExtra(AppConstant.KEY.IMG_PATH);
                        //执行 js 回调
                        JSUtil.execCallback(iWebview, CallBackID, returnData, JSUtil.OK, false);
                        Log.d("aaa", "返回的数据为：" + returnData);
                    }
                }
                return false;
            }
        }, SysEventType.onActivityResult);
        Intent intent = new Intent(getDPluginContext(), MainActivity.class);
        pWebview.getActivity().startActivityForResult(intent, 0);
    }
}
