package com.example.probuilder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;

public class ClientProjectsViewModel extends ViewModel {

    private final MutableLiveData<List<ClientProject>> projects = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<ClientProject>> getProjects() {
        return projects;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void setProjects(List<ClientProject> projectList) {
        projects.setValue(projectList);
        isLoading.setValue(false);
    }
    
    public void setLoading(boolean loading) {
        isLoading.setValue(loading);
    }

    public void setError(String errorMessage) {
        error.setValue(errorMessage);
        isLoading.setValue(false);
    }
    
    // Check if we already have data
    public boolean hasProjects() {
        return projects.getValue() != null && !projects.getValue().isEmpty();
    }
}
