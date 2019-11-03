package com.matrixsoft.pison.pa;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.methods.VKApiWall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

public class CommentActivity extends AppCompatActivity {

    ListView comment;
    String[] scope = new String[] {VKScope.GROUPS, VKScope.WALL};
    final int comentsCount = 10;
    Intent intent;
    CommentsAdapter adapter;
    int commentPage = 0;
    int fvi = 0;
    int vic = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        intent = getIntent();

        VKSdk.initialize(this);

        comment = (ListView) findViewById(R.id.commentView);

        int isl = intent.getIntExtra("isLiked", 0);
        boolean l;
        if (isl == 1){
            l = true;
        }else {
            l = false;
        }

        comment.addHeaderView(createHeader(intent.getStringExtra("text"), intent.getIntExtra("count_likes", 0), intent.getLongExtra("date", 0), l));

        Button addComment = (Button) findViewById(R.id.addComment);
        final TextInputEditText t = (TextInputEditText) findViewById(R.id.editComment);
        addComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = t.getText().toString();
                if (text.trim().length() != 0){
                    VKRequest request = new VKRequest("wall.createComment", VKParameters.from("owner_id", intent.getIntExtra("owner_id", 0), "post_id", intent.getIntExtra("post_id", 0), "message", text));
                    request.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
                            t.setText("");
                            if (VKSdk.isLoggedIn()){

                                VKRequest request = new VKRequest("wall.getComments", VKParameters.from("owner_id", intent.getIntExtra("owner_id", 0), "post_id", intent.getIntExtra("post_id", 0), "count", 1, "extended", 1, "need_likes", 1, "offset", adapter.getCount()));
                                request.executeWithListener(new VKRequest.VKRequestListener() {
                                    @Override
                                    public void onComplete(VKResponse response) {
                                        super.onComplete(response);
                                        try {
                                            JSONObject j = response.json.getJSONObject("response");
                                            Log.d("myTag", j.getString("count") + " count");
                                            Log.d("myTag", Integer.toString(commentPage + 11) + " page");
                                            if (j.getInt("count") <= commentPage + 11) {
                                                if (j.getInt("count") != 0) {
                                                    commentPage++;
                                                    JSONArray coments = j.getJSONArray("items");
                                                    JSONArray names = j.getJSONArray("profiles");
                                                    JSONArray groups = j.getJSONArray("groups");

                                                    adapter.updateArrays(coments, names, groups, coments.length());
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                            } else VKSdk.login(CommentActivity.this, scope);
                        }
                    });
                }
            }
        });

        getComents();
    }

    View createHeader(String text, int countLikes, long date, Boolean isLiked){

        View v = getLayoutInflater().inflate(R.layout.item_header_comment, null);

        TextView textView = (TextView) v.findViewById(R.id.textArticle);
        TextView dateView = (TextView) v.findViewById(R.id.textDate);
        CheckBox like = (CheckBox) v.findViewById(R.id.countLikes);

        java.util.Date time = new java.util.Date(1000*date);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        String sdate = sdf.format(time);

        textView.setText(text);
        dateView.setText(sdate);

        like.setText(Integer.toString(countLikes));
        like.setChecked(isLiked);
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CheckBox c = (CheckBox) v.findViewById(R.id.countLikes);
                boolean isLiked = c.isChecked();
                if (isLiked) {
                    VKRequest request = new VKRequest("likes.add", VKParameters.from("type", "post", "owner_id", intent.getIntExtra("owner_id", 0), "item_id", intent.getIntExtra("post_id", 0)));
                    request.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
                            c.setText(Integer.toString(Integer.parseInt((String) c.getText()) + 1));
                        }
                    });
                }else {
                    VKRequest request = new VKRequest("likes.delete", VKParameters.from("type", "post", "owner_id", intent.getIntExtra("owner_id", 0), "item_id", intent.getIntExtra("post_id", 0)));
                    request.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
                            c.setText(Integer.toString(Integer.parseInt((String) c.getText()) - 1));
                        }
                    });
                }
            }
        });

        return v;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
// Пользователь успешно авторизовался

            }
            @Override
            public void onError(VKError error) {
// Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                Toast.makeText(CommentActivity.this, "Ошибка авторизации, повторитете позже", Toast.LENGTH_SHORT).show();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    void getComents (){
        if (VKSdk.isLoggedIn()){

            VKRequest request = new VKRequest("wall.getComments", VKParameters.from("owner_id", intent.getIntExtra("owner_id", 0), "post_id", intent.getIntExtra("post_id", 0), "count", comentsCount, "extended", 1, "need_likes", 1));
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    try {
                        JSONObject j = response.json.getJSONObject("response");
                        if (j.getInt("count") != 0) {
                            JSONArray coments = j.getJSONArray("items");
                            JSONArray names = j.getJSONArray("profiles");
                            JSONArray groups = j.getJSONArray("groups");
                            adapter = new CommentsAdapter(coments, names, groups, coments.length(), CommentActivity.this);
                            comment.setAdapter(adapter);
                            comment.setOnScrollListener(new AbsListView.OnScrollListener() {
                                @Override
                                public void onScrollStateChanged(AbsListView view, int scrollState) {
                                    if (scrollState == 0 || scrollState == 2 || scrollState == 1){

                                        if (vic + fvi - 1 == commentPage + comentsCount){
                                            commentPage = commentPage + comentsCount;
                                            commentUpd();
                                        }
                                    }
                                }

                                @Override
                                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                                    fvi = firstVisibleItem;
                                    vic = visibleItemCount;
                                }
                            });
                        }else{
                            adapter = new CommentsAdapter(null, null, null, 0, CommentActivity.this);
                            comment.setAdapter(adapter);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        } else VKSdk.login(this, scope);
    }

    void commentUpd () {
        if (VKSdk.isLoggedIn()){

            VKRequest request = new VKRequest("wall.getComments", VKParameters.from("owner_id", intent.getIntExtra("owner_id", 0), "post_id", intent.getIntExtra("post_id", 0), "count", comentsCount, "extended", 1, "need_likes", 1, "offset", commentPage));
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    try {
                        JSONObject j = response.json.getJSONObject("response");
                        if (j.getInt("count") != 0) {
                            JSONArray coments = j.getJSONArray("items");
                            JSONArray names = j.getJSONArray("profiles");
                            JSONArray groups = j.getJSONArray("groups");
                            adapter.updateArrays(coments, names, groups, coments.length());
                        }else{
                            adapter = new CommentsAdapter(null, null, null, 0, CommentActivity.this);
                            comment.setAdapter(adapter);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        } else VKSdk.login(this, scope);
    }

}
