package com.dabloons.wattsapp.manager;

import com.dabloons.wattsapp.model.Scene;
import com.dabloons.wattsapp.model.integration.IntegrationScene;
import com.dabloons.wattsapp.repository.SceneRepository;
import com.dabloons.wattsapp.repository.UserRepository;

import java.util.List;

import util.WattsCallback;
import util.WattsCallbackStatus;

public class SceneManager {
    private static volatile  SceneManager instance;

    private SceneRepository sceneRepository = SceneRepository.getInstance();

    private  UserManager userManager = UserManager.getInstance();

    public SceneManager() {
    }

    public void createScene(String roomID, String sceneName, List<IntegrationScene> sceneList, WattsCallback<Scene, Void> callback)
    {
        sceneRepository.createScene(roomID, sceneName, sceneList, callback);
    }

    public void getAllScenes(String roomID, WattsCallback<List<Scene>, Void> callback)
    {
        sceneRepository.getAllScenes(roomID, callback);
    }

    public void deleteUserScenes(WattsCallback<Void, Void> callback) {
        sceneRepository.deleteScenesForUser(callback);
    }

    public static SceneManager getInstance() {
        SceneManager result = instance;
        if (result != null) {
            return result;
        }
        synchronized(SceneManager.class) {
            if (instance == null) {
                instance = new SceneManager();
            }
            return instance;
        }
    }
}
