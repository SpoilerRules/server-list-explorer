-keep class androidx.compose.ui.input.pointer.** { *; }
-keep class androidx.compose.ui.window.** { *; }

# Required: Used via reflection in FloatingDialogBuilder.kt
# - Class.forName("androidx.compose.material3.TooltipScopeImpl") and its constructor
#   are invoked explicitly, so the class name and members must remain unchanged.
-keep class androidx.compose.material3.TooltipScopeImpl {
    <init>(...);
    *;
}
