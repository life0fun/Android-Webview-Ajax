
package com.mot.ajaxwebapp;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.ConsoleMessage;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;

/**
 * FragmentWrapper must be loading asynclly, hence, must use AsyncTaskLoader to indicate progress loading.
 */
public class WebviewFragment extends FragmentWrapper implements LoaderManager.LoaderCallbacks<WebView>{
	public static final String TAG = "WV_FRAG";
	
    private static Activity mActivity;
    private boolean mDualPane;
    
    // the parent of webview, so we can cache webview without reloading between fragment swappings.
    private FrameLayout mParentFrame;   
    private WebView mWebview;
    
	private MyWebViewClient mWebviewclient;
	private MyWebChromeClient mWebChromeClient;
	private String mUrl;  // the url for this webview
	private boolean mPageLoaded = false;
	
	/**
     * Static factory to create a fragment object from tab click.
     */
    public static WebviewFragment newInstance(Activity activity, String url) {
    	WebviewFragment f = new WebviewFragment();  // constructor is morphyed into onCreate()
    	mActivity = activity;
    	
        Bundle args = new Bundle();
        args.putString("url", url);
        f.setArguments(args);
        Log.d(TAG, "newInstance " + url);
        return f;
    }
    String getUrl() { return getArguments().getString("url"); }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {  // this callback invoked after newInstance done.  
        super.onCreate(savedInstanceState);
        initWebview();   // init webview when created
        getLoaderManager().initLoader(getUrl().hashCode(), null, this);
        Log.d(TAG, "onCreate webview fragment " + mUrl);
    }

    private void initWebview() {
        mWebview = new WebView(mActivity);
        mWebviewclient = new MyWebViewClient(mWebview);
        mWebview.setWebViewClient(mWebviewclient);
        mWebview.setWebChromeClient(mWebChromeClient);
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        mWebview.getSettings().setBuiltInZoomControls(true);
        mWebview.getSettings().setEnableSmoothTransition(true);
        mWebview.getSettings().setUseWideViewPort(true);
        mWebview.clearCache(true);
        Log.d(TAG, "initWebview with url" + mUrl);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        // get the fragment arguments.
        String url = getUrl();
        if (url == null) url = "http://www.google.com";
        mUrl = url;
        Log.d(TAG, "onCreateView :" + url);
        
        // inflate webview for this fragment, just a framelayout contains a webview.s
        View v = inflater.inflate(R.layout.webview, container, false);  // no care whatever container is.
        mParentFrame = (FrameLayout)v.findViewById(R.id.webview_root);  // webview embeded inside a FrameLayout.
        //WebView wv = (WebView)v.findViewById(R.id.webview);

        if(!mPageLoaded){
        	Log.d(TAG, "onCreateView : use loader manager to loading..." + url);
	        //loadUrl(mUrl);
        }else{
            mParentFrame.removeView(mParentFrame.findViewById(R.id.webview));  // replace only the webview
            mParentFrame.addView(mWebview);
        	Log.d(TAG, "onCreateView : already loaded..." + url);
        }

        return v;
    }
    @Override public void onDestroyView(){ 
    	super.onDestroyView(); 
    	mParentFrame.removeAllViews();  // detach webview from parent, so webview can be re-attached later.
    	Log.d(TAG, "onDestroyView: " + mUrl);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {  // invoked after fragment view created.
        super.onActivityCreated(savedInstanceState);
        
        if (mDualPane) {
            //showSeasons();
        }
        setHasOptionsMenu(true);
        Log.d(TAG, "onActivityCreated webview fragment " + mUrl);
    }
    
    /**
     * can webview go back ?
     */
    public boolean goBack() {
        boolean back = false;
        if( mWebview != null && mWebview.canGoBack()){
            mWebview.goBack();
            back = true;
        }
        return back;
    }
    
	public Loader<WebView> onCreateLoader(int arg0, Bundle arg1) {
		return new WebviewLoader(getActivity(), mWebview, getUrl());
	}

	public void onLoadFinished(Loader<WebView> loader, WebView webview) {
		mParentFrame.removeAllViews();
		mParentFrame.addView(mWebview);
		Log.d(TAG, " LoaderManager : onLoadFinished :" + mUrl);
	}

	public void onLoaderReset(Loader<WebView> arg0) {
		// TODO Auto-generated method stub
	}
	
    /**
     * each fragment instance has one loader, and each loader responsible for loading one URL.
     * Loader owns the data it load, and manages the data. e.g., no worry of un-closed cursor.
     */
	static class WebviewLoader extends AsyncTaskLoader<WebView> {
		WebView wv;
		String url;
		
		public WebviewLoader(Context context) {
			super(context);
		}
		
		public WebviewLoader(Context context, WebView webview, String url) {
			super(context);
			this.url = url;
			this.wv = webview;
			Log.d(TAG, "WebviewLoader constructor..." + this.url);
		}

		@Override
		public WebView loadInBackground() {  // will be running inside executor service.
			// onLoadFinished will be called when this func ends.
			// if this func is async, should pass this func as the callback of async func.
			Log.d(TAG, "loadInBackground: " + this.url);
			return loadUrl(url);
		}
		
		@Override protected void onStartLoading() { Log.d(TAG, "onStartLoading " + this.url); forceLoad();}
		@Override public void deliverResult(WebView wv) { super.deliverResult(wv); }
		
		private WebView loadUrl(String url){
	    	Log.d(TAG, "loadUrl :" + url);
	    	if(url.indexOf("http://") >= 0){
	    		wv.loadUrl(url);
	    	}else{
	    		loadLocalPage();
	    	}
	    	return wv;
	    }
	    
	    private WebView loadLocalPage() {
	        final String mimetype = "text/html";
	        final String encoding = "UTF-8";
	        String htmldata = "<html><body>boo</body></html>";
	        // fileurl = "file://data/webapp/" + url + "/index.html";
	        // load res/raw/template.xml
	        String data = ResourceUtils.loadResToString(R.raw.sameorigin, mActivity);
	        if (data != null){
	      	  htmldata = data;
	        }
	        
	        wv.loadDataWithBaseURL("", htmldata, mimetype, encoding, "");
	        //web.loadData(htmldata, mimetype, encoding);  // this doesnot work

	        //setContentView(mWebview);
	        Log.d(TAG, "initHomeWebView: done");  
	        return wv;
	    }
	    
	    public void fetchURL(final String url, final String localname) {
	    	Runnable fetchRunnable = new Runnable() {
	    		public void run() {
	    			try{
	    				HttpClient client = new DefaultHttpClient();
	    				HttpGet getRequest = new HttpGet(url);
	    				HttpResponse response  = client.execute(getRequest);
	    				HttpEntity entity = response.getEntity();
	    				InputStream fis = entity.getContent();
	    				//FileOutputStream fos = new FileOutputStream(mImageFileName);
	    				FileOutputStream fos = mActivity.openFileOutput(localname, Activity.MODE_WORLD_WRITEABLE);
	    				byte[] readData = new byte[1024];
	    				
	    				int i = fis.read(readData);
	    				while(i != -1){
	    					fos.write(readData, 0, i);
	    					i = fis.read(readData);
	    				}
	    				
	    				fis.close();
	    				fos.close();
	    			}catch(Exception e){
	    				Log.e(TAG, "DownloadImage:" + e.toString());
	    			}
	    		}
	    	};
	    	
	    	new Thread(fetchRunnable).start();
	    }

	}
	
    private void setLoadingScreen(boolean show) {
    	if(!show){
    		mActivity.findViewById(android.R.id.empty).setVisibility(View.GONE);
    	}else{
    		mActivity.findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
    	}
    	mActivity.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_END);
    }

