package org.godotengine.godot;

import com.google.android.gms.ads.*;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

/* CHARTBOOST */
import com.chartboost.sdk.CBLocation;
import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.chartboost.sdk.Libraries.CBLogging.Level;
/**/

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.widget.FrameLayout;
import android.view.ViewGroup.LayoutParams;
import android.provider.Settings;
import android.graphics.Color;
import android.util.Log;
import java.util.Locale;
import android.view.Gravity;
import android.view.View;

public class GodotAdMob extends Godot.SingletonBase implements RewardedVideoAdListener 
{
	private Activity activity = null; // The main activity of the game
	private int instanceId = 0;

	private AdView bannerAd     = null;       // Banner view
    private AdSize bannerAdSize = AdSize.SMART_BANNER;

	private InterstitialAd interstitialAd = null; // Interstitial object

    private RewardedVideoAd rewardedVideoAd;
    private String          rewardedVideoAdUnitId;

	private boolean isReal = false; // Store if is real or not

	private FrameLayout layout = null; // Store the layout
	private FrameLayout.LayoutParams adParams = null; // Store the layout params

	/* Init
	 * ********************************************************************** */

	/**
	 * Prepare for work with AdMob
	 * @param boolean isReal Tell if the enviroment is for real or test
	 */
	public void init(final String appId, boolean isReal, int instanceId)
	{
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(activity, appId);

		this.isReal = isReal;
		this.instanceId = instanceId;
		Log.d("godot", "AdMob: init");
	}

	/* Banner
	 * ********************************************************************** */

