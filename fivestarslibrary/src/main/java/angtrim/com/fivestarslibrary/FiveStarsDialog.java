package angtrim.com.fivestarslibrary;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;


/**
 * Created by angtrim on 12/09/15.
 *
 */
public class FiveStarsDialog  implements DialogInterface.OnClickListener{

    private final static String DEFAULT_TITLE = "Rate this app";
    private final static String DEFAULT_TEXT = "How much do you love our app?";
    private final static String DEFAULT_POSITIVE = "Ok";
    private final static String DEFAULT_NEGATIVE = "Not Now";
    private final static String DEFAULT_NEVER = "Never";
    private final static String SP_NUM_OF_ACCESS = "numOfAccess";
    private static final String SP_DISABLED = "disabled";
    private static final String TAG = FiveStarsDialog.class.getSimpleName();
    private final Context context;
    private boolean isForceMode = false;
    SharedPreferences sharedPrefs;
    private String supportEmail;
    private TextView contentTextView;
    private RatingBar ratingBar;
    private String title = null;
    private String rateText = null;
    private AlertDialog alertDialog;
    private View dialogView;


    public FiveStarsDialog(Context context,String supportEmail){
        this.context = context;
        sharedPrefs = context.getSharedPreferences(context.getPackageName(),Context.MODE_PRIVATE);
        this.supportEmail = supportEmail;
    }

    private void build(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        dialogView = inflater.inflate(R.layout.stars, null);
        String titleToAdd = (title == null) ? DEFAULT_TITLE : title;
        String textToAdd = (rateText == null) ? DEFAULT_TEXT : rateText;
        contentTextView = (TextView)dialogView.findViewById(R.id.text_content);
        contentTextView.setText(textToAdd);
        ratingBar = (RatingBar) dialogView.findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                Log.d(TAG, "Rating changed : " + v);
                if (isForceMode && v >= 4) {
                    openMarket();
                }
            }
        });
        alertDialog = builder.setTitle(titleToAdd)
                .setView(dialogView)
                .setNegativeButton(DEFAULT_NEGATIVE,this)
                .setPositiveButton(DEFAULT_POSITIVE,this)
                .setNeutralButton(DEFAULT_NEVER,this)
                .create();
    }



    private void disable() {
        SharedPreferences shared = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putBoolean(SP_DISABLED, true);
        editor.apply();
    }

    private void openMarket() {
        final String appPackageName = context.getPackageName();
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }


    private void sendEmail() {
        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, supportEmail);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "App Report ("+context.getPackageName()+")");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }




    private void show() {
        boolean disabled  = sharedPrefs.getBoolean(SP_DISABLED, false);
        if(!disabled){
            build();
            alertDialog.show();
        }
    }

    public void showAfter(int numberOfAccess){
        build();
        SharedPreferences.Editor editor = sharedPrefs.edit();
        int numOfAccess = sharedPrefs.getInt(SP_NUM_OF_ACCESS, 0);
        editor.putInt(SP_NUM_OF_ACCESS, numOfAccess + 1);
        editor.apply();
        if(numOfAccess + 1 >= numberOfAccess){
            show();
        }
    }


    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if(i == DialogInterface.BUTTON_POSITIVE){
            if(ratingBar.getRating() <= 3){
                sendEmail();
            }else if(!isForceMode){
                openMarket();
            }
            disable();
        }
        if(i == DialogInterface.BUTTON_NEUTRAL){
            disable();
        }
        if(i == DialogInterface.BUTTON_NEGATIVE){
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putInt(SP_NUM_OF_ACCESS, 0);
            editor.apply();
        }
        alertDialog.hide();
    }

    public FiveStarsDialog setTitle(String title) {
        this.title = title;
        return this;

    }

    public FiveStarsDialog setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
        return this;
    }

    public FiveStarsDialog setRateText(String rateText){
        this.rateText = rateText;
        return this;
    }

    public FiveStarsDialog setForceMode(boolean isForceMode){
        this.isForceMode = isForceMode;
        return this;
    }
}
