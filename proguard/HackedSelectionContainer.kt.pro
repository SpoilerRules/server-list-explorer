-keepattributes *Annotation*,InnerClasses,EnclosingMethod,Signature,Exceptions

-keep class androidx.compose.foundation.text.selection.** { *; }
-keep class androidx.compose.ui.text.** { *; }
-keep class androidx.compose.foundation.text.** { *; }
-keep class androidx.compose.foundation.ContextMenu* { *; }

-keepclassmembers class **$Companion { *; }

-keep class androidx.compose.foundation.text.selection.SelectionRegistrarKt { *; }
-keep class androidx.compose.foundation.text.selection.SelectionKt { *; }
-keep class androidx.compose.foundation.text.selection.SelectionJvmKt { *; }
-keep class androidx.compose.foundation.text.selection.Selection_desktopKt { *; }
-keep class androidx.compose.foundation.text.selection.Selection { *; }