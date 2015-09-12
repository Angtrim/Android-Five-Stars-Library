package angtrim.com.fivestarslibrary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by angtrim on 12/09/15.
 *
 */
public class FiveStarsDialog  {

    private final static String DEFAULT_TITLE = "Rate this app";
    private final static String DEFAULT_TITLE_SUPPORT = "Ask for support";
    private final static String DEFAULT_POSITIVE = "Ok";
    private final static String DEFAULT_NEGATIVE = "Not Now";
    private final static String DEFAULT_NEVER = "Never";
    private final static String SP_NUM_OF_ACCESS = "numOfAccess";
    private static final String SP_DISABLED = "disabled";
    private final Context context;
    private MaterialDialog ratingDialog;
    private String packageName;
    private String negativeMessage;
    private String supportEmail;
    private int buttonsColor = R.color.md_material_blue_600;
    private View positiveButton;
    private View negativeButton;
    private View neutralButton;
    private TextView contentText;
    private RatingBar ratingBar;
    private String supportText = "Do you need support? Want to tell us what's wrong? Tap OK to send us a report.";
    private String rateText ="How much do you love our app?";


    public FiveStarsDialog(Context context,String supportEmail){

        this.context = context;

    }

    private void build(){
        ratingDialog =  new MaterialDialog.Builder(context)
                .title(DEFAULT_TITLE)
                .customView(R.layout.stars, true)
                .positiveText(DEFAULT_POSITIVE)
                .negativeText(DEFAULT_NEGATIVE)
                .positiveColorRes(buttonsColor)
                .negativeColorRes(buttonsColor)
                .neutralColorRes(buttonsColor)
                .neutralText(DEFAULT_NEVER).build();
        initViews();
    }

    private void initViews() {
        ratingBar = (RatingBar) ratingDialog.getCustomView().findViewById(R.id.ratingBar);
        positiveButton = ratingDialog.getActionButton(DialogAction.POSITIVE);
        negativeButton = ratingDialog.getActionButton(DialogAction.NEGATIVE);
        neutralButton = ratingDialog.getActionButton(DialogAction.NEUTRAL);
        contentText = (TextView)ratingDialog.getCustomView().findViewById(R.id.text_content);
        contentText.setText(rateText);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ratingBar.getRating() <= 3){
                    initSupportDialog();
                }else{
                    openMarket();

                }
                ratingDialog.hide();
                disable();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ratingDialog.hide();
                SharedPreferences shared = context.getSharedPreferences(context.getPackageName(),Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = shared.edit();
                int numOfAccess = shared.getInt(SP_NUM_OF_ACCESS,0);
                editor.putInt(SP_NUM_OF_ACCESS, 0 - numOfAccess*2 );
                editor.apply();
            }
        });

        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ratingDialog.hide();
                disable();
            }
        });


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

    private void initSupportDialog() {
        MaterialDialog supportDialog = new MaterialDialog.Builder(context)
                .title(DEFAULT_TITLE_SUPPORT)
                .positiveText(DEFAULT_POSITIVE)
                .negativeText(DEFAULT_NEGATIVE)
                .positiveColorRes(buttonsColor)
                .negativeColorRes(buttonsColor)
                .content(supportText)
                .build();

        supportDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

                emailIntent.setType("plain/text");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, supportEmail);
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "App Report ("+context.getPackageName()+")");
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
                context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            }
        });
        ratingDialog.hide();
        supportDialog.show();
    }


    public FiveStarsDialog setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public FiveStarsDialog setNegativeMessage(String negativeMessage) {
        this.negativeMessage = negativeMessage;
        return this;
    }

    public FiveStarsDialog setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
        return this;
    }

    public FiveStarsDialog setButtonsColor(int buttonsColor) {
        this.buttonsColor = buttonsColor;
        return this;
    }

    public FiveStarsDialog setSupportText(String supportText){
        this.supportText = supportText;
        return this;
    }

    public FiveStarsDialog setRateText(String rateText){
        this.rateText = rateText;
        return this;
    }

    public void show() {
        SharedPreferences shared = context.getSharedPreferences(context.getPackageName(),Context.MODE_PRIVATE);
        boolean disabled  = shared.getBoolean(SP_DISABLED,false);
        if(!disabled){
            build();
            ratingDialog.show();
        }

    }

    public void showAfter(int numberOfAccess){
        SharedPreferences shared = context.getSharedPreferences(context.getPackageName(),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        int numOfAccess = shared.getInt(SP_NUM_OF_ACCESS,0);
        editor.putInt(SP_NUM_OF_ACCESS, numOfAccess + 1);
        editor.apply();
        if(numOfAccess + 1 >= numberOfAccess){
            show();
        }
    }



}
