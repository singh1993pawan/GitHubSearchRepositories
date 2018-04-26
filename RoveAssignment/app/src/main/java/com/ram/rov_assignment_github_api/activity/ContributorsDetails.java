package com.ram.rov_assignment_github_api.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ram.rov_assignment_github_api.R;
import com.ram.rov_assignment_github_api.adapters.RecyclerViewAdapter;
import com.ram.rov_assignment_github_api.adapters.RepoListAdapter;
import com.ram.rov_assignment_github_api.events.RepoListClickEvent;
import com.ram.rov_assignment_github_api.models.Repo;
import com.ram.rov_assignment_github_api.models.RepoDetail;
import com.ram.rov_assignment_github_api.utils.ApiService;
import com.ram.rov_assignment_github_api.utils.ConnectionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class ContributorsDetails extends AppCompatActivity {
    static final String TAG = ContributorsDetails.class.getSimpleName();
    ImageView avatarImg;
    TextView repoList;
    ConnectionManager connectionManager;
    RecyclerView repoList_LV;
    RepoListTask repoListTask;
    RecyclerViewAdapter repoListAdapter;
    String repo_List, profile_pic;
    ProgressBar pb;
    List<Repo> list;
    ApiService apiService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contributors_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Contributor");
        //toolbar.setSubtitle("Subtitle");
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );

        avatarImg = (ImageView) findViewById(R.id.imageView2);
        repoList = (TextView) findViewById(R.id.textView2);
        repoList_LV = (RecyclerView) findViewById(R.id.recycler_view);
        pb = (ProgressBar) findViewById(R.id.progressbar);
        pb.setVisibility(View.GONE);

        apiService = new ApiService();
        list = new ArrayList<Repo>();
        connectionManager = new ConnectionManager(ContributorsDetails.this);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this,1 );
        repoList_LV.setLayoutManager(mLayoutManager);
        repoList_LV.setItemAnimator(new DefaultItemAnimator());

        Intent dataIntent = getIntent();
        profile_pic = dataIntent.getStringExtra("AVATAR_IMG");
        repo_List = dataIntent.getStringExtra("REPO_LIST");

        Glide.with(ContributorsDetails.this).load(profile_pic).into(avatarImg);

        if(connectionManager.isNetworkAvailable()){
            Log.d(TAG,"Repo List URL == "+repo_List);
            repoListTask = new RepoListTask();
            repoListTask.execute();
        } else {
            Log.d(TAG,"Check your NetWork Connection");
            Toast.makeText(ContributorsDetails.this, "Check your NetWork Connection", Toast.LENGTH_SHORT).show();
        }
    }

    private class RepoListTask extends AsyncTask<String, String, String> {
        String responseStr;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
        }
        @Override
        protected String doInBackground(String... f_url) {
            try {
                if(repo_List.length() > 0 && repo_List != null){
                    responseStr = apiService.post(repo_List);
                    list.clear();
                    list = new ArrayList<Repo>();
                    JSONArray jsonArray = new JSONArray(responseStr);
                    if(jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject itemObj = jsonArray.getJSONObject(i);
                            String name = itemObj.getString("name");
                            String fullName = itemObj.getString("full_name");
                            String projectLink = itemObj.getString("html_url");
                            String contributorsUrl = itemObj.getString("contributors_url");
                            String description = itemObj.getString("description");
                            int watchersCount = itemObj.getInt("watchers_count");
                            JSONObject ownerObj = itemObj.getJSONObject("owner");
                            String avatarImg = ownerObj.getString("avatar_url");

                            Repo repo = new Repo();

                            repo.setName(name);
                            repo.setFull_name(fullName);
                            repo.setProjectLink(projectLink);
                            repo.setContributorsUrl(contributorsUrl);
                            repo.setDescription(description);
                            repo.setWatchers_count(watchersCount);
                            repo.setAvatar_img(avatarImg);
                            list.add(repo);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            if(list.size() > 0){
                repoListAdapter = new RecyclerViewAdapter(ContributorsDetails.this,list);
                repoList_LV.setAdapter(repoListAdapter);
                repoListAdapter.notifyDataSetChanged();
            }
            pb.setVisibility(View.GONE);
        }
    }

    public void onEventMainThread(RepoListClickEvent event) {
        Log.d(TAG,"RepoListClickEvent ==");
        //Toast.makeText(ContributorsDetails.this, "Repo position === "+event.getPosition(), Toast.LENGTH_SHORT).show();

        Intent repoDetails = new Intent(ContributorsDetails.this, RepoDetailsActivity.class);
        repoDetails.putExtra("AVATAR_IMG",list.get(event.getPosition()).getAvatar_img());
        repoDetails.putExtra("NAME",list.get(event.getPosition()).getName());
        repoDetails.putExtra("PROJECT_LINK",list.get(event.getPosition()).getProjectLink());
        repoDetails.putExtra("DESCRIPTION",list.get(event.getPosition()).getDescription());
        repoDetails.putExtra("CONTRIBUTORS_URL",list.get(event.getPosition()).getContributorsUrl());
        startActivity(repoDetails);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!EventBus.getDefault().isRegistered(ContributorsDetails.this)){
            EventBus.getDefault().register(ContributorsDetails.this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(ContributorsDetails.this)){
            EventBus.getDefault().unregister(ContributorsDetails.this);
        }
    }

}