	/**
	 * Load a banner
	 * @param String id AdMod Banner ID
	 * @param boolean isOnTop To made the banner top or bottom
	 */
	public void loadBanner(final String id, final String size, final boolean isOnTop)
	{
        if (size.equals("BANNER")) {
            bannerAdSize = AdSize.BANNER;
        } else if (size.equals("LARGE_BANNER")) {
            bannerAdSize = AdSize.LARGE_BANNER;
        } else if (size.equals("MEDIUM_RECTANGLE")) {
            bannerAdSize = AdSize.MEDIUM_RECTANGLE;
        } else if (size.equals("FULL_BANNER")) {
            bannerAdSize = AdSize.FULL_BANNER;
        } else if (size.equals("LEADERBOARD")) {
            bannerAdSize = AdSize.LEADERBOARD;
        }

		activity.runOnUiThread(new Runnable()
		{
			@Override public void run()
			{
				layout = ((Godot) activity).layout;
				adParams = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.WRAP_CONTENT
				);
				if (isOnTop) adParams.gravity = Gravity.TOP;
				else adParams.gravity = Gravity.BOTTOM;

				bannerAd = new AdView(activity);
				bannerAd.setAdUnitId(id);

				bannerAd.setBackgroundColor(Color.TRANSPARENT);

				bannerAd.setAdSize(bannerAdSize);
				bannerAd.setAdListener(new AdListener()
				{
					@Override
					public void onAdLoaded() {
						Log.w("godot", "AdMob: onAdLoaded");
						GodotLib.calldeferred(instanceId, "_on_admob_ad_loaded", new Object[]{ });
					}

					@Override
					public void onAdFailedToLoad(int errorCode)
					{
						String	str;
						switch(errorCode) {
							case AdRequest.ERROR_CODE_INTERNAL_ERROR:
								str	= "ERROR_CODE_INTERNAL_ERROR";
								break;
							case AdRequest.ERROR_CODE_INVALID_REQUEST:
								str	= "ERROR_CODE_INVALID_REQUEST";
								break;
							case AdRequest.ERROR_CODE_NETWORK_ERROR:
								str	= "ERROR_CODE_NETWORK_ERROR";
								GodotLib.calldeferred(instanceId, "_on_admob_network_error", new Object[]{ });
								break;
							case AdRequest.ERROR_CODE_NO_FILL:
								str	= "ERROR_CODE_NO_FILL";
								break;
							default:
								str	= "Code: " + errorCode;
								break;
						}
						Log.w("godot", "AdMob: onAdFailedToLoad -> " + str);
					}
				});
				layout.addView(bannerAd, adParams);

				// Request
				AdRequest.Builder adBuilder = new AdRequest.Builder();
				adBuilder.tagForChildDirectedTreatment(true);
				if (!isReal) {
					adBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
					adBuilder.addTestDevice(getAdmobDeviceId());
				}
				bannerAd.loadAd(adBuilder.build());
			}
		});
	}

	/**
	 * Show the banner
	 */
	public void showBanner()
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override public void run()
			{
				if (   bannerAd == null
                    || bannerAd.getVisibility() == View.VISIBLE) {
                    return;
                }
				bannerAd.setVisibility(View.VISIBLE);
				bannerAd.resume();
				Log.d("godot", "AdMob: Show Banner");
			}
		});
	}

	/**
	 * Resize the banner
	 *
	 */
	public void resize()
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override public void run()
			{
				layout.removeView(bannerAd); // Remove the old view

				// Extract params

				int gravity = adParams.gravity;
				FrameLayout	layout = ((Godot)activity).layout;
				adParams = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.WRAP_CONTENT
				);
				adParams.gravity = gravity;
				AdListener adListener = bannerAd.getAdListener();
				String id = bannerAd.getAdUnitId();

				// Create new view & set old params
				bannerAd = new AdView(activity);
				bannerAd.setAdUnitId(id);
				bannerAd.setBackgroundColor(Color.TRANSPARENT);
				bannerAd.setAdSize(bannerAdSize);
				bannerAd.setAdListener(adListener);

				// Add to layout and load ad
				layout.addView(bannerAd, adParams);

				// Request
				bannerAd.loadAd(getAdRequest());

				Log.d("godot", "AdMob: Banner Resized");
			}
		});
	}

	/**
	 * Hide the banner
	 */
	public void hideBanner()
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override public void run()
			{
				if (   bannerAd == null
                    || bannerAd.getVisibility() == View.GONE) {
                    return;
                }
				bannerAd.setVisibility(View.GONE);
				bannerAd.pause();
				Log.d("godot", "AdMob: Hide Banner");
			}
		});
	}

	/**
	 * Get the banner width
	 * @return int Banner width
	 */
	public int getBannerWidth()
	{
		return bannerAdSize.getWidthInPixels(activity);
	}

	/**
	 * Get the banner height
	 * @return int Banner height
	 */
	public int getBannerHeight()
	{
		return bannerAdSize.getHeightInPixels(activity);
	}

	/* Interstitial
	 * ********************************************************************** */

	/**
	 * Load a interstitial
	 * @param String id AdMod Interstitial ID
	 */
	public void loadInterstitial(final String id)
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override public void run()
			{
				interstitialAd = new InterstitialAd(activity);
				interstitialAd.setAdUnitId(id);
		        interstitialAd.setAdListener(new AdListener()
				{
					@Override
					public void onAdLoaded() {
						Log.w("godot", "AdMob: onAdLoaded");
						GodotLib.calldeferred(instanceId, "_on_interstitial_loaded", new Object[] { });
					}

					@Override
					public void onAdClosed() {
						GodotLib.calldeferred(instanceId, "_on_interstitial_close", new Object[] { });

						AdRequest.Builder adBuilder = new AdRequest.Builder();
						adBuilder.tagForChildDirectedTreatment(true);
						if (!isReal) {
							adBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
							adBuilder.addTestDevice(getAdmobDeviceId());
						}
						interstitialAd.loadAd(adBuilder.build());

						Log.w("godot", "AdMob: onAdClosed");
					}
				});

				interstitialAd.loadAd(getAdRequest());
			}
		});
	}

	/**
	 * Show the interstitial
	 */
	public void showInterstitial()
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override public void run()
			{
				if (   interstitialAd != null
                    && interstitialAd.isLoaded()) {
					interstitialAd.show();
				} else {
					Log.w("godot", "AdMob: _on_interstitial_not_loaded");
					GodotLib.calldeferred(instanceId, "_on_interstitial_not_loaded", new Object[] { });
				}
			}
		});
	}

	/* Rewarded Video
	 * ********************************************************************** */

	/**
	 * Load a rewarded video
	 * @param String id AdMod Interstitial ID
	 */
	public void loadRewardedVideo(final String adUnitId)
	{
        final RewardedVideoAdListener listener = this;

        rewardedVideoAdUnitId = adUnitId;
		activity.runOnUiThread(new Runnable()
		{
			@Override public void run()
			{
                rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(activity);
                rewardedVideoAd.setRewardedVideoAdListener(listener);
                loadRewardedVideoAd();
			}
		});
    }
	/**
	 * Show the rewarded video
	 */
	public void showRewardedVideo()
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override public void run()
			{
				if (   rewardedVideoAd != null
                    && rewardedVideoAd.isLoaded()) {
					rewardedVideoAd.show();
				} else {
					Log.w("godot", "AdMob: _on_rewardedvideoad_not_loaded");
					GodotLib.calldeferred(instanceId, "_on_rewardedvideoad_not_loaded", new Object[] { });
				}
			}
		});
    }
 
    private void loadRewardedVideoAd() {
        if (!rewardedVideoAd.isLoaded()) {
            rewardedVideoAd.loadAd(rewardedVideoAdUnitId, getAdRequest());
        }
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        Log.w("godot", "AdMob: _on_rewardedvideoad_left_application");
        GodotLib.calldeferred(instanceId, "_on_rewardedvideoad_left_application", new Object[] { });
    }

    @Override
    public void onRewardedVideoAdClosed() {
        Log.w("godot", "AdMob: _on_rewardedvideoad_closed");
        GodotLib.calldeferred(instanceId, "_on_rewardedvideoad_closed", new Object[] { });
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        Log.w("godot", "AdMob: _on_rewardedvideoad_failed_to_load");
        GodotLib.calldeferred(instanceId, "_on_rewardedvideoad_failed_to_load", new Object[] { });
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        Log.w("godot", "AdMob: _on_rewardedvideoad_loaded");
        GodotLib.calldeferred(instanceId, "_on_rewardedvideoad_loaded", new Object[] { });
    }

    @Override
    public void onRewardedVideoAdOpened() {
        Log.w("godot", "AdMob: _on_rewardedvideoad_opened");
        GodotLib.calldeferred(instanceId, "_on_rewardedvideoad_opened", new Object[] { });
    }

    @Override
    public void onRewarded(RewardItem reward) {
        Log.w("godot", "AdMob: _on_rewarded");
        GodotLib.calldeferred(instanceId, "_on_rewarded", new Object[] { reward.getType(), reward.getAmount() });
    }

    @Override
    public void onRewardedVideoStarted() {
        Log.w("godot", "AdMob: _on_rewardedvideoad_started");
        GodotLib.calldeferred(instanceId, "_on_rewardedvideoad_started", new Object[] { });
    }

	/* Utils
	 * ********************************************************************** */

	/**
	 * Generate MD5 for the deviceID
	 * @param String s The string to generate de MD5
	 * @return String The MD5 generated
	 */
	private String md5(final String s)
	{
		try {
			// Create MD5 Hash
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i=0; i<messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2) h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();
		} catch(NoSuchAlgorithmException e) {
			//Logger.logStackTrace(TAG,e);
		}
		return "";
	}

	/**
	 * Get the Device ID for AdMob
	 * @return String Device ID
	 */
	private String getAdmobDeviceId()
	{
		String android_id = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
		String deviceId = md5(android_id).toUpperCase(Locale.US);
		return deviceId;
	}

    private AdRequest getAdRequest() {
        AdRequest.Builder adBuilder = new AdRequest.Builder();
        adBuilder.tagForChildDirectedTreatment(true);
        if (!isReal) {
            adBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
            adBuilder.addTestDevice(getAdmobDeviceId());
        }
        return adBuilder.build();
    }

	/* Definitions
	 * ********************************************************************** */

	/**
	 * Initilization Singleton
	 * @param Activity The main activity
	 */
 	static public Godot.SingletonBase initialize(Activity activity)
 	{
 		return new GodotAdMob(activity);
 	}

	/**
	 * Constructor
	 * @param Activity Main activity
	 */
	public GodotAdMob(Activity p_activity) {
		registerClass("AdMob", new String[] {
			"init",
			"loadBanner", "showBanner", "hideBanner", "getBannerWidth", "getBannerHeight", "resize",
			"loadInterstitial", "showInterstitial",
			"loadRewardedVideo", "showRewardedVideo"
		});
		activity = p_activity;
        /* CHARTBOOST */
        Chartboost.onStart(activity);
        /**/
	}

    protected void onMainPause() {
        /* CHARTBOOST */
        Chartboost.onPause(activity);
        /**/
    }

    protected void onMainResume() {
        /* CHARTBOOST */
        Chartboost.onResume(activity);
        /**/
    }
   
    protected void onMainDestroy() {
        /* CHARTBOOST */
        Chartboost.onDestroy(activity);
        /**/
    }
}

