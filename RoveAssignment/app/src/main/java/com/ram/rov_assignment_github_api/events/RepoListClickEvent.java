package com.ram.rov_assignment_github_api.events;

/**
 * Created by rmreddy on 19/11/17.
 */

public class RepoListClickEvent {
    private int position;

    public RepoListClickEvent(int position){
        this.position = position;
    }

    public int getPosition(){
        return position;
    }
}
