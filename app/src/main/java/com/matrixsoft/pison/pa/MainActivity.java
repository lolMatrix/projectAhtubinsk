package com.matrixsoft.pison.pa;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.methods.VKApiGroups;
import com.vk.sdk.api.methods.VKApiWall;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.util.VKUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    String[] scope = new String[] {VKScope.GROUPS, VKScope.WALL};
    final int articles_count = 10;
    int articles_page = 0;
    ListView l;
    ArticleAdapter a;
    int fvi = 0;
    FloatingActionButton fab;
    int vic = 0;
    int id_group;
    String aboutGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VKSdk.initialize(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newPost = new Intent(MainActivity.this, NewPostActivity.class);
                newPost.putExtra("id", id_group);
                startActivity(newPost);
            }
        });
        getArticles();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_refresh:
                getArticles();
                break;
            case R.id.abouMe:
                Intent aboutMe = new Intent(MainActivity.this, AboutMeActivity.class);
                aboutMe.putExtra("text", aboutGroup);
                startActivity(aboutMe);
                break;
            case R.id.logout_action:
                VKSdk.logout();
                finish();
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                getArticles();
            }
            @Override
            public void onError(VKError error) {
// Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                Toast.makeText(MainActivity.this, "Ошибка авторизации, повторитете позже", Toast.LENGTH_SHORT).show();
                finish();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    void getArticles () {
        articles_page = 0;
        if(VKSdk.isLoggedIn()) {
            VKRequest vkRequest = new VKApiGroups().getById(VKParameters.from("group_ids", "loveaht", "fields", "description"));
            vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    VKList vkList = (VKList) response.parsedModel;
                    try {
                        id_group = vkList.get(0).fields.getInt("id");
                        aboutGroup = vkList.get(0).fields.get("description").toString();
                        VKRequest vkRequest1 = VKApi.wall().get(VKParameters.from(VKApiConst.OWNER_ID, "-"+vkList.get(0).fields.getInt("id"), "count", articles_count, "offset", articles_page));
                        vkRequest1.executeWithListener(new VKRequest.VKRequestListener() {
                            @Override
                            public void onComplete(VKResponse response) {
                                super.onComplete(response);
                                try {
                                    JSONObject j = response.json.getJSONObject("response");
                                    l = (ListView) findViewById(R.id.articlesView);
                                    a = new ArticleAdapter(MainActivity.this, j.getJSONArray("items"), articles_count + articles_page);

                                    l.setAdapter(a);

                                    l.setOnScrollListener(new AbsListView.OnScrollListener() {
                                        @Override
                                        public void onScrollStateChanged(AbsListView view, int scrollState) {
                                            if (scrollState == 0 || scrollState == 2 || scrollState == 1){

                                                if (vic + fvi == articles_page + articles_count){
                                                    articles_page = articles_page + articles_count;
                                                    updateArticle();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                                            fvi = firstVisibleItem;
                                            vic = visibleItemCount;
                                            if(firstVisibleItem == 0){
                                                fab.show();
                                            }else {
                                                fab.hide();
                                            }
                                        }

                                    });

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    super.onComplete(response);
                }
            });
        }else VKSdk.login(this, scope);
    }

    void updateArticle(){

        if(VKSdk.isLoggedIn()) {
            VKRequest vkRequest = new VKApiGroups().getById(VKParameters.from("group_ids", "loveaht"));
            vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    VKList vkList = (VKList) response.parsedModel;
                    try {
                        VKRequest vkRequest1 = VKApi.wall().get(VKParameters.from(VKApiConst.OWNER_ID, "-"+vkList.get(0).fields.getInt("id"), "offset", articles_page, "count", articles_count));
                        vkRequest1.executeWithListener(new VKRequest.VKRequestListener() {
                            @Override
                            public void onComplete(VKResponse response) {
                                super.onComplete(response);
                                try {
                                    JSONObject j = response.json.getJSONObject("response");

                                    a.updateArray(j.getJSONArray("items"), articles_count + articles_page);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    super.onComplete(response);
                }
            });
        }else VKSdk.login(this, scope);

    }
    
}
