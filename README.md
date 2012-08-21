# A project to demo Android Ajax fetch with client templating with webview.

## Architecture

1. web html with client templating stored in res/raw/*.xml
2. when sarted, read xml html content and create a web page for that.
3. inside html, issue $.ajax() to foursquare to retrieve the location
4. json object returned, display the json.

## note that cross-site refreence is ok here.

# Add ActionBarSherlock lib project

1. mkdir actionbarsherlock;cd actionbarsherlock;
2. cp -R ~/workspace/ActionBarSherlock/library/* .
3. echo 'android.library.reference.1=actionbarsherlock' >> project.properties
4. ant debug
5. Need to add android:theme="@style/Theme.Sherlock" to Activity

# Android ActionBar

1. <uses-sdk android:minSdkVersion="4" android:targetSdkVersion="11" />
2. disable by <activity android:theme="@android:style/Theme.Holo.NoActionBar">

## Adding Action Items

3. When the activity first starts, the system populates the action bar and overflow menu by calling 
	   onCreateOptionsMenu() for your activity.

	   public boolean onCreateOptionsMenu(Menu menu) {
		 // action bar items
	     MenuInflater inflater = getMenuInflater();
		 inflater.inflate(R.menu.main_activity, menu);  // <menu> <item andriod:id=save/> </menu>
		 return true;
		 // or
		 menu.add("Save").setTitle().setIcon(R.drawable.ic_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		 // submenu
		 SubMenu subMenu1 = menu.addSubMenu("Action Item");
		 subMenu1.add("Sample");
		 subMenu1.add("Menu");
		 subMenu1.add("Items");
		 subMenu1.getItem().setIcon(R.drawable.ic_title).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		 // Action view
		 SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
	   }

	4. Split ActionBar. <activity android:uiOptions="splitActionBarWhenNarrow"> />

## App Icon for Navigation, go to App home page, or Navigate up the app's hierarchy

	ActionBar actionBar = getActionBar();
	actionBar.setDisplayHomeAsUpEnabled(true);
	public boolean onOptionsItemSelected(MenuItem item) {
	  switch (item.getItemId()) {
		case android.R.id.home:
		 // app icon in action bar clicked; go home
		 Intent intent = new Intent(this, HomeActivity.class);
		 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		 startActivity(intent);
		 return true;
		default:
		 return super.onOptionsItemSelected(item);
	}

## ActionBar.Tab == Fragment. ActionBar.TabListener.onTabSelected(Tab, FragmentTransaction)
   Each Fragment is a template view.

1. main activity adds ActionBar.Tab, each fragment under each tab. 
2. switch between tabs ActionBar.TabListener.onTabSelected(Tab, FragmentTransaction) 
3. Tab -> LinearLayout -> FrameLayout -> WebView Fragment. 
4. Be sure the ViewGroup has a resource ID so you can reference it from your tab-swapping code. 
5. if the tab content will fill the activity layout, then no need a layout and setContentView at all. 
6. place each fragment in the default root ViewGroup, which is android.R.id.content.

	public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
		List<Fragment> mfragments;
		Activity mActivity;
		Class<T> mClass;

		public void onTabSelected(Tab tab, FragmentTransaction ft) { <-- ft here
		  // Check if the fragment is already initialized
		  if (mFragment == null) {
		    mFragment = Fragment.instantiate(mActivity, mClass.getName());
			ft.add(android.R.id.content, mFragment, mTag);
		  } else {
		    // If it exists, simply attach it in order to show it
			ft.attach(mFragment); 
		  }
	   }
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	  if (mFragment != null) {
	    // Detach the fragment, because another one is being attached
		ft.detach(mFragment);
	  }
	}

## Fragment = View template <==  AsyncTaskLoader

0. Fragement embedded in an Activity. use getActivity() to get the act it associated with.
1. Every fragment must have an empty constructor.
2. Argument is supplied with setArguments(Bundle)/getArguments()
3. getArguments() called inside onCreateView() when creating the view of fragment.
---------------------------------------------------------------------------
4. Fragment is a View template with its own adapter to source data to view thru LoaderManager.
---------------------------------------------------------------------------
4. Framgent view layout in a FrameLayout contains detailed view with content, i.e., webview, etc.
4. stitch the view of real content to FrameLayout container, main activity shows Frag's FrameLayout. 
5. Fragment content must be populated by Loader, AsyncTaskLoader. The two come together.
6. Fragment's activity onCreate(), initLoader; when loader created, start AsyncTaskLoader.

## populate (list)fragment content with loader.
## Loader, LoaderManager, AsyncTaskLoader, CursorLoader, and endless list
## CursorLoader, 

  class WebviewFragment extends Fragment implements LoaderManager.LoaderCallbacks<WebView>{
	1. In a ListFragment, when the fragement activity is created, initLoader.
	  AppListAdapter mAdapter;  // pass ref to activity's adapter to fragment.
	  void onActivityCreated(){ // fragment part of activity, glue to activity with this callback.
		  getLoaderManager().initLoader(0, null, this);  // impl LoaderManager.LoaderCallbacks IF
	  }
	2. when loader created, start the AsyncTaskLoader to load list.
	   public Loader<List<AppEntry>> onCreateLoader(int id, Bundle args) {
		   return new AppListLoader(getActivity());
	   }
	   public Loader<Cursor> onCreateLoader(int, Bundle){
		   return new CursorLoader();
	   }

	3. public class AppListLoader extends AsyncTaskLoader<List<AppEntry>> {
		// get the result into a local list data structure, the same as AsyncTask
		List<AppEntry> entries = new ArrayList<AppEntry>();
		@Override public List<AppEntry> loadInBackground() {
			for each item, fetch data, load images, return new ArrayList<AppEntry>();
		}
	}
	4. Inside Fragment, upon load finish, set the new data to its Adapter.
	   void onLoadFinished(Loader<List<AppEntry>> loader, List<AppEntry> data) {
		   mAdapter.setData(data); // or notifyDataSetChange();
	   }
	   or stitch the real data content view to Fragment activity's FrameLayout view.
			mParentFrame.removeAllViews();
			mParentFrame.addView(mWebview);

	5. Inside Adapter, adapt the new data to view with getView() on each entry.
	    class AppListAdapter extends ArrayAdapter<AppEntry> {
			void setData(List<AppEntry> data){ addAll(data); }
			View getView(int position, View convertView, ViewGroup parent) {
				AppEntry item = getItem(position);
				((ImageView)view.findViewById(R.id.icon)).setImageDrawable(item.getIcon());
			}
		}


## GoogleAnalytics to track event, pageview, ecommerce, etc.
  public void traceEvent(category, action, label, value){
	// getStatus() == AsyncTask.Status.RUNNING;
    new AsyncTask<Void, Void, Void>() {
		@Override
		protected void doInBackground(Void... voids){
		}
	}.execute();
  }

###################### ###################### ###################### ###################### 
Android command line
###################### ###################### ###################### ###################### 

## android ant command line tool
    0. android list target
	1. android project update -n reddit -p .
    2. android update project --target 14 -n WifiDirect -p .

## local.properties points to SDK.
	sdk.dir=/Volumes/Build/android-sdk

## ant.properties defines customized build, i.e., certificate, sign.
	certificate.path=${sdk.dir}/add-ons/mmi_apps_sdk/certs
	app.package=com.motorola.contextual.smartrules
	build.sysclasspath=last
	signjar=${sdk.dir}/add-ons/mmi_apps_sdk/tools/signapk.jar
	#app.icon=ic_launcher_msg
	certificate.class=common
	#extensible.libs.classpath=${sdk.dir}/add-ons/mmi_apps_sdk/libs
	anttasks.dir=./anttasks

    key.store=/Users/e51141/.android/release.keystore
    key.alias=releasekey

## project properties set the project target and lib dependency.
	target=Google Inc.:Google APIs:15
	jar.libs.dir=./libs
	android.library.reference.1=../Facebook

## External jar files 
  Put your dependence jar files under libs/ folder.
  All the external libs under ./libs folder are automatically collected 

  If you want customize, put the following line into project.properties, or ant.properties.

	jar.libs.dir=./libs

## Dependency APK file
  The trick for me was to add this to my default.properties files.

  android.library.reference.1=../Facebook

  Where ../Facebook contains AndroidManifest.xml, etc. 
  The real key being the relative path. Don't use an absolute path because Ant seems to treat your project directory as the root.


## The excessive use of AsyncTask to load content from web asyncly.
   AsyncTask is a runnable in platform executorService.

* new AsyncTask(){}.execute() submit the task to executor service to run.
* The real work in Task is defined inside doInBackground(Type... args)
* For Fragment Loader IF AsyncTaskLoader<WebView>, it is inside loadInBackground()
* AsyncTaskLoader IF post loading callback is onStartLoading(), onLoadFinish().
* Normally, an AsyncTask is embedded in Activity, so onPostExecute(), it can 
* Adapter.List<Thing>.addAll() and then Adapter.notifyDataSetChanged() or resetUI().
* AsynTask, onPreExecute(), progress bar. mActivity.enableLoadingScreen(){ setVisibility();}

    new AsyncTask<Void, Void, Void>() {  // new IF(){} create anonymous clz object, args must be final!
		@Override
		protected void doInBackground(Void... voids){
		}
	}.execute();  // submit to executor service to run.

   if not embeded, pass the Activity to the AsyncTask.
	   public task(Activity activity) { this.mActivity = activity; }
	   mActivity.mCommentsList.addAll(mDeferredAppendList);
	   mActivity.mCommentsAdapter.notifyDataSetChanged();

   or
       mActivity.runOnUiThread(new Runnable() { public void run() {
		 mActivity.resetUI(mActivity.mCommentsAdapter); }

###################### ###################### ###################### ###################### 
 Url xml reader or JSON reader
###################### ###################### ###################### ###################### 
1. if REST service gives json, read it using ObjectMap.
   If using http.client.request, it is sync call, need to put it inside an async task.
	ObjectMap takes input a stream and outputs an array of JSON, with json schema as Class obj.

    Listing[] listings = ObjectMap.readValue(inputstream, Listing[].class);

	// the real task(sync call) inside an AsyncTask.
	doInBackground(Integer... maxComments) {
		String url = ("www.reddit.com/r/comments/id/z/.json?");
		//HttpGet request = new HttpGet(url); HttpResponse response = mClient.execute(request); entity = response.getEntity();
		in = mClient.execute(new HttpGet(url)).getEntity().getContent();
		in = entity.getContent();

		ProgressInputStream pin = new ProgressInputStream(in, mContentLength);
		pin.addPropertyChangeListener(this);
		parseCommentsJSON(pin);
	}

	parseCommentsJSON(InputStream in){
		mObjectMapper = new ObjectMapper(); // org.codehaus.jackson.map.ObjectMapper.ObjectMapper()
		Listing[] listings = mObjectMapper.readValue(in, Listing[].class);

		ListingData threadListingData = listings[0].getData();
		ThingListing threadThingListing = threadListingData.getChildren()[0];

		ListingData commentListingData = listings[1].getData();
		for (ThingListing commentThingListing : commentListingData.getChildren()) {
			insertedCommentIndex = insertNestedComment(commentThingListing, 0, insertedCommentIndex + 1);
		}
	}

	public class Listing {
		private String kind;
		private ListingData data;
	}
	public class ListingData {
		private ThingListing[] children;
		private String after;
		private String before;
		private String modhash;
	}
	public class ThingListing {
		private String kind;
		private ThingInfo data;
	}

2. if REST xml, read use Xml.parse(in, UTF_8, contentHandler) with sax.ContentHandler.
   The key is setEndElementListener for each sub element to RootElement.getChild();

	xml.parse(mClient.execute(new HttpGet(url)).getEntity().getContent(), UTF_8, new RootElement("Data").getContentHandler());
    xml.parse(new URL("get.php?Series=xxx").getConnection().getInputStream(), UTF_8, root.getContentHandler());

	List<Result> searchShow(String title, Context context) throws IOException, SAXException {
		List<SearchResult> series = new ArrayList<SearchResult>();
		SearchResult currentShow = new SearchResult();

		RootElement root = new RootElement("Data");
		Element item = root.getChild("Series");
		// set handlers(EndElementListener) for elements we interested.
		item.setEndElementListener(new EndElementListener() { series.add(current.copy();}
		item.getChild("id").setEndTextElementListener(new EndTextElementListener() { cur.id = body;}

		String url = xmlMirror + Constants.API_KEY + "/series/" + seriesid + "/all/";
		mClient.execute(new HttpGet(url), httpClient, root.getContentHandler(), true);
	}

	void execute(HttpUriRequest request, HttpClient httpClient, ContentHandler handler){
		final HttpResponse resp = httpClient.execute(request);
		final InputStream input = resp.getEntity().getContent();
		Xml.parse(input, Xml.Encoding.UTF_8, handler);
	}


