package ando.guard.autoservice;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class AutoInstallAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                event.getPackageName().equals("com.android.packageinstaller")) {
            AccessibilityNodeInfo nodeInfo = AccessibilityUtils.INSTANCE.findViewByText(this,"安装", true);
            if (nodeInfo != null) {
                AccessibilityUtils.INSTANCE.performViewClick(nodeInfo);
            }
        }
    }

    @Override
    public void onInterrupt() {
    }

}