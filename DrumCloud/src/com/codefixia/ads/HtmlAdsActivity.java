package com.codefixia.ads;


//ISSUE_ADD_APPWALL_URL_HERE
//ISSUE_CHANGE_PACKAGE_NAME
//ISSUE_SET_MINIMUM_INTERVAL_TIME


//ISSUE_WEBVIEW_WARNING_ON_ANDROID_ICS_ETC
	//
	// it seems doing loadUrl() in a separate (non-GUI) thread
	//	creates a warning ..
	//
	//	01-22 13:46:58.556: D/LeadboltAppWallActivity(18074): tryToPreLoadUrl() - loadUrl()
	//	01-22 13:46:58.561: W/webview(18074): java.lang.Throwable: Warning: A WebView method was called on thread 'Thread-1172'. All WebView methods must be called on the UI thread. Future versions of WebView may not support use on other threads.
	//
	// though can still use ..
	//
	//
	// see also:
	// http://stackoverflow.com/questions/8555749/getting-an-error-in-webview-on-ice-cream-sandwich
	//	You can call webview.loadUrl("javascript:myJavaMethod(" + itemArr + "," + telcoID + ");"); in a background thread. this can be a solution.
	//	
	//	it will warn: A webview method was called on thread xxxx All webview methods must be called on the UI thread.
	//	
	//	But it works.
	//	


//ISSUE_ENSURE_NOT_LOADURL_WHILE_ALREADY_DOING_SO

//ISSUE_ENSURE_NOTSTART_ACTIVITY_WHILE_ALREADY_STARTED




//ISSUE_SEQUENCE_OF_EVENTS_AFTER_USER_CLICKS_ON_AD_ALSO_SHOW_PROGRESS_DIALOG_AFTER_CLICK_ON_AD
	//
	// adding progress dialog .. that starts AFTER click on an ad ..
	//	since can take some time to load the different URLs ..
	//	and eventually get to market:// link that takes to Google Play ..
	//	(esp. on a GPRS connection)
	//
	//	therefore wanted to show a progress dialog ..



//ISSUE_LEADBOLTAPPWALLACTIVITY_NOT_WANT_ORIENTATION_CHANGES_TO_DESTROY_ACTIVITY
	//
	// to avoid orientation changes from causing a destroy of activity and then restart (onCreate() etc. ..)
	//	and onPause() etc.
	//
	//	(you are currently cleaning up stuff in onPause())
	//
	//
	// can simplify .. by choosing to NOT support rotation ..
	//
	//	this causes orientation changes to NOT cause activity destroy/restart etc. ..
	//
	//
	//
	// to not support rotation .. put this line in the AndroidManifest.xml file:
	//
	//	android:configChanges="keyboard|keyboardHidden|orientation|screenSize"
	//

//ISSUE_LEADBOLTAPPWALLACTIVITY_ZOOM_LEVEL_BAD_ON_HTC_DESIRE_HTC_ONE_X
	//
	// seeing that when showing on HTC devices in "portrait" mode .. the layout is very small ..
	//
	//	i.e. not fills the screen ..
	//
	//	at least with the commands that given in original source code
	//	(from David at makingmoneywithandroid.com forum ..)
	//
	//
	// it seems HTC phones have an issue with WebView layout ..
	//	i.e. supposedly they ignore the meta-tag for HTML or etc. ..
	//
	//	i.e. for the browser .. it seems to ignore the tag
	//	and thus rendering screwed up or etc. .. !
	//
	//


import com.codefixia.drumcloud.R;
import com.codefixia.utils.AndroidUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

public class HtmlAdsActivity extends Activity {

	protected static final String TAG = "LeadboltAppWallActivity";


	//----------	
	// ISSUE_ADD_APPWALL_URL_HERE
	//
	// add your AD url - as displayed by the Leadbolt webpage for the Leadbolt AppWall HTML ad format you have created ..