    @Override public void onResume() { super.onResume(); }
    @Override public void onAttach(Activity act) { super.onAttach(act); Log.d(TAG, "onAttach : " + mUrl); }
    @Override public void onDetach(){ super.onDetach(); Log.d(TAG, "onDetach:" + mUrl);}
    @Override public void onStart() { super.onStart(); Log.d(TAG, "onStart : " + mUrl); }
    @Override public void onStop(){ super.onStop(); Log.d(TAG, "onStop:" + mUrl);}
    @Override public void onDestroy(){ super.onDestroy(); Log.d(TAG, "onDestroy:" + mUrl);}
    
    @Override
    public void onCreateOptionsMenu(Menu menu, android.view.MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);
        //inflater.inflate(R.menu.overview_menu, menu);
        //menu.add(Menu.NONE, MENUITEM_SEARCH, 0, "Search").setTitle("Search").setIcon(android.R.drawable.ic_menu_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        //menu.add(Menu.NONE, MENUITEM_REFRESH, 0, "Refresh").setTitle("Refresh").setIcon(android.R.drawable.ic_popup_sync).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                //searchWeb();
            	Toast.makeText(mActivity, "Searching...", 1);
            	Log.d(TAG, "onOptionsItemSelected: Search" );
                break;
            case R.id.menu_sync:
                //refresh();
            	Toast.makeText(mActivity, "Refreshing...", 1);
            	Log.d(TAG, "onOptionsItemSelected: Refreshing..." );
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

        
    public class MyWebViewClient extends WebViewClient {
    	private WebView mView;
    	
    	MyWebViewClient(WebView webview){
    		mView = webview;
    	}
    	
    	@Override 
    	public boolean shouldOverrideUrlLoading(WebView view, String url) {
    		if (url.contains("clock")) {
   				String html = "<html><body>Date:" + new Date().toString() + "</body></html>";
    			//view.loadData(html, mimetype, encoding);
    			//Log.d(TAG, "shouldOverrideUrlLoading :" + url);
    			return true;
    		}else {
    			view.loadUrl(url);
    			Log.d(TAG, "shouldOverrideUrlLoading :" + url);
    			return true;
    		}	      
    	}
   	  
    	@Override
 	    public void onPageFinished(WebView webview, String url){
    		//webview.loadUrl("javascript:(function() { " +  "document.getElementsByTagName('body')[0].style.color = 'red'; " + "})()");
    		Log.d(TAG, "onPageFinished :" + url);
    		mPageLoaded = true;
    	}
 	  
    	@Override
    	public void onLoadResource(WebView view, String url) {
    		//Log.d(TAG, "onLoadResource :: " + url);
    	}
    	
    	@Override
    	public void onReceivedError (WebView view, int errorCode, String description, String failingUrl){
    		Log.d(TAG, "onReceivedError : " +failingUrl + "::" + description);
    	}
    	@Override
    	public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm){
    		Log.d(TAG, "onReceivedHttpAuthRequest : " +host + "::" + realm);
    	}
    	@Override
    	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
    		Log.d(TAG, "onReceivedSslError : " + error.toString());
    	}
    }
    
    public class MyWebChromeClient extends WebChromeClient {
    	@Override
    	public void onConsoleMessage(String message, int lineNumber, String sourceID) {
    	    Log.d(TAG, "onConsoleMessage: " + message);
    	}
    	@Override
    	public boolean onConsoleMessage(ConsoleMessage cm){
    		super.onConsoleMessage(cm);
    		Log.d(TAG, "onConsoleMessage: " + cm.toString());
    		return true;
    	}
    }
}
