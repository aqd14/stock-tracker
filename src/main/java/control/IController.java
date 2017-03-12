package main.java.control;

import main.java.utility.Screen;

public interface IController {
	/**
	 * <p>
	 * Switch from current scene to target scene. The primary stage will be remained, only 
	 * load new scene.
	 * </p>
	 * @param target	Target screen
	 * @param title		Title of target screen
	 * @param url		<code>URL</code> to fxml file of target screen
	 */
    public abstract void switchScreen(Screen target, String title, String url);
    
	/**
	 * Create new stage besides primary one. That means there are more than one views displayed 
	 * on the screen.
	 * 
	 * @param target The view that user wants to switch to
	 * @param stageTitle The title of created stage
	 * @param url <code>URL</code> to FXML file
	 */
	public abstract void makeNewStage(Screen target, String title, String url);
}
