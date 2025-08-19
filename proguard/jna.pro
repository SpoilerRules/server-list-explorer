-keep class com.sun.jna.** {
    *;
}
-keepclassmembers class com.sun.jna.** {
    native <methods>;
}

-keep class com.sun.jna.platform.** {
    *;
}

-keepclassmembers class * extends com.sun.jna.Structure {
    <fields>;
    <methods>;
}

-keep class * extends com.sun.jna.Library { *; }
-keep class * extends com.sun.jna.Callback { *; }
