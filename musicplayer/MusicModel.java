package musicplayer;

import java.util.LinkedHashMap;

import javafx.beans.property.*;
import javafx.scene.media.*;
import javafx.util.Duration;

public class MusicModel {
	private LinkedHashMap<String, MediaPlayer> mediaMap; //same order
	private MediaView currentView;
	private int songIndex = 0;
	private StringProperty currSongName = new SimpleStringProperty();
	private BooleanProperty playing = new SimpleBooleanProperty(false);
	private BooleanProperty muted = new SimpleBooleanProperty(false);
	private DoubleProperty volume = new SimpleDoubleProperty(0.7);
	private BooleanProperty hiddenMode = new SimpleBooleanProperty(false);
	private DoubleProperty musicDuration = new SimpleDoubleProperty();
	
	public MusicModel() {
		mediaMap  = new LinkedHashMap<>();
		addSong("A Fight Away - Censation", "a_fight_away.wav", false);
		addSong("Perserverence - Censation", "perserverence.wav", false);
		setCurrentView("A Fight Away - Censation"); //default song
	}
	public void addSong(String title, String fileName, boolean external) {
		if(mediaMap.containsKey(title))return; //already have this song :[
		Media media = new Media(external ? fileName : getClass().getResource(fileName).toExternalForm());
		MediaPlayer mediaPlayer = new MediaPlayer(media);
		mediaPlayer.volumeProperty().bindBidirectional(volume); //each player wil
		mediaPlayer.setVolume(0.7); //default		
		mediaPlayer.setOnEndOfMedia(()-> {
			mediaPlayer.seek(mediaPlayer.getStartTime());
		});
		mediaMap.put(title, mediaPlayer);
	}

	public void setCurrentView(String title) {
		MediaPlayer mp = mediaMap.get(title);
		currentView = new MediaView(mp);
		currSongName.set(title);
		mp.setVolume(volume.get());	
		mp.setOnReady(()->musicDuration.set(mp.getMedia().getDuration().toMillis())); //first load
		
		musicDuration.set(mp.getMedia().getDuration().toMillis()); //after first time
	}
	public MediaView getMediaView(String name) {
		setCurrentView(name);
		return currentView;
	}
	public MediaView getMediaView(int index) { //overload
		songIndex = index % mediaMap.size(); //mod size so will automatically go back to 0 when index reaches size
		if(songIndex==-1)songIndex=mediaMap.size()-1; //when index is below 0, go to size
		//manual get index
		//MediaPlayer fetch = null;
		int i =-1;
		for(String songName: mediaMap.keySet()) {
			if(++i==songIndex) {
				setCurrentView(songName);
				return currentView;
				//fetch = song;
			}
		}
		
		//currentView.setMediaPlayer(fetch);
		return currentView; //will never reach
	}
	
	public MediaView getMediaView() {return currentView;}
	public int getSongIndex() {return songIndex;}
	public DoubleProperty musicDurationProperty() {return musicDuration;}
	public BooleanProperty playingProperty() {return playing;}
	public StringProperty currSongNameProperty() {return currSongName;}
	public BooleanProperty mutedProperty() {return muted;}
	public DoubleProperty volumeProperty() {return volume;}
	public BooleanProperty hiddenModeProperty() {return hiddenMode;}
}