	public static String AD_URL = "http://ad.leadboltads.net/show_app_wall?section_id=481218852";
	private static String LOCAL_AD_URL = "file:///android_asset/codefixia_app_wall.html";
	//----------	



	//----------	
	//private static final boolean DEBUG_MESSAGES = true;
	private static final boolean DEBUG_MESSAGES = false;
	//----------	


	//----------	
	// ISSUE_SEQUENCE_OF_EVENTS_AFTER_USER_CLICKS_ON_AD_ALSO_SHOW_PROGRESS_DIALOG_AFTER_CLICK_ON_AD
	private static Activity adActivityForUseByProgressDialog = null;
	//----------	



	//----------	
	// utility methods
	//----------	
	// ISSUE_SEQUENCE_OF_EVENTS_AFTER_USER_CLICKS_ON_AD_ALSO_SHOW_PROGRESS_DIALOG_AFTER_CLICK_ON_AD
	//
	// progress dialog to show so users know that url loading is taking place
	//	this is used AFTER user clicks on an ad ..
	//
	private static ProgressDialog progress;

	private static void showProgress() {
		// ISSUE_SEQUENCE_OF_EVENTS_AFTER_USER_CLICKS_ON_AD_ALSO_SHOW_PROGRESS_DIALOG_AFTER_CLICK_ON_AD
		if (adActivityForUseByProgressDialog != null) {
			adActivityForUseByProgressDialog.runOnUiThread(new Runnable() {
				public void run() {

					// runs in the UI thread
					if (false) {
						Toast.makeText(adActivityForUseByProgressDialog,
							"showProgress()"
							, Toast.LENGTH_LONG).show();
					}
					
					//----------
					if (progress == null || progress.getContext()!=adActivityForUseByProgressDialog){
						progress = new ProgressDialog(adActivityForUseByProgressDialog);

						// ISSUE_WERE_GETTING_ERROR_ON_PROGRESS_SHOW_SO_PUT_IN_CHECK_FOR_NULL
						if (progress != null) {
							progress.setMessage("Loading...");
						}
					}
					// ISSUE_WERE_GETTING_ERROR_ON_PROGRESS_SHOW_SO_PUT_IN_CHECK_FOR_NULL
					if (progress != null) {
						progress.show();
					}
					//----------
				}
			});
		}

	}

	// for running on UI thread ..
	//
	private static void hideProgressPlain() {
		//----------
		if (progress != null && progress.isShowing()) {
			progress.dismiss();
		}
		//----------
	}

	private static void hideProgress() {
		// ISSUE_SEQUENCE_OF_EVENTS_AFTER_USER_CLICKS_ON_AD_ALSO_SHOW_PROGRESS_DIALOG_AFTER_CLICK_ON_AD
		if (adActivityForUseByProgressDialog != null) {
			adActivityForUseByProgressDialog.runOnUiThread(new Runnable() {
				public void run() {

					// runs in the UI thread
					if (false) {
						Toast.makeText(adActivityForUseByProgressDialog,
							"hideProgress()"
							, Toast.LENGTH_LONG).show();
					}

					//----------
					hideProgressPlain();
					//----------
				}
			});
		}

	}
	//----------	






	//----------	
	private static boolean doneLoadingUrl = false;
	private static View preloadedView = null;




	// allow further:
	//	tryToPreLoadUrl()
	//
	private static boolean startedLoadingUrl_flag = false;
	private static void allowPreLoadUrl() {
		// allow further:
		//	tryToPreLoadUrl()
		//
		startedLoadingUrl_flag = false;
	}
	private static void disallowPreLoadUrl() {
		// disallow further:
		//	tryToPreLoadUrl()
		//
		startedLoadingUrl_flag = true;
	}



	private static void reset() {
		doneLoadingUrl = false;
		preloadedView = null;
	}




	// Whether the first page loaded successfully
	private static boolean initialLoadFinished = false;



