package seker.monitor;

import seker.common.BaseApplication;
import android.view.WindowManager;

/**
 * 
 * @author liuxinjian
 * @since 2013-8-20
 */
public class MonitorApp extends BaseApplication {

    private WindowManager.LayoutParams mWmParams = new WindowManager.LayoutParams();

    public WindowManager.LayoutParams getMywmParams() {
        return mWmParams;
    }
}
