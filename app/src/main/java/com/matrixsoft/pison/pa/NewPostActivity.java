package com.matrixsoft.pison.pa;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

public class NewPostActivity extends AppCompatActivity {
    Button sendArticle;
    TextInputEditText messageArticle;
    CheckBox isAnon;
    int isAnonMessage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        VKSdk.initialize(NewPostActivity.this);

        final Intent intent = getIntent();

        isAnon = (CheckBox) findViewById(R.id.isAnonArticle);
        isAnon.setChecked(false);
        isAnon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox c = (CheckBox) findViewById(R.id.isAnonArticle);
                if (c.isChecked()){
                    isAnonMessage = 0;
                }else {
                    isAnonMessage = 1;
                }
            }
        });
        messageArticle = (TextInputEditText) findViewById(R.id.messageArticle);
        sendArticle = (Button) findViewById(R.id.sendArticle);
        sendArticle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageArticle.getText().toString().trim() != ""){
                    VKRequest request = new VKRequest("wall.post", VKParameters.from("owner_id", -1*intent.getIntExtra("id", 0), "message", messageArticle.getText().toString(), "signed", isAnonMessage));
                    request.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
                            finish();
                        }

                        @Override
                        public void onError(VKError error) {
                            super.onError(error);
                            Toast.makeText(NewPostActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }



}
