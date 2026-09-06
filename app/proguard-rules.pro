# Proguard rules for Egyptian Legal Deadlines Calculator

# Keep Room generated classes
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Retrofit and Gson DTOs
-keep class com.ateflaw.legaldeadlines.data.dto.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Domain Models
-keep class com.ateflaw.legaldeadlines.domain.model.** { *; }
