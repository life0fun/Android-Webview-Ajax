package com.mot.ajaxwebapp;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

public class MainActivity extends Activity implements ActionBar.TabListener {
	
	private static final String TAG = "WV_Act";
	
	private static final String Reddittab = "Reddit";
	private static final String Gigaomtab = "GigaOM";
	private static final String Ajaxtab = "Ajax";
	private static final String Locationtab = "Locations";
	
	private static final String mImageFileName = "downloaded_img.png";
	final String imgsrc = "http://developerlife.com/theblog/wp-content/uploads/2007/11/news-thumb.png";
	
    private CountDownLatch latch;
	private EditText mLocalurl; // = new EditText(this);
	public static String localurl;
	
	FragmentWrapper mRedditFrag = null;
	FragmentWrapper mGigaOMFrag = null;
	FragmentWrapper mAjaxFrag = null;
	FragmentWrapper mLocFrag = null;
	FragmentWrapper mCurFrag = null;
	List<FragmentWrapper> mFrags;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_fragment);

        //The following two options trigger the collapsing of the main action bar view.
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        
        actionBar.addTab(actionBar.newTab().setText(Reddittab).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(Gigaomtab).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(Ajaxtab).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(Locationtab).setTabListener(this));
        
        Log.d(TAG, "onCreate done");        
    }
    
    @Override public void onResume() { super.onResume(); }
    public void onTabReselected(Tab arg0, FragmentTransaction arg1) { }
	
	
    /**
     * Your layout must include a ViewGroup in which you place each FragmentWrapper associated with a tab. 
     * Be sure the ViewGroup has a resource ID so you can reference it from your tab-swapping code. 
     * Alternatively, if the tab content will fill the activity layout (excluding the action bar), 
     * then your activity doesn't need a layout at all (you don't even need to call setContentView()). 
     * Instead, you can place each fragment in the default root ViewGroup, which you can refer to with the android.R.id.content.
     */
	//@Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
    	Log.d(TAG, "onTabSelected  " + tab.getText());
    	
    	if(tab.getText().equals(Reddittab)){
    		if(mRedditFrag == null){
    			mRedditFrag = WebviewFragment.newInstance(this, "http://www.reddit.com/");
    			ft.add(R.id.webviewfrag, mRedditFrag, "tag_reddit");
    			//ft.add(android.R.id.content, mRedditFrag, "tag_reddit");
    			Log.d(TAG, "onTabSelected: add reddit");
    		}else{
    			ft.attach(mRedditFrag);
    			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    			Log.d(TAG, "onTabSelected: replace with reddit");
    		}
    		setCurFrag(mRedditFrag);
    	}else if(tab.getText().equals(Gigaomtab)){
    		if(mGigaOMFrag == null){
    			mGigaOMFrag = WebviewFragment.newInstance(this, "http://www.gigaom.com/");
    			ft.add(R.id.webviewfrag, mGigaOMFrag, "tag_reddit");
    			//ft.add(android.R.id.content, mGigaOMFrag, "tag_gigaom");
    			Log.d(TAG, "onTabSelected: add gigaom");
    		}else{
    			ft.attach(mGigaOMFrag);
    			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    			Log.d(TAG, "onTabSelected: replace with gigaom");
    		}
    		setCurFrag(mGigaOMFrag);
    	}else if( tab.getText().equals(Ajaxtab)){
    		if(mAjaxFrag == null){
    			mAjaxFrag = WebviewFragment.newInstance(this, "file://www.ajax.com/");
    			ft.add(R.id.webviewfrag, mAjaxFrag, "tag_reddit");
    			//ft.add(android.R.id.content, mAjaxFrag, "tag_ajax");
    			Log.d(TAG, "onTabSelected: add with ajax");
    		}else{
    			ft.attach(mAjaxFrag);
    			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    			Log.d(TAG, "onTabSelected: replace with ajax");
    		}
    		setCurFrag(mAjaxFrag);
    	}else if( tab.getText().equals(Locationtab)){
    	    if( mLocFrag == null ){
    	        mLocFrag = JsonLocFragment.newInstance(this, null);
    	        ft.add(R.id.webviewfrag, mLocFrag, "tag_locjson");
    	        Log.d(TAG, "onTabSelected: add location json frag");
    	    }else{
                ft.attach(mLocFrag);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                Log.d(TAG, "onTabSelected: replace with mLocFrag");
            }
            //setCurFrag(mLocFrag);
    	}
    }
	
	//@Override 
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		Log.d(TAG, "onTabUnselected " + tab.getTag());
		ft.detach(getCurFrag());  // detach the current page
	}

	private void setCurFrag(FragmentWrapper f) { mCurFrag = f; }
	private Fragment getCurFrag() { return mCurFrag; }
	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.overview_menu, menu);
        return true;
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Log.d(TAG, "onOptionsItemSelected " + item.getItemId());
    	switch ( item.getItemId() ) {
        case R.id.menu_togglemark:
        		//latch = new CountDownLatch(1);
                getURLToLoad();
                break;
        }
    	return false;  // return false, means event not handled here, propagate to overlay fragments to handle.
    }
    
    public void getURLToLoad() {
    	new AlertDialog.Builder(MainActivity.this).setTitle("Input HTML file").setMessage("Input Local URL").
    	        setView(mLocalurl).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    	        	public void onClick(DialogInterface dialog, int whichButton) {
    	        		Editable value = mLocalurl.getText(); 
    	        		MainActivity.localurl = new String(value.toString());
    	        		//loadLocalURL(MainActivity.localurl);
    	        	}
    			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int whichButton) {
    					// Do nothing.
    				}
    			}).show();
    }
    
    /**
     * override onBackPressed so press back wont cause app to exit
     * if current fragment is webview, ask whether webview can go back, if not, call the super.
     */
    @Override
    public void onBackPressed() {
        boolean goback = false;
        goback = mCurFrag.goBack();
        if( !goback ){
            super.onBackPressed();
        }
    }
}
