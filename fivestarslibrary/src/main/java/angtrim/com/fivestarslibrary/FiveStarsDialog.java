package angtrim.com.fivestarslibrary;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by angtrim on 12/09/15.
 */
public class FiveStarsDialog implements DialogInterface.OnClickListener {

    private final static String SP_NUM_OF_ACCESS = "numOfAccess";
    private final static String SP_MAX_DATE = "maxDate";
    private final static String PATTERN = "yyyy/MM/dd";
    private static final String SP_DISABLED = "disabled";
    private static final String TAG = FiveStarsDialog.class.getSimpleName();
    private final Context context;
    private boolean isForceMode = false;
    private final SharedPreferences sharedPrefs;
    private String defaultTitle;
    private String defaultText;
    private String supportEmail;
    private TextView contentTextView;
    private RatingBar ratingBar;
    private String title = null;
    private String rateText = null;
    private AlertDialog alertDialog;
    private View dialogView;
    private int upperBound = 4;
    private NegativeReviewListener negativeReviewListener;
    private ReviewListener reviewListener;
    private InAppReviewListener inAppReviewListener;
    private int starColor;
    private String positiveButtonText;
    private String negativeButtonText;
    private String neutralButtonText;
    private boolean inAppReviewMode = false;
    private boolean afterNDaysMode = false;

    public FiveStarsDialog(Context context, String supportEmail) {
        this.context = context;
        negativeButtonText = context.getString(R.string.BtnLater);
        positiveButtonText = context.getString(R.string.BtnOK);
        neutralButtonText = context.getString(R.string.BtnNever);
        defaultTitle = context.getString(R.string.RateApp);
        defaultText = context.getString(R.string.DefaultText);
        sharedPrefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        this.supportEmail = supportEmail;
    }

