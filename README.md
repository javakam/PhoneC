
## 黑名单(BlockedNumbers)

1.单个手机号添加

2.导出本机黑名单(json文件)

3.导入外部黑名单(json文件)

## Bilibili

1.热榜屏蔽不喜欢的UP(根据uid)

2.屏蔽一些用户的所有信息(此人发的视频和言论等等, 根据uid)


## 设置`APP`为默认拨号程序

```kotlin
@TargetApi(Build.VERSION_CODES.M)
protected fun launchSetDefaultDialerIntent() {
    Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName).apply {
        if (resolveActivity(packageManager) != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val rm: RoleManager? = getSystemService(RoleManager::class.java)
                if (rm?.isRoleAvailable(RoleManager.ROLE_DIALER) == true) {
                    startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_DIALER), REQUEST_CODE_SET_DEFAULT_DIALER)
                }
            } else {
                startActivityForResult(this, REQUEST_CODE_SET_DEFAULT_DIALER)
            }
        } else {
            toast(R.string.no_app_found)
        }
    }
}
```

## Bug
```kotlin
java.lang.SecurityException: Caller must be system, default dialer or default SMS app
```
Fixed: 需要把应用设置成默认的拨号程序
```kotlin
@SuppressLint("QueryPermissionsNeeded")
@TargetApi(Build.VERSION_CODES.M)
fun launchSetDefaultDialerIntent(activity: AppCompatActivity) {
    Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).putExtra(
        TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
        activity.packageName
    ).apply {
        if (resolveActivity(activity.packageManager) != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val rm: RoleManager? = activity.getSystemService(RoleManager::class.java)
                if (rm?.isRoleAvailable(RoleManager.ROLE_DIALER) == true) {
                    @Suppress("DEPRECATION")
                    activity.startActivityForResult(
                        rm.createRequestRoleIntent(RoleManager.ROLE_DIALER),
                        REQUEST_CODE_SET_DEFAULT_DIALER
                    )
                }
            } else {
                @Suppress("DEPRECATION")
                activity.startActivityForResult(this, REQUEST_CODE_SET_DEFAULT_DIALER)
            }
        } else {
            activity.toastShort(R.string.no_contacts_found)
        }
    }
}
```
🍎 发现`startActivityForResult`不生效需要在相应`Acticity`的清单文件中加入
```kotlin
//跳转无效
val rm: RoleManager? = activity.getSystemService(RoleManager::class.java)
startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_DIALER),
         REQUEST_CODE_SET_DEFAULT_DIALER)
```
`AndroidManifest.xml`中加入
```xml
<activity android:name=".ui.blocked.BlockedNumbersActivity" >
    <!-- 提供打电话的UI -->
    <intent-filter>
        <action android:name="android.intent.action.DIAL"/>
        <action android:name="android.intent.action.VIEW"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <category android:name="android.intent.category.BROWSABLE"/>
        <data android:scheme="tel"/>
    </intent-filter>
    <!-- region provides dial UI -->
    <intent-filter>
        <action android:name="android.intent.action.DIAL"/>
        <category android:name="android.intent.category.DEFAULT"/>
    </intent-filter>
</activity>
```
参考:

- https://stackoverflow.com/questions/55612361/telecommanager-action-change-default-dialer-returns-result-canceled-on-huawei-p8

- https://blog.csdn.net/qq_21916701/article/details/84062361



## Thanks

<https://github.com/zyyoona7/EasyPopup>

<https://github.com/blainepwnz/AndroidContacts>

<https://github.com/manjotpahwa/contacts-app>

<https://github.com/iostream17/MyContacts>

<https://github.com/arekolek/simple-phone>

<https://juejin.cn/post/6844903456809943053>

<https://github.com/xuyisheng/AccessibilityUtil>

<https://github.com/unclepizza/AutoClickService>

<https://blog.csdn.net/qq_28356833/article/details/83145931>

