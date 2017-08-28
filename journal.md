# Alarm
By default, all alarms are canceled when a device shuts down. 
To prevent this from happening, you can design your application to automatically restart a repeating alarm if the user reboots the device. 
>https://developer.android.com/training/scheduling/alarms.html
>https://developer.android.com/reference/android/app/AlarmManager.html

# Broadcast
The broadcast message itself is wrapped in an Intent object whose action string identifies the event that occurred (for example android.intent.action.AIRPLANE_MODE).

Apps can receive broadcasts in two ways: through manifest-declared receivers and context-registered receivers.

If you declare a broadcast receiver in your manifest, the system launches your app (if the app is not already running) when the broadcast is sent.
实际测试，在小米的机子上，应用被强制退出后不能收到广播

On Android 3.1+, no. Once the user has force-stopped your application, you will receive no more broadcast Intents, of any kind, until the user manually starts one of your activities.
>https://stackoverflow.com/questions/9240200/can-i-still-receive-broadcast-receiver-intent-after-i-force-stopped-my-app-on-an
>https://developer.android.com/about/versions/android-3.1.html#launchcontrols

>https://developer.android.com/guide/components/broadcasts.html

# Database
>https://developer.android.com/training/basics/data-storage/databases.html
---

>https://stackoverflow.com/questions/40480355/pass-serializable-object-to-pending-intent