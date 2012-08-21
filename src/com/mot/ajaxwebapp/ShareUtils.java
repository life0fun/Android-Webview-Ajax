
package com.mot.ajaxwebapp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class ShareUtils {

    public static final String KEY_GETGLUE_COMMENT = "com.battlelancer.seriesguide.getglue.comment";

    public static final String KEY_GETGLUE_IMDBID = "com.battlelancer.seriesguide.getglue.imdbid";

    protected static final String TAG = "ShareUtils";

    /**
     * Show a dialog allowing to chose from various sharing options.
     * 
     * @param shareData - a {@link Bundle} including all
     *            {@link ShareUtils.ShareItems}
     */
    public static void showShareDialog(FragmentManager manager, Bundle shareData) {
        // Create and show the dialog.
        ShareDialogFragment newFragment = ShareDialogFragment.newInstance(shareData);
        FragmentTransaction ft = manager.beginTransaction();
        newFragment.show(ft, "sharedialog");
    }

    public static class ShareDialogFragment extends DialogFragment {
        public static ShareDialogFragment newInstance(Bundle shareData) {
            ShareDialogFragment f = new ShareDialogFragment();
            f.setArguments(shareData);
            return f;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final String imdbId = getArguments().getString(ShareUtils.ShareItems.IMDBID);
            final String sharestring = getArguments().getString(ShareUtils.ShareItems.SHARESTRING);
            final CharSequence[] items = getResources().getStringArray(R.array.share_items);

            return new AlertDialog.Builder(getActivity()).setTitle("Share")
                    .setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            switch (item) {
                                case 0:
                                    // GetGlue check in
                                	Toast.makeText(getActivity(), "NoIMDB entry", Toast.LENGTH_LONG).show();
                                    break;
                                case 1: {
                                    // trakt check in

                                    // DialogFragment.show() will take care of
                                    // adding the fragment
                                    // in a transaction. We also want to remove
                                    // any currently showing
                                    // dialog, so make our own transaction and
                                    // take care of that here.
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    Fragment prev = getFragmentManager().findFragmentByTag("progress-dialog");
                                    if (prev != null) {
                                        ft.remove(prev);
                                    }
                                    ft.addToBackStack(null);

                                    // Create and show the dialog.
                                    ProgressDialog.show(getActivity(), "checking in", "checking in");
                                    break;
                                }
                                case 4: {
                                    // Android apps
                                    String text = sharestring + imdbId;
                                    Intent i = new Intent(Intent.ACTION_SEND);
                                    i.setType("text/plain");
                                    i.putExtra(Intent.EXTRA_TEXT, text);
                                    startActivity(Intent.createChooser(i, "Share Episode"));
                                    break;
                                }
                            }
                        }
                    }).create();
        }
    }

       
    public interface ShareItems {
        String SEASON = "season";

        String IMDBID = "imdbId";

        String SHARESTRING = "sharestring";

        String EPISODESTRING = "episodestring";

        String EPISODE = "episode";

        String TVDBID = "tvdbid";

        String RATING = "rating";

        String TRAKTACTION = "traktaction";
    }

    public static String onCreateShareString(Context context, final Cursor episode) {
        String season = episode.getString(episode.getColumnIndexOrThrow("SEASON"));
        String number = episode.getString(episode.getColumnIndexOrThrow("NUMBER"));
        String title = episode.getString(episode.getColumnIndexOrThrow("TITLE"));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return season + number + title;
    }

    public static void onAddCalendarEvent(Context context, String title, String description,
            String airdate, long airtime, String runtime) {
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("title", title);
        intent.putExtra("description", description);

        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context
                    .getApplicationContext());
            boolean useUserTimeZone = prefs.getBoolean("KEY_USE_MY_TIMEZONE", false);

            Calendar cal = Calendar.getInstance();

            long startTime = cal.getTimeInMillis();
            long endTime = startTime + Long.valueOf(runtime) * 60 * 1000;
            intent.putExtra("beginTime", startTime);
            intent.putExtra("endTime", endTime);

            context.startActivity(intent);

            AnalyticsUtils.getInstance(context).trackEvent("Sharing", "Calendar", "Success", 0);
        } catch (Exception e) {
            AnalyticsUtils.getInstance(context).trackEvent("Sharing", "Calendar", "Failed", 0);
        }
    }

    public static String toSHA1(byte[] convertme) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return byteArrayToHexString(md.digest(convertme));
    }

    public static String byteArrayToHexString(byte[] b) {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public enum TraktAction {
        SEEN_EPISODE(0), RATE_EPISODE(1), CHECKIN_EPISODE(2);

        final private int mIndex;

        private TraktAction(int index) {
            mIndex = index;
        }

        public int index() {
            return mIndex;
        }
    }

    public interface TraktStatus {
        String SUCCESS = "success";

        String FAILURE = "failure";
    }
   
}