	// ISSUE_ENSURE_NOT_LOADURL_WHILE_ALREADY_DOING_SO
	// ASSERTION: no harm in calling tryToPreLoadUrl() multiple times ..
	//
	// making "synchronized" so not have race condition with "startedLoadingUrl_flag"
	//	as are using to not do anything if it is set etc. ..
	//
	//
	//
	// make "final" activity ..
	// so it can be used in the anonymous inner class (Runnable) ..
	//
	public static synchronized void tryToPreLoadUrl(final Activity activity) {

		//----------
		if (startedLoadingUrl_flag) {
			// then not do tryToPreLoadUrl() again

			// do nothing
			return;
		}
		// startedLoadingUrl_flag == false

		if (doneLoadingUrl) {
			// do nothing
			return;
		}
		// doneLoadingUrl == false

		if (preloadedView != null) {
			// do nothing
			return;
		}
		// preloadedView == null
	

		// startedLoadingUrl_flag == false
		// doneLoadingUrl == false
		// preloadedView == null
		//----------

		//----------
		// ISSUE_ENSURE_NOT_LOADURL_WHILE_ALREADY_DOING_SO
		// ASSERTION: no harm in calling tryToPreLoadUrl() multiple times ..
		//
		// prevent further:
		//	tryToPreLoadUrl()
		//
		disallowPreLoadUrl();
		//----------

		//----------
		Log.d(TAG, "tryToPreLoadUrl() - inflating layout");

		if (DEBUG_MESSAGES) {
			Toast.makeText(
				activity,
				"tryToPreLoadUrl() - inflating layout",
				Toast.LENGTH_SHORT).show();
		}
		//----------



		//----------
		// from GetJarMainActivity.java
		//
		LayoutInflater inflater = activity.getLayoutInflater();

		if (inflater == null) {
			// return
			// do nothing
		}

		preloadedView = inflater.inflate(R.layout.leadbolt_app_wall, null);
		//setContentView(preloadedView);

		if (preloadedView == null) {
			// return
			// do nothing
		}
		//----------


		// preloadedView != null

		//----------
		// Get Webview
		//	this is a webview in the above layout XML file ..
		//
		// set to "final" so can pass to Thread below ..
		//
		final WebView adWebView = (WebView) preloadedView.findViewById(R.id.adWebView);



		if (adWebView == null) {
			// getting null ..
			//
			//	Finds a view that was identified by the id attribute from the XML THAT WAS PROCESSED in onCreate(Bundle). 

			// return
			// do nothing
		}





		// ISSUE_LEADBOLTAPPWALLACTIVITY_ZOOM_LEVEL_BAD_ON_HTC_DESIRE_HTC_ONE_X
		// commenting out what done in original source code ..
		//	i.e. by David of makingmoneywithandroid.com forum ..
		//
		if (false) {
			//----------
			// Load data
			adWebView.getSettings().setJavaScriptEnabled(true);
			adWebView.setInitialScale(1);
			adWebView.setBackgroundColor(0);
			//----------
		}


		//----------
		// ISSUE_LEADBOLTAPPWALLACTIVITY_ZOOM_LEVEL_BAD_ON_HTC_DESIRE_HTC_ONE_X

		adWebView.getSettings().setLoadsImagesAutomatically(true);
		adWebView.getSettings().setJavaScriptEnabled(true);
		adWebView.getSettings().setBuiltInZoomControls(true);
		adWebView.setInitialScale(100);      


		// need this from above - else the screen flashes white ..
		adWebView.setBackgroundColor(0);


		// ok, this fix it ..
		//
		adWebView.getSettings().setLoadWithOverviewMode(true);
		adWebView.getSettings().setUseWideViewPort(true);
		adWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		//----------

		//----------
		adWebView.setVisibility(View.GONE);
		//----------


		// Set callback on error
		adWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, final String url) {
				// return true if want to block ..

				Log.d(TAG, "tryToPreLoadUrl() - URL: " + url);

				//----------
				if (DEBUG_MESSAGES) {
					activity.runOnUiThread(new Runnable() {
						public void run() {
							// runs in the UI thread
							Toast.makeText(activity,
								"tryToPreLoadUrl() - URL: " + url
								, Toast.LENGTH_LONG).show();
						}
					});
				}
				//----------
	
				// ISSUE_SEQUENCE_OF_EVENTS_AFTER_USER_CLICKS_ON_AD_ALSO_SHOW_PROGRESS_DIALOG_AFTER_CLICK_ON_AD
				//
				// it seems:
				//	shouldOverrideUrlLoading()
				// is called ONLY if user clicks on an ad ..
				//
				//
				// in which case all the URL redirects trigger a call to:
				//	shouldOverrideUrlLoading()
				//
				// i.e. is called for whatever the url redirects that occur ..
				// at each stage you have ability to override behavior ..
				//
				// leadbolt seems to go through a series of URLs ..
				//
				// eventually leading to the market:// url ..
				//
				//
				// at which point you handle that differently below ..
				//
				//
				// this is why original code referred to:
				//	"first page"
				//
				//	meant the first webpage etc. ..
				//

				showProgress();
				//----------



				// If this is HTTP or HTTPS, load as normal
				if (url.startsWith("http")) {
					//----------
					// Don't override
					return false;
					//----------
				} else {
					// Otherwise, this might be a market:// or mailto:// URL,
				
					//----------
					// ISSUE_SEQUENCE_OF_EVENTS_AFTER_USER_CLICKS_ON_AD_ALSO_SHOW_PROGRESS_DIALOG_AFTER_CLICK_ON_AD
					//
					// are done .. this is the final step
					// where url takes to Google Play etc. ..
					// or market:// url link ..
					//

					hideProgress();
					//----------


					// so launch another Activity that handles URLs
					try {
						//----------
						// Safely launch the intent
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
						activity.startActivity(intent);
						//----------

						//----------
						// not do finish() here
						//	since haven't done onCreate() yet
						//
						//	that will be done after url loaded etc. ..
						//
						// Once we've left this application, no need to keep the
						// ad visible
						//finish();
						//----------

						//----------
						// block
						return true;
						//----------

					} catch (Exception e) {
						// Couldn't launch intent on this device
						e.printStackTrace();
						Log.d(TAG, "tryToPreLoadUrl() - could not launch");

						// Let the user know
						//----------
						activity.runOnUiThread(new Runnable() {
							public void run() {
								// runs in the UI thread
								Toast.makeText(activity,
									"Not supported on your device."
									, Toast.LENGTH_LONG).show();
							}
						});
						//----------


						//----------
						// not do finish() here
						//	since haven't done onCreate() yet
						//
						//	that will be done after url loaded etc. ..
						//
						// If the URL's not working, just quit this ad display
						// No point annoying the user further by letting them
						// try again
						//finish();
						//----------

						//----------
						// cleanup
						reset();
						allowPreLoadUrl();
						//----------

						//----------
						// block
						return true;
						//----------
					}

				}

			}
			
