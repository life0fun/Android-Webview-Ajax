package com.mot.ajaxwebapp;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class JsonLocFragment extends FragmentWrapper implements LoaderManager.LoaderCallbacks<List<String>> {

    public static final String TAG = "WV_JsonFrag";
    
    private static final String FSQ_URL = "https://api.foursquare.com/v2/venues/search?ll=42.288,-88.000&client_id=TI05WPK23W2P3YZGGU1YCJMAGJSSU1PAJQH4ZE3TDSN4221V&client_secret=0FDPA4TBAM3TY1VGWI5KRKDIBI4XGPHKQBOWWE3AIWVJGW3H";
    private static final int JSON_LOADER = 1;  // int to diff between different loaders. pass to initLoader/restartLoader

    private static Activity mActivity;
    private LocationsAdapter mAdapter;
    private List<String> mLocations;
    
    private boolean mDualPane;
    
    // the parent of webview, so we can cache webview without reloading between fragment swappings.
    private FrameLayout mParentFrame;   
    private String mUrl;  // the url for this webview
    private boolean mPageLoaded = false;
    
    /**
     * Static factory to create a fragment object from tab click.
     */
    public static JsonLocFragment newInstance(Activity activity, String url) {
        JsonLocFragment f = new JsonLocFragment();
        mActivity = activity;  // get hold of the activity context this fragment attached to.
        
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

        // get the fragment arguments.
        String url = getUrl();
        if (url == null) url = FSQ_URL;
        mUrl = url;
        
        mLocations = new ArrayList<String>();
        mAdapter = new LocationsAdapter(getActivity(), R.layout.row_loc, mLocations);
        setListAdapter(mAdapter);
        
        getLoaderManager().initLoader(JSON_LOADER, null, this);
        Log.d(TAG, "onCreate Json fragment : setup adapter and init loader : " + mUrl);
    }

    /**
     * for activity, setContentView, for fragment, inflate view in onCreateView.
     * no matter your fragment is declared in main activity layout, or dynamically added thru fragment transaction
     * You need to inflate fragment view inside this function. 
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        
        // inflate the view for this fragment, 
        View v = inflater.inflate(R.layout.loc_list, container, false);  // no care whatever container is.
        mParentFrame = (FrameLayout)v.findViewById(R.id.webviewfrag);    // embeded inside a FrameLayout.
        
        if(!mPageLoaded){
            Log.d(TAG, "onCreateView : loader start loading..." + mUrl);
        }else{
            //mParentFrame.removeAllViews();   // remove all children views so we can attach.
            //mParentFrame.addView(mWebview);
            Log.d(TAG, "onCreateView : loader already loaded..." + mUrl);
        }
        return v;
    }
    
    @Override public void onDestroyView(){ 
        super.onDestroyView(); 
        //mParentFrame.removeAllViews();  // detach webview from parent, so webview can be re-attached later.
        Log.d(TAG, "onDestroyView: " + mUrl);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {  // invoked after fragment view created.
        super.onActivityCreated(savedInstanceState);
        
        setHasOptionsMenu(true);
        Log.d(TAG, "onActivityCreated Json fragment :  " + mUrl);
    }
    
    /**
     * json fragment cannot go back
     */
    public boolean goBack() {
        boolean back = false;
        return back;
    }
    
    /**
     * reset UI with the new data just loaded
     */
    public void resetUI( List<String> data ){
        // you can create a new adapter with the new source,
        //mLocations = data;  // change data source for the adapter, wont work.
        //mAdapter = new LocationsAdapter(getActivity(), R.layout.row_loc, mLocations);
        //setListAdapter(mAdapter);
        
        // or deep copy the new data to old adapter.
        for(String s : data){
            mLocations.add(s);
        }
        mAdapter.notifyDataSetChanged();
    }
    
    /**
     * callback when loaded created
     */
    public Loader<List<String>> onCreateLoader(int arg0, Bundle arg1) {
        Log.d(TAG, "onCreateLoader :  " + mUrl);
        return new LocationsLoader(getActivity(), "libertyville, IL", mUrl);
    }

    /**
     * callback when loading done, set the data for adapter.
     */
    public void onLoadFinished(Loader<List<String>> loader, List<String> data) {
        resetUI(data);
        mPageLoaded = true;
        Log.d(TAG, " LoaderManager : onLoadFinished : set adapter data : " + data.get(0));
    }
    
    public void onLoaderReset(Loader<List<String>> arg0) { } 
    
    /**
     * each fragment instance has one loader, and each loader responsible for loading one URL.
     * Loader owns the data it load, and manages the data. e.g., no worry of un-closed cursor.
     * Loader return a list of string as result.
     */
    static class LocationsLoader extends AsyncTaskLoader<List<String>> {
        String mCurLoc;
        String mUrl;
        List<String> mLocList = new ArrayList<String>();
        
        public LocationsLoader( Context context ){
            super(context);
        }
        
        /**
         * create a loader to load the given url resource.
         */
        public LocationsLoader(Context context, String curloc, String url) {
            super(context);
            this.mCurLoc = curloc;
            this.mUrl = url;
        }
       
        @Override
        public List<String> loadInBackground() {  // will be running inside executor service.
            // onLoadFinished will be called when this func ends.
            // if this func is async, should pass this func as the callback of async func.
            Log.d(TAG, "LocationsLoader : loadInBackground: " + this.mUrl);
            return loadLocations(mUrl);
        }
        
        @Override protected void onStartLoading() { Log.d(TAG, "onStartLoading " + this.mUrl); forceLoad();}
        @Override public void deliverResult(List<String> mLocList ) { super.deliverResult(mLocList); }
        
        /**
         * retrieve location info from fsq url
         */
        private List<String> loadLocations( String url ){
            Log.d(TAG, "loadLocations :" + url);
            JsonElement locjson = null;
            final HttpClient client = new DefaultHttpClient();
            final HttpGet getRequest = new HttpGet(url);
            
            try {
                HttpResponse response = client.execute(getRequest);
                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    Log.w(TAG, "Error " + statusCode + " while retrieving : " + url);
                    return null;
                }

                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream inputStream = null;
                    try {
                        inputStream = entity.getContent();
                        //String locstr = BaseActivity.convertStreamToString(inputStream);
                        //Log.d(TAG, "LocationsLoader : " + locstr);
                        locjson = BaseActivity.unmarshall(inputStream);
                        parseFsqJson(locjson.getAsJsonObject(), this.mLocList);
                    }finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        entity.consumeContent();
                    }
                }
            }catch (IOException e) {
                getRequest.abort();
                Log.e(TAG, "I/O error while retrieving : " + url, e);
            } catch (IllegalStateException e) {
                getRequest.abort();
                Log.e(TAG, "Incorrect URL: " + url);
            } catch (Exception e) {
                getRequest.abort();
                Log.e(TAG, "Error while retrieving : " + url, e);
            }
            return mLocList;
        }
        
        /**
         * parse the REST json object from fsq v2/venues/explore? end point.
         * resultjson.response.groups[0].items;  group 0 is recommended. 
         * reddit uses ObjectMap to map a json stream into json object with schema class.
         */
        public void parseFsqJson(JsonObject locjson, List<String> result){
            result.clear();
            JsonObject resp = locjson.getAsJsonObject("response");
            JsonObject items = (JsonObject)resp.getAsJsonArray("groups").get(0);
            JsonArray locs = items.getAsJsonArray("items");
            
            for(int i=0;i<locs.size();i++){
                JsonObject loc = locs.get(i).getAsJsonObject();
                Log.d(TAG, "parseFsqJson: " + loc.get("name").getAsString());
                result.add(loc.get("name").getAsString());
            }
        }
    }
    
    /**
     * use array adpater to work with location list view.
     * Adapter just convert data to view. Always dep inj data source to adapter.
     * DepInj data source into adapter, wihtout adapter itself hold the data for clean code.
     */
    private static class LocationsAdapter extends ArrayAdapter<String> {
        private Context mContext;
        
        /**
         * depinj data into adapter, always depinj data source for adapter.
         */
        public LocationsAdapter( Context ctx, int rowTxtVId, List<String> data ){
            super(ctx, 0, data);   // passing 0, or the real row layout id, does not matter. getView will adjust it.
            mContext = ctx;
        }
        
        /**
         * activity says, "adapter, give me a view to display, at position "
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            View v = convertView;
            if (convertView == null) {
                convertView = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.row_loc, null);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.loc_name);
                holder.details = (TextView) convertView.findViewById(R.id.loc_details);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            
            String curloc = this.getItem(position);
            if(curloc != null){
                holder.name.setText(curloc);   // extract attr from view hold and set it to view.
            }
            
            Log.d(TAG, "Adapter getView : " + position + " : " +  curloc);
            return convertView;
        }
        
        static class ViewHolder {
            TextView name;
            TextView details;
        }
    }
}
