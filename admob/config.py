def can_build(plat):
    return plat=="android"

def configure(env):
    if (env['platform'] == 'android'):
        env.android_add_java_dir("android/src")
        env.android_add_dependency("compile 'com.google.android.gms:play-services-ads:+'")
        env.android_add_to_manifest("android/AndroidManifestChunk.xml")
        ################################################################################
        # ADCOLONY MEDIATION: uncomment the "env" lines below, files must be downloaded
        ################################################################################
        env.android_add_maven_repository("url 'https://adcolony.bintray.com/AdColony'")
        env.android_add_dependency("compile 'com.adcolony:sdk:3.0.7'")
        # For this file, open https://bintray.com/google/mobile-ads-adapters-android/com.google.ads.mediation.adcolony#files/com/google/ads/mediation/adcolony/3.0.6.0
        # download the adcolony-3.0.6.0.aar file, unzip it, and move/rename classes.jar to libs/adcolonyadapter.jar
        env.android_add_dependency("compile files('../../../modules/admob/android/libs/adcolonyadapter.jar')")
        env.android_add_to_manifest("android/AndroidManifestChunkAdColony.xml")
        ################################################################################
        # CHARTBOOST MEDIATION: uncomment the "env" lines below, files must be downloaded
        ################################################################################
        # and copied into the android/libs directory
        # Download this file from https://answers.chartboost.com/hc/en-us/articles/201219545
        env.android_add_dependency("compile files('../../../modules/admob/android/libs/chartboost.jar')")
        # Download this file from https://answers.chartboost.com/hc/en-us/articles/209756523-Mediation-AdMob
        env.android_add_dependency("compile files('../../../modules/admob/android/libs/ChartboostAdapter.jar')")
        env.android_add_to_manifest("android/AndroidManifestChunkChartboost.xml")
        ################################################################################
        # VUNGLE MEDIATION (not working yet): uncomment the "env" lines below, files must be downloaded
        ################################################################################
        # Download those files from https://support.vungle.com/hc/en-us/articles/207604108-QuickStart-Guide-for-AdMob-Mediation-Vungle-Android-#IntegVungle
        #env.android_add_dependency("compile files('../../../modules/admob/android/libs/dagger-2.7.jar')")
        #env.android_add_dependency("compile files('../../../modules/admob/android/libs/javax.inject-1.jar')")
        #env.android_add_dependency("compile files('../../../modules/admob/android/libs/vungle-publisher-adaptive-id-4.0.3.jar')")
        #env.android_add_dependency("compile files('../../../modules/admob/android/libs/android/libs/VungleAdapter.jar')")
        #env.android_add_to_manifest("android/AndroidManifestChunkVungle.xml")
        env.disable_module()