			@Override
			public void onReceivedError(WebView view, final int errorCode,
					String description, String failingUrl) {

				//----------
				// ISSUE_SEQUENCE_OF_EVENTS_AFTER_USER_CLICKS_ON_AD_ALSO_SHOW_PROGRESS_DIALOG_AFTER_CLICK_ON_AD
				//
				// when error .. also going to stop showing of progress dialog ..
				//

				hideProgress();
				//----------


				Log.d(TAG, "tryToPreLoadUrl() - WebView failed to load. Error code:" + errorCode);

				//----------
				if (DEBUG_MESSAGES) {
					activity.runOnUiThread(new Runnable() {
						public void run() {
							// runs in the UI thread
							Toast.makeText(activity,
								"tryToPreLoadUrl() - WebView failed to load. Error code:" + errorCode
								, Toast.LENGTH_LONG).show();
						}
					});
				}
				//----------


				//----------
				// If this was the first page, and failed to load, we should die
				// Because there's no way the user can do anything
				if (initialLoadFinished == false) {

					//----------
					adWebView.setVisibility(View.GONE);
					//----------


					//----------
					super.onReceivedError(view, errorCode, description,
							failingUrl);
					//----------



					//----------
					// cleanup
					reset();
					allowPreLoadUrl();
					//----------

					//----------
					// not do finish() here
					//	since haven't done onCreate() yet
					//
					//	that will be done after url loaded etc. ..
					//
					// Cancel this screen
					//finish();
					//----------

				} else {
					// can still continue if get error
					//	AFTER the first page .. !?
					//
				}
				//----------
			}

