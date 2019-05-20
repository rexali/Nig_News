package com.ebizebiz.android.nignews;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static com.ebizebiz.android.nignews.MainActivity.EXTRA_DATA_ID;
import static com.ebizebiz.android.nignews.MainActivity.EXTRA_DATA_UPDATE_NEWS_NAME;
import static com.ebizebiz.android.nignews.MainActivity.EXTRA_DATA_UPDATE_NEWS_URL;

public class NewNewsActivity extends AppCompatActivity {


    public static final String EXTRA_REPLY_NAME = "com.ebizebiz.android.nignews.REPLY_NAME";
    public static final String EXTRA_REPLY_URL = "com.ebizebiz.android.nignews.REPLY_URL";
    public static final String EXTRA_REPLY_ID = "com.ebizebiz.android.nignews.REPLY_ID";

    private EditText mEditNewsName;
    private EditText mEditNewsUrl;
    int id = -1 ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_news);

        mEditNewsName = findViewById(R.id.edit_news_name);
        mEditNewsUrl = findViewById(R.id.edit_news_url);


        final Bundle extras = getIntent().getExtras();

        // If we are passed content, fill it in for the user to edit.
        if (extras != null) {
            String name = extras.getString(EXTRA_DATA_UPDATE_NEWS_NAME, "");
            String url = extras.getString(EXTRA_DATA_UPDATE_NEWS_URL, "");

            if (!name.isEmpty()) {
                mEditNewsName.setText(name);
                mEditNewsName.setSelection(name.length());
                mEditNewsName.requestFocus();
            }

            if (!url.isEmpty()) {
                mEditNewsUrl.setText(url);
                mEditNewsUrl.setSelection(url.length());
                mEditNewsUrl.requestFocus();

            }

            // Otherwise, start with empty fields.
        }


        final Button button = findViewById(R.id.button_save);

        // When the user presses the Save button, create a new Intent for the reply.
        // The reply Intent will be sent back to the calling activity (in this case, MainActivity).
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Create a new Intent for the reply.
                Intent replyIntent = new Intent();
                if (TextUtils.isEmpty(mEditNewsName.getText())&&
                        TextUtils.isEmpty(mEditNewsUrl.getText())) {
                    // No word was entered, set the result accordingly.
                    setResult(RESULT_CANCELED, replyIntent);
                } else {
                    // Get the new word that the user entered.
                    String newsName = mEditNewsName.getText().toString();
                    String newsUrl = mEditNewsUrl.getText().toString();
                    // Put the new word in the extras for the reply Intent.
                    replyIntent.putExtra(EXTRA_REPLY_NAME, newsName);
                    replyIntent.putExtra(EXTRA_REPLY_URL, newsUrl);
                    if (extras != null && extras.containsKey(EXTRA_DATA_ID)) {
                        int id = extras.getInt(EXTRA_DATA_ID, -1);
                        if (id != -1) {
                            replyIntent.putExtra(EXTRA_REPLY_ID, id);
                        }
                    }
                    // Set the result status to indicate success.
                    setResult(RESULT_OK, replyIntent);
                }
                finish();
            }
        });
    }
}