    private void build() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        dialogView = inflater.inflate(R.layout.stars, null);
        String titleToAdd = (title == null) ? defaultTitle : title;
        String textToAdd = (rateText == null) ? defaultText : rateText;
        contentTextView = dialogView.findViewById(R.id.text_content);
        contentTextView.setText(textToAdd);
        ratingBar = dialogView.findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                if (isForceMode && v >= upperBound) {
                    if (inAppReviewMode) {
                        launchInAppReview();
                    } else {
                        openMarket();
                    }
                    if (reviewListener != null)
                        reviewListener.onReview((int) ratingBar.getRating());
                }
            }
        });

        if (starColor != -1) {
            LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
            stars.getDrawable(1).setColorFilter(starColor, PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(2).setColorFilter(starColor, PorterDuff.Mode.SRC_ATOP);
        }

        builder.setTitle(titleToAdd)
            .setView(dialogView);

        if (negativeButtonText !=null && !negativeButtonText.isEmpty())
            builder.setNegativeButton(negativeButtonText, this);
        if (positiveButtonText !=null && !positiveButtonText.isEmpty())
            builder.setPositiveButton(positiveButtonText, this);
        if (neutralButtonText !=null && !neutralButtonText.isEmpty())
            builder.setNeutralButton(neutralButtonText, this);
        alertDialog = builder.create();
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
        emailIntent.setType("text/email");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{supportEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "App Report (" + context.getPackageName() + ")");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    private void show() {
        boolean disabled = sharedPrefs.getBoolean(SP_DISABLED, false);
        if (!disabled) {
            build();
            alertDialog.show();
        }
    }

    private boolean isMaxDateEmpty() {
        if (!sharedPrefs.contains(SP_MAX_DATE)) {
            return true;
        }
        else if (TextUtils.isEmpty(sharedPrefs.getString(SP_MAX_DATE, ""))) {
            return true;
        }
        else {
            return false;
        }
    }

    public void showAfter(int numberOfAccess) {
        build();
        SharedPreferences.Editor editor = sharedPrefs.edit();
        if (!afterNDaysMode) {
            defaultLaunch(numberOfAccess, editor);
        }
        else {
            launchByDates(numberOfAccess, editor);
        }
    }

    private void launchByDates(int numberOfAccess, SharedPreferences.Editor editor) {
        Date maxDate;
        if (isMaxDateEmpty()) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, numberOfAccess);

            SimpleDateFormat formatter = new SimpleDateFormat(PATTERN);
            maxDate = c.getTime();
            editor.putString(SP_MAX_DATE, formatter.format(maxDate));
            editor.apply();
        } else {
            try {
                maxDate = new SimpleDateFormat(PATTERN).parse(sharedPrefs.getString(SP_MAX_DATE, ""));

                long diffInMillie = Math.abs((new Date()).getTime() - maxDate.getTime());
                long diff = TimeUnit.DAYS.convert(diffInMillie, TimeUnit.MILLISECONDS);

                if (diff >= numberOfAccess) {
                    show();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void defaultLaunch(int numberOfAccess, SharedPreferences.Editor editor) {
        int numOfAccess = sharedPrefs.getInt(SP_NUM_OF_ACCESS, 0);
        editor.putInt(SP_NUM_OF_ACCESS, numOfAccess + 1);
        editor.apply();
        if (numOfAccess + 1 >= numberOfAccess) {
            show();
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                if (ratingBar.getRating() < upperBound) {
                    if (negativeReviewListener == null) {
                        sendEmail();
                    } else {
                        negativeReviewListener.onNegativeReview((int) ratingBar.getRating());
                    }
                } else if (!isForceMode) {
                    openMarket();
                }
                disable();
                if (reviewListener != null) {
                    reviewListener.onReview((int) ratingBar.getRating());
                }
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                disable();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putInt(SP_NUM_OF_ACCESS, 0);
                if (sharedPrefs.contains(SP_MAX_DATE)) {
                    editor.putString(SP_MAX_DATE, "");
                }
                editor.apply();
                break;
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

    public FiveStarsDialog setRateText(String rateText) {
        this.rateText = rateText;
        return this;
    }

    public FiveStarsDialog setStarColor(int color) {
        starColor = color;
        return this;
    }

    public FiveStarsDialog setPositiveButtonText(String positiveButtonText) {
        this.positiveButtonText = positiveButtonText;
        return this;
    }

    public FiveStarsDialog setNegativeButtonText(String negativeButtonText) {
        this.negativeButtonText = negativeButtonText;
        return this;
    }

    public FiveStarsDialog setNeutralButton(String neutralButtonText) {
        this.neutralButtonText = neutralButtonText;
        return this;
    }

    /**
     * Set to true if you want to send the user directly to the market
     *
     * @param isForceMode
     * @return
     */
    public FiveStarsDialog setForceMode(boolean isForceMode) {
        this.isForceMode = isForceMode;
        return this;
    }

    /**
     * Set the upper bound for the rating.
     * If the rating is >= of the bound, the market is opened.
     *
     * @param bound the upper bound
     * @return the dialog
     */
    public FiveStarsDialog setUpperBound(int bound) {
        this.upperBound = bound;
        return this;
    }

    /**
     * Set a custom listener if you want to OVERRIDE the default "send email" action when the user gives a negative review
     *
     * @param listener
     * @return
     */
    public FiveStarsDialog setNegativeReviewListener(NegativeReviewListener listener) {
        this.negativeReviewListener = listener;
        return this;
    }

    /**
     * Set a listener to get notified when a review (positive or negative) is issued, for example for tracking purposes
     *
     * @param listener
     * @return
     */
    public FiveStarsDialog setReviewListener(ReviewListener listener) {
        this.reviewListener = listener;
        return this;
    }

    /**
     * Enable in-app review popup
     *
     * @param inAppReviewMode
     * @return
     */
    public FiveStarsDialog setInAppReviewMode(boolean inAppReviewMode) {
        this.inAppReviewMode = inAppReviewMode;
        return this;
    }

    /**
     * Set a listener to get notified when in app review flow completed, for example for tracking purposes
     *
     * Note that, The API does not indicate whether the user reviewed or not, or even whether the review dialog was shown
     *
     * @param inAppReviewListener
     * @return
     */
    public FiveStarsDialog setInAppReviewListener(InAppReviewListener inAppReviewListener) {
        this.inAppReviewListener = inAppReviewListener;
        return this;
    }

    /**
     *
     * Enable launching after N days
     * @param afterNDaysMode
     * @return
     */
    public FiveStarsDialog setAfterNDaysMode(boolean afterNDaysMode) {
        this.afterNDaysMode = afterNDaysMode;
        return this;
    }

    private void launchInAppReview() {
        final ReviewManager manager = ReviewManagerFactory.create(context);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(new OnCompleteListener<ReviewInfo>() {
            @Override
            public void onComplete(@NonNull Task<ReviewInfo> task) {
                if (task.isSuccessful()) {
                    if (context instanceof Activity) {
                        Task<Void> flow = manager.launchReviewFlow(((Activity) context), task.getResult());
                        flow.addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (inAppReviewListener != null) {
                                    inAppReviewListener.onInAppReview();
                                }
                            }
                        });
                        flow.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                openMarket();
                            }
                        });
                    } else {
                        openMarket();
                    }
                } else {
                    openMarket();
                }
            }
        });
    }
}