			@Override
			public void onPageFinished(WebView view, String url) {


				Log.d(TAG, "tryToPreLoadUrl() - WebView onPageFinished");

				//----------
				if (DEBUG_MESSAGES) {
					activity.runOnUiThread(new Runnable() {
						public void run() {
							// runs in the UI thread
							Toast.makeText(activity,
								"tryToPreLoadUrl() - WebView onPageFinished"
								, Toast.LENGTH_LONG).show();
						}
					});
				}
				//----------


				//----------
				view.setVisibility(View.VISIBLE);
				//----------




				//----------
				// we now have the complete webview with url loaded ..
				doneLoadingUrl = true;
				//preloadedView is probably non-null since being used ..
				//	was assigned above ..
				//startedLoadingUrl_flag is probably true .. since was set so ..
				//	disallowPreLoadUrl();
				//----------


				//----------
				// We've loaded the first page
				initialLoadFinished = true;
				//----------
				
				tryToShowAds(activity);
			}
		});



		//----------
		// this part will take some time ..
		//	so offload it to a thread ..
		//
		// adWebView should be "final" ..
		//
		//
		new Thread(new Runnable() {
			public void run() {

				//----------
				if(AndroidUtil.isNetworkAvailable(activity)){
					adWebView.loadUrl(AD_URL);
				}else{
					String shortLocale=activity.getString(R.string.shortLocale);
					if(shortLocale.equalsIgnoreCase("es")){
						String htmlFile=LOCAL_AD_URL.replace(".html", "")+"_"+shortLocale+".html";
						//Log.d("FILE","Opening:"+htmlFile);
						adWebView.loadUrl(htmlFile);	
					}else{
						adWebView.loadUrl(LOCAL_AD_URL);
					}
				}
				//----------

			}
		}).start();
		//----------




		//----------
		if (DEBUG_MESSAGES) {
			Toast.makeText(
				activity,
				"tryToPreLoadUrl() - started thread to loadUrl() eventually"
				, Toast.LENGTH_SHORT).show();
		}
		//----------

		// return
		//	when webview has loaded url ..
		//	will set:
		//	doneLoadingUrl == true
		//	preloadedView != null
		//
	}
	//----------	
	public static boolean isReadyToShowAds() {
		if (doneLoadingUrl) {
			if (preloadedView != null) {
				//	doneLoadingUrl == true
				//	preloadedView != null
				return true;
			}
		}

		return false;
	}


	// ISSUE_ENSURE_NOTSTART_ACTIVITY_WHILE_ALREADY_STARTED
	//
	// used to ensure that while started:
	//	startActivity()
	//
	// that not able to make another request until:
	//	onDestroy()
	//
	//
	private static boolean startedActivity_flag = false;



	// ISSUE_ENSURE_NOTSTART_ACTIVITY_WHILE_ALREADY_STARTED
	// ASSERTION: no harm in calling tryToShowAds() multiple times ..
	//
	// making "synchronized" so not have race condition with "startedLoadingUrl_flag"
	//	as are using to not do anything if it is set etc. ..
	//
	//

	// return true - if had ads to show and tried to show them
	// return false - if didn't have ads to show and didn't try to show them
	//	and instead it started a download of the ads (for next time etc.):
	//
	//		tryToPreLoadUrl(activity);
	//
	public static synchronized boolean tryToShowAds(Activity activity) {

		// may want to avoid doing:
		//	tryToPreLoadUrl()
		//
		//	here ..
		//
		// since if someone calls and it not done ..
		//	it maybe queued again ..
		//
		if (isReadyToShowAds()) {
			// ISSUE_ENSURE_NOTSTART_ACTIVITY_WHILE_ALREADY_STARTED
			startedActivity_flag = true;


			//----------
			// only if have preloaded ads
			//

			Intent intent = new Intent(activity, HtmlAdsActivity.class);
			activity.startActivity(intent);
			//----------



			//----------
			// successfully tried to show ads ..
			//
			return true;
			//----------

		} else {
			// not ready ..

			// ISSUE_ENSURE_NOT_LOADURL_WHILE_ALREADY_DOING_SO
			// ASSERTION: no harm in calling tryToPreLoadUrl() multiple times ..
			tryToPreLoadUrl(activity);

			//----------
			// not able to show the ads ..
			return false;
			//----------

		}
	}


	// ISSUE_SET_MINIMUM_INTERVAL_TIME
	//	1800000 = 180 seconds = 3 minutes
	//
	private static final long WAIT_BETWEEN_DISPLAYAD = 0;

	
	// set it to earliest time ..
	//
	private static long lastFullScreenAdDisplayTime = 0L;

	
	// making "synchronized" so not have race condition with "startedLoadingUrl_flag"
	//	as are using to not do anything if it is set etc. ..
	//
	//

	// return true - if had ads to show
	//	AND been a long enough time that can show ads ..
	//	and so tried to show the ads ..
	//
	// return false - if didn't have ads
	//	OR not enough time elapsed since last showed ads .. so ads were not shown ..
	//
	//	in any case if there were no ads available to show
	//	it will start a download of the ads (for next time etc.):
	//
	//		tryToPreLoadUrl(activity);
	//
	public static synchronized boolean tryToShowOccasionalAds(Activity activity) {

		// may want to avoid doing:
		//	tryToPreLoadUrl()
		//
		//	here ..
		//
		// since if someone calls and it not done ..
		//	it maybe queued again ..
		//
		if (isReadyToShowAds()) {

			//----------
			long waitBetweenDisplayAd = WAIT_BETWEEN_DISPLAYAD;


			long currentTime = System.currentTimeMillis();
			if (currentTime < lastFullScreenAdDisplayTime + waitBetweenDisplayAd) {
				// still not waited long enough ..
				//
				// going to ignore this request ..
				//
				// good feedback ..

				if (false) {
					Toast.makeText(activity,
							"tryToShowOccasionalAds() - ignore as enough time not elapsed since last successful display of ad - waitBetweenDisplayAd=" + String.valueOf(waitBetweenDisplayAd)
							, Toast.LENGTH_LONG).show();
				}


				//----------
				// do nothing
				//
				// not able to show the ads ..
				return false;
				//----------

			}
			// enough time has elapsed .. can bother trying to display an ad etc. ..
			//----------
			// ISSUE_ENSURE_NOTSTART_ACTIVITY_WHILE_ALREADY_STARTED
			startedActivity_flag = true;


			//----------
			// only if have preloaded ads
			//

			Intent intent = new Intent(activity, HtmlAdsActivity.class);
			activity.startActivity(intent);
			//----------

			//----------
			// since have started displaying ad ..
			//	remember when was last time did so ..
			//
			//lastFullScreenAdDisplayTime = System.currentTimeMillis();
			lastFullScreenAdDisplayTime = currentTime;
			//----------
			
			//----------
			// successfully tried to show ads ..
			//
			return true;
			//----------


		} else {
			// not ready ..

			// ISSUE_ENSURE_NOT_LOADURL_WHILE_ALREADY_DOING_SO
			// ASSERTION: no harm in calling tryToPreLoadUrl() multiple times ..
			tryToPreLoadUrl(activity);

			//----------
			// not able to show the ads ..
			return false;
			//----------
		}
	}

	//----------	










	//----------	
	// Usage:
	//	LeadboltAppWallActivity.tryToPreLoadUrl(YourActivity.this);
	//	...
	//	if (LeadboltAppWallActivity.isReadyToShowAds()) {
	//	}
	//
	// Called when the activity is first created.
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//----------
		//setContentView(R.layout.leadbolt_app_wall);
		if (isReadyToShowAds()) {
			setContentView(preloadedView);

			//----------
			// ISSUE_SEQUENCE_OF_EVENTS_AFTER_USER_CLICKS_ON_AD_ALSO_SHOW_PROGRESS_DIALOG_AFTER_CLICK_ON_AD
			adActivityForUseByProgressDialog = HtmlAdsActivity.this;
			//----------



			//----------
			// have consumed in onCreate():
			//	preloadedView
			//
			// allow further:
			//	tryToPreLoadUrl()
			//

			// cleanup
			reset();
			allowPreLoadUrl();
			//----------


		} else {
			// not done .. should not have been called
			// in the first place
			//

			//----------
			// cleanup
			cleanup();
			//----------

		}
		//----------

	}
	//----------
	@Override
	protected void onResume() {
		super.onResume();

	}
	//----------
	private void cleanup() {

		//----------
		// cleanup
		reset();
		allowPreLoadUrl();
		//----------


		//----------
		WebView temp_adWebView = (WebView) findViewById(R.id.adWebView);
		if (temp_adWebView != null) {

			//----------
			temp_adWebView.setVisibility(View.GONE);
			//----------
			removeWebview();

		}
		//----------


		//----------
		// ISSUE_SEQUENCE_OF_EVENTS_AFTER_USER_CLICKS_ON_AD_ALSO_SHOW_PROGRESS_DIALOG_AFTER_CLICK_ON_AD
		hideProgressPlain();

		// remove references
		//
		adActivityForUseByProgressDialog = null;
		//----------


		//----------
		// stop this activity
		finish();
		//----------

	}
	
	// onPause() will destroy ..
	//
	//	i.e. want to keep it ephemeral ..
	//
	//	this way if user shifts away ..
	//
	//	it removed from the top of the activity stack etc. ..
	//
	@Override
	protected void onPause() {
		super.onPause();

		//----------
		// cleanup
		cleanup();
		//----------
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
	//----------
	// this was called on the activity:
	//	finish();
	//
	// or activity closed by system ..
	//
	// ISSUE_TRYTOBUYPRODUCT_USE_FINISH_TO_ENSURE_ONDESTROY_CALLED_SO_CAN_RESET_FLAG
	
	private void removeWebview(){
		WebView temp_adWebView = (WebView) findViewById(R.id.adWebView);
		if(temp_adWebView!=null){
			LinearLayout mainLayout = (LinearLayout) findViewById(R.id.mainAdsLayout);
			if(mainLayout!=null){
				mainLayout.removeView(temp_adWebView);
			}			
			temp_adWebView.removeAllViews();
			temp_adWebView.destroy();
		}		
	}
	
	protected void onDestroy() {
		//progress.dismiss();
		removeWebview();
		super.onDestroy();

		// remove references

		// are already doing much of the cleanup in:
		//	onResume()
		//

		// ISSUE_ENSURE_NOTSTART_ACTIVITY_WHILE_ALREADY_STARTED
		// allow this to be done again:
		//	tryToShowAds();
		//
		startedActivity_flag = false;

	}
	//----------
	// ISSUE_LEADBOLTAPPWALLACTIVITY_NOT_WANT_ORIENTATION_CHANGES_TO_DESTROY_ACTIVITY
	// Must place this into the AndroidManifest.xml file for this activity in order for this to work properly 
	//   android:configChanges="keyboardHidden|orientation"
	//   optionally 
	//   android:screenOrientation="landscape"
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	//----------

}	