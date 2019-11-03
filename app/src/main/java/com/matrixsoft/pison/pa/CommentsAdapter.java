package com.matrixsoft.pison.pa;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;

import static android.content.ContentValues.TAG;

public class CommentsAdapter extends BaseAdapter {

    JSONArray comments;
    int count;
    Context ctx;
    LayoutInflater inflater;
    JSONArray names;
    JSONArray groups;

    CommentsAdapter (JSONArray a, JSONArray n, JSONArray g,  int c, Context ct) {
        comments = a;
        names = n;
        count = c;
        ctx = ct;
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        groups = g;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null){
            view = inflater.inflate(R.layout.item_comment, parent, false);
        }

        TextView nameComentator = (TextView) view.findViewById(R.id.nameComentator);
        TextView comentText = (TextView) view.findViewById(R.id.textComent);
        TextView dateText = (TextView) view.findViewById(R.id.dateComent);
        CheckBox likeButton = (CheckBox) view.findViewById(R.id.comenLikeButton);

        try {
            JSONObject coment = comments.getJSONObject(position);
            comentText.setText(coment.getString("text"));
            java.util.Date time = new java.util.Date(1000*coment.getLong("date"));
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            dateText.setText(sdf.format(time));
            likeButton.setText(coment.getJSONObject("likes").getString("count"));
            int isLiked = coment.getJSONObject("likes").getInt("user_likes");
            if (isLiked == 1) {
                likeButton.setChecked(true);
            }
            else likeButton.setChecked(false);
            likeButton.setTag(position);
            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final CheckBox l = (CheckBox) v.findViewById(R.id.comenLikeButton);
                    final int p = Integer.parseInt(l.getTag().toString());
                    if (l.isChecked()){
                        try {
                            VKRequest request = new VKRequest("likes.add", VKParameters.from("type", "comment", "owner_id", comments.getJSONObject(p).getInt("from_id"), "item_id", comments.getJSONObject(p).getInt("id")));
                            request.executeWithListener(new VKRequest.VKRequestListener() {
                                @Override
                                public void onComplete(VKResponse response) {
                                    super.onComplete(response);
                                    l.setText(Integer.toString(Integer.parseInt(l.getText().toString()) + 1));
                                    try {
                                        comments.getJSONObject(p).getJSONObject("likes").put("user_likes", 1);
                                        comments.getJSONObject(p).getJSONObject("likes").put("count", comments.getJSONObject(p).getJSONObject("likes").getInt("count") + 1);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {
                        try {
                            VKRequest request = new VKRequest("likes.delete", VKParameters.from("type", "comment", "owner_id", comments.getJSONObject(p).getInt("from_id"), "item_id", comments.getJSONObject(p).getInt("id")));
                            request.executeWithListener(new VKRequest.VKRequestListener() {
                                @Override
                                public void onComplete(VKResponse response) {
                                    super.onComplete(response);
                                    l.setText(Integer.toString(Integer.parseInt(l.getText().toString()) - 1));
                                    try {
                                        comments.getJSONObject(p).getJSONObject("likes").put("user_likes", 0);
                                        comments.getJSONObject(p).getJSONObject("likes").put("count", comments.getJSONObject(p).getJSONObject("likes").getInt("count") - 1);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            boolean isPage = false;
            for (int i = 0; i < names.length(); i++){
                JSONObject name = names.getJSONObject(i);
                if (name.getInt("id") == coment.getInt("from_id")){
                    ImageView avatar = (ImageView) view.findViewById(R.id.avatarImg);
                    avatar.setClipToOutline(true);
                    nameComentator.setText(name.getString("first_name") + " " + name.getString("last_name"));
                    Picasso.with(ctx).load(name.getString("photo_100")).into(avatar);
                    isPage = true;
                }

            }
            if (!isPage){
                for(int i = 0; i < groups.length(); i++){
                    JSONObject group = groups.getJSONObject(i);
                    if (-1 * group.getInt("id") == coment.getInt("from_id")){
                        ImageView avatar = (ImageView) view.findViewById(R.id.avatarImg);
                        nameComentator.setText(group.getString("name"));
                        Picasso.with(ctx).load(group.getString("photo_100")).into(avatar);
                    }
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return view;
    }

    void updateArrays (JSONArray a, JSONArray n, JSONArray g, int c){
        for (int i = 0; i < a.length(); i++){
            try {
                comments.put(a.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < n.length(); i++){
            try {
                names.put(n.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < g.length(); i++){
            try {
                groups.put(g.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        count = count + c;

        notifyDataSetChanged();

    }

}
