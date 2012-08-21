
package com.mot.ajaxwebapp;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * load details fragment using AsyncTaskLoader
 */
public class DetailsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EPISODE_LOADER = 3;

    private static final String TAG = "EpisodeDetails";

    private SimpleCursorAdapter mAdapter;


    public static DetailsFragment newInstance(String episodeId, boolean isShowingPoster) {
        DetailsFragment f = new DetailsFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("id", episodeId);
        args.putBoolean("showposter", isShowingPoster);
        f.setArguments(args);

        return f;
    }

    public void fireTrackerEvent(String label) {
        AnalyticsUtils.getInstance(getActivity()).trackEvent(TAG, "Click", label, 0);
        Log.d(TAG, "fireTrackerEvent " + label);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
         * never use this here (on config change the view needed before removing
         * the fragment)
         */
        // if (container == null) {
        // return null;
        // }
        return inflater.inflate(R.layout.episodedetails_fragment, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // AnalyticsUtils.getInstance(getActivity()).trackPageView("/EpisodeDetails");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // mImageCache = ImageCache.getInstance(getActivity());

        setupAdapter();
        
        // init load, start loading inside onCreateLoader LoaderManager.LoaderCallbacks 
        getLoaderManager().initLoader(EPISODE_LOADER, null, this);

        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, android.view.MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);
        //inflater.inflate(R.menu.overview_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_togglemark:
                fireTrackerEvent("Toggle watched");
                //onToggleWatchState();
                break;
            case R.id.menu_share: {
                fireTrackerEvent("Share episode");

                final Cursor episode = (Cursor) getListAdapter().getItem(0);
                if (episode == null) {
                    break;
                }
                episode.moveToFirst();
                Bundle shareData = new Bundle();

                String episodestring = ShareUtils.onCreateShareString(getActivity(), episode);
                String sharestring = "";
                sharestring += episode.getString(EpisodeDetailsQuery.TITLE);
                sharestring += " - " + episodestring + "\" via @SeriesGuide";
                shareData.putString("EPISODESTRING", episodestring);
                ShareUtils.showShareDialog(getFragmentManager(), shareData);
                break;
            }
            case R.id.menu_sync: {
                fireTrackerEvent("Add to calendar");

                final Cursor episode = (Cursor) getListAdapter().getItem(0);
                episode.moveToFirst();
                final String showTitle = episode.getString(EpisodeDetailsQuery.TITLE);
                final String airDate = episode.getString(EpisodeDetailsQuery.TITLE);
                final long airsTime = episode.getLong(EpisodeDetailsQuery.TITLE);
                final String runTime = episode.getString(EpisodeDetailsQuery.TITLE);
                final String episodestring = ShareUtils.onCreateShareString(getActivity(), episode);
                ShareUtils.onAddCalendarEvent(getActivity(), showTitle, episodestring, airDate,
                        airsTime, runTime);
                break;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * setup adapter to map a cursor columns to a view textview and imageviews.
     */
    private void setupAdapter() {

        String[] from = new String[] {  "TITLE", "OVERVIEW" };  // map title and overview to two UI textview
        int[] to = new int[] { R.id.TextViewEpisodeTitle, R.id.TextViewEpisodeDescription };

        // set cursor to be null when init; once cursor is loaded, a swapCursor call will set it up!
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.episodedetails, null, from, to, 0);
        
        mAdapter.setViewBinder(new ViewBinder() {
            public boolean setViewValue(View view, Cursor episode, int columnIndex) {
                if (columnIndex == EpisodeDetailsQuery.TITLE) {
                    TextView showtitle = (TextView) view;
                    showtitle.setText(episode.getString(EpisodeDetailsQuery.TITLE));
                    final String showId = episode.getString(EpisodeDetailsQuery.TITLE);
                    showtitle.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                        	//startActivity(i);
                        }
                    });
                    return true;
                }
                return false;
            }
        });
        setListAdapter(mAdapter);
    }

    public String getEpisodeId() {
        return getArguments().getString("_ID");
    }

    protected void onLoadImage(String imagePath, FrameLayout container) {
    }
    
    interface EpisodeDetailsQuery {
        String[] PROJECTION = new String[] { "_ID", "TITLE", "OVERVIEW" };
        int _ID = 0;
        int TITLE = 1;
        int OVERVIEW = 2;
    }

    /**
     * impl LoaderManager.callbacks<Cursor> callback funcs.
     */
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return new CursorLoader(getActivity(), Uri.parse(getEpisodeId()),
                EpisodeDetailsQuery.PROJECTION, null, null, null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);  // when cursorloader done, setup adapter data.
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
