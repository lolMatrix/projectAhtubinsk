package com.matrixsoft.pison.pa;

import android.content.Context;
import android.content.Intent;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.methods.VKApiWall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.TimeZone;


public class ArticleAdapter extends BaseAdapter{

    JSONArray articles;
    String textArticle;
    String dateArticle;
    String count_likes;
    LayoutInflater inflater;
    Context ctx;
    int articles_count;
    int c1 = 0;

    ArticleAdapter (Context c, JSONArray j, int count) {
        ctx = c;
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        articles = j;
        articles_count = count;
    }

    @Override
    public int getCount() {
        return articles_count;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_articles, parent, false);
        }
        String count_comments = "";
        try {
            JSONObject article = articles.getJSONObject(position);

            java.util.Date time = new java.util.Date(1000*article.getLong("date"));
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            dateArticle = sdf.format(time);
            textArticle = article.getString("text");
            count_likes = article.getJSONObject("likes").getString("count");
            int is_liked = article.getJSONObject("likes").getInt("user_likes");
            count_comments = article.getJSONObject("comments").getString("count");

            CheckBox likeButton = (CheckBox) view.findViewById(R.id.likeButton);
            likeButton.setText(count_likes);

            if (is_liked == 1) {
                likeButton.setChecked(true);
            } else likeButton.setChecked(false);

            likeButton.setTag(position);
            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    CheckBox box = (CheckBox) v.findViewById(R.id.likeButton);
                    int i = (Integer) v.getTag();
                    Boolean isChecked = box.isChecked();

                    try {

                        articles.getJSONObject(i).getJSONObject("likes").put("user_likes", (isChecked) ? 1 : 0);
                        int cl = articles.getJSONObject(i).getJSONObject("likes").getInt("count");

                        if (isChecked) {
                            cl++;
                            VKRequest vkRequest = new VKRequest("likes.add", VKParameters.from("type", articles.getJSONObject(i).getString("post_type"), VKApiConst.OWNER_ID, articles.getJSONObject(i).getString("owner_id"), "item_id", articles.getJSONObject(i).getString("id")));
                            vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
                                @Override
                                public void onComplete(VKResponse response) {
                                    super.onComplete(response);

                                }
                            });

                        } else {
                            cl--;

                            VKRequest vkRequest = new VKRequest("likes.delete", VKParameters.from("type", articles.getJSONObject(i).getString("post_type"), VKApiConst.OWNER_ID, articles.getJSONObject(i).getString("owner_id"), "item_id", articles.getJSONObject(i).getString("id")));
                            vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
                                @Override
                                public void onComplete(VKResponse response) {
                                    super.onComplete(response);

                                }
                            });

                        }

                        articles.getJSONObject(i).getJSONObject("likes").put("count", cl);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    notifyDataSetChanged();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView textView = (TextView) view.findViewById(R.id.articleTextView);
        textView.setText(textArticle);
        TextView dateView = (TextView) view.findViewById(R.id.dateView);
        dateView.setText(dateArticle);
        Button commentButton = (Button) view.findViewById(R.id.commentButton);
        commentButton.setText(count_comments);
        commentButton.setTag(position);
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v.findViewById(R.id.commentButton);
                Intent i = new Intent(ctx, CommentActivity.class);
                int p = Integer.parseInt(b.getTag().toString());
                try {
                    i.putExtra("text", articles.getJSONObject(p).getString("text"));
                    i.putExtra("count_likes", articles.getJSONObject(p).getJSONObject("likes").getInt("count"));
                    i.putExtra("isLiked", articles.getJSONObject(p).getJSONObject("likes").getInt("user_likes"));
                    i.putExtra("date", articles.getJSONObject(p).getLong("date"));
                    i.putExtra("owner_id", articles.getJSONObject(p).getInt("owner_id"));
                    i.putExtra("post_id", articles.getJSONObject(p).getInt("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ctx.startActivity(i);
            }
        });


        return view;

    }

    void updateArray(JSONArray a, int c){

        for (int i = 0; i < a.length(); i++){
            try {
                articles.put(a.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        articles_count = c;
        notifyDataSetChanged();

    }

    void reloadArray (JSONArray a, int c) {
        articles_count = c;
        articles = a;
        notifyDataSetChanged();
    }

}