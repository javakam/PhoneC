package ando.guard.autoservice;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

/**
 * https://github.com/xuyisheng/AccessibilityUtil
 */
public class BilibiliAccessibility extends AccessibilityService {

    private static final String TAG = "123";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "MyAccessibility onCreate ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "MyAccessibility onDestroy ");
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.e(TAG, "onAccessibilityEvent: " + event.toString());

        try {
            AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
            if (rootInfo != null) {
                findNodeInfoByDFS(rootInfo);
            }

            //AccessibilityUtils.INSTANCE.findNodesByText(this,NOTIFY_TEXT);
            /*AccessibilityNodeInfo nodeInfo = AccessibilityUtils.INSTANCE.findViewByText(this, NOTIFY_TEXT, true);
            if (nodeInfo != null) {
                AccessibilityUtils.INSTANCE.performViewClick(nodeInfo);
            }*/
        } catch (Exception e) {
            ztLog("Exception:" + e.getMessage(), true);
        }
    }

    private static final String NOTIFY_VIEW = "android.widget.TextView";
    private static final String NOTIFY_TEXT = "会员购中心";

    /**
     * 深度优先遍历寻找目标节点
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void findNodeInfoByDFS(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null || TextUtils.isEmpty(nodeInfo.getClassName())) {
            return;
        }

        if (!TextUtils.equals(nodeInfo.getClassName(), NOTIFY_VIEW)) {
            ztLog(nodeInfo.getClassName().toString());
            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                findNodeInfoByDFS(nodeInfo.getChild(i));
            }
        } else {
            ztLog("==find==");
            if (!TextUtils.isEmpty(nodeInfo.getText()) && nodeInfo.getText().toString().equals(NOTIFY_TEXT)) {
                AccessibilityNodeInfo parent = nodeInfo.getParent();
                Log.w(TAG, "==text111 == " + nodeInfo.getClassName() + "  "
                        + nodeInfo.getText().toString() + "  " + nodeInfo.getViewIdResourceName() + "  "
                        + parent.getViewIdResourceName());

                AccessibilityUtils.INSTANCE.inputText(nodeInfo, "Hello");

                if (parent.getViewIdResourceName() == null) {
                    performClick(parent);
                    return;
                }
            }

            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                final AccessibilityNodeInfo frameLayoutInfo = nodeInfo.getChild(i);
                if (!TextUtils.isEmpty(frameLayoutInfo.getText()) && frameLayoutInfo.getText().toString().equals(NOTIFY_TEXT)) {
                    Log.w(TAG, "==text222 ==");
                    performClick(frameLayoutInfo);
                    return;
                }

                final AccessibilityNodeInfo childInfo = frameLayoutInfo.getChild(0);
                Log.w(TAG, "==text333 ==");

                String text = childInfo.getText().toString();
                if (text.equals(NOTIFY_TEXT)) {
                    Log.w(TAG, "==result ==");
                    performClick(frameLayoutInfo);
                } else {
                    ztLog(text);
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void performClick(AccessibilityNodeInfo targetInfo) {
        //targetInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    }

    private void ztLog(String str) {
        ztLog(str, false);
    }

    private void ztLog(String str, boolean showToast) {
        Log.i(TAG, str);
        if (showToast) {
            Toast.makeText(this, str, Toast.LENGTH_LONG).show();
        }
    }
}