package musicplayer;

import java.io.File;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXToggleButton;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import de.jensd.fx.glyphs.octicons.OctIconView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class MusicController {
	private MusicModel model = new MusicModel();
	private Stage stage;
	private InvalidationListener progressListener;
	private double xOffset=0, yOffset = 0;
	private Timeline t; //for rewind and fast forward
	private Timeline songNameScroll; //mini pane marquee
	private double multiplier = 1;
	@FXML private AnchorPane mediaPane, miniPane, topPane, rootPane, dropPane, miniDropPane;
	@FXML private MediaView musicViewer;
	@FXML private MaterialDesignIconView mainPlay, next, previous;
	@FXML private Slider videoSlider;
	@FXML private JFXProgressBar mainProgress;
	@FXML private JFXSlider volumeSlider;
	@FXML private JFXButton muteBtn, playBtn, nextBtn, previousBtn, rewindBtn, forwardBtn, closeBtn, minimiseBtn;
	@FXML private OctIconView muteIcon;
	
	@FXML private ImageView miniTop;
	@FXML private MaterialDesignIconView miniPlay, miniNext, miniPrev;
	@FXML private JFXProgressBar miniProgress;
	@FXML private Label miniSongName;
	@FXML private JFXToggleButton hideMain;
	@FXML private JFXButton miniPlayBtn, miniPrevBtn, miniNextBtn, miniCloseBtn;
	@FXML private ScrollPane miniScrollPane;
	
	
	
	
	
	
	
	
	
	public void initialize() {
		//musicViewer.fitWidthProperty().bind(mediaPane.widthProperty());
		//musicViewer.fitHeightProperty().bind(mediaPane.heightProperty());
		//musicViewer.mediaPlayerProperty().bind(model.getMediaView().mediaPlayerProperty()); //if model mediaplayer changes, so will view
		progressListener = new InvalidationListener() {
			public void invalidated(Observable o) {
				updateValues();
			}
		};
		
		
		Platform.runLater(()-> {
			videoSlider.setValue(0);
			muteBtn.setText("");
			hideMain.setText("");
			previousBtn.setText("");
			rewindBtn.setText("");
			playBtn.setText("");
			forwardBtn.setText("");
			nextBtn.setText("");
			miniPlayBtn.setText("");
			miniPrevBtn.setText("");
			miniNextBtn.setText("");
			closeBtn.setText("");
			minimiseBtn.setText("");
			miniCloseBtn.setText("");
			miniSongName.setText(model.currSongNameProperty().get());
		});
	
		//initial initialisation
		musicViewer.setMediaPlayer(model.getMediaView().getMediaPlayer());				
		
		
		model.playingProperty().addListener((observable, oldValue, newValue) -> {
			Status status = model.getMediaView().getMediaPlayer().getStatus();
			if(status== Status.UNKNOWN||status==Status.HALTED) {
				model.playingProperty().set(oldValue);
				return; //do nothing in these states: not loaded/error
			}
			if(newValue) {//is playing
				model.getMediaView().getMediaPlayer().play();
				miniPlay.setGlyphName("PAUSE");
				mainPlay.setGlyphName("PAUSE");
				
			} else {
				musicViewer.getMediaPlayer().pause();
				miniPlay.setGlyphName("PLAY");
				mainPlay.setGlyphName("PLAY");
			}

		});
		
		musicViewer.getMediaPlayer().currentTimeProperty().addListener(progressListener);
		videoSlider.valueProperty().addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {
				if(videoSlider.isValueChanging()) {
					mainProgress.setProgress(videoSlider.getValue()==100.0?0:videoSlider.getValue()/100.0); //move the black fill along with slider
					model.getMediaView().getMediaPlayer().seek(Duration.millis(model.musicDurationProperty().get()).multiply(videoSlider.getValue()/100.0));

				}
			}
		});
		miniProgress.progressProperty().bindBidirectional(mainProgress.progressProperty());
		
		
		volumeSlider.valueProperty().addListener(o -> {
			if(volumeSlider.isValueChanging()) {
				model.volumeProperty().set(volumeSlider.getValue()/100.0);
			}
		});
		
		//musicViewer.getMediaPlayer().volumeProperty().bind(model.getMediaView().getMediaPlayer().volumeProperty());
		model.mutedProperty().addListener((obs, ov, nv)-> {
			musicViewer.getMediaPlayer().setMute(nv);
			muteIcon.setGlyphName(nv ? "MUTE": "UNMUTE");
		});
		
		model.currSongNameProperty().addListener((obs, ov, nv) -> { //doesn't change at initial name
			miniSongName.setText(nv);
		});
		
		songNameScroll = new Timeline(new KeyFrame(Duration.millis(300), ae -> marquee()));
		songNameScroll.setCycleCount(Timeline.INDEFINITE);
		
		model.hiddenModeProperty().addListener((obs,ov,nv) -> {
			if(nv) { //if party mode
				miniPane.setBackground(new Background(new BackgroundFill(Color.rgb(0,0,0,0.5), new CornerRadii(10), null)));
				miniPlay.setFill(Color.WHITE);
				miniNext.setFill(Color.WHITE);
				miniPrev.setFill(Color.WHITE);
			} else {
				miniPane.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), null)));
				miniPlay.setFill(Color.BLACK);
				miniNext.setFill(Color.BLACK);
				miniPrev.setFill(Color.BLACK);
			}
			
		});
		
		model.musicDurationProperty().addListener(o->updateValues()); //when song changes
	
	}
	public void updateValues() {
		Duration musicDuration = Duration.millis(model.musicDurationProperty().get());
		Duration currentDuration = musicViewer.getMediaPlayer().getCurrentTime(); //don't use model as the duration does not change - song is played from here
		videoSlider.setDisable(musicDuration.isUnknown());
		if(!videoSlider.isDisabled() && musicDuration.greaterThan(Duration.ZERO)&&!videoSlider.isValueChanging()) {
			double decimal = currentDuration.toMillis()/musicDuration.toMillis();
			mainProgress.setProgress(decimal);
			videoSlider.setValue(decimal*100.0);
		}
	}
	public void marquee() {
		miniSongName.setText(miniSongName.getText().substring(1));
		if(miniSongName.getText().isEmpty()) {
			miniSongName.setText(model.currSongNameProperty().get());
		}
	}
	
	@FXML
	public void muteMusic(ActionEvent e) {
		model.mutedProperty().set(!model.mutedProperty().get());
	}
	
	
	@FXML
	public void playPauseMusic(ActionEvent e) {
		if(mainPlay.getGlyphName().equals("PLAY"))play();
		else pause(); //can only be play or pause
	}
	public void pause() {
		model.playingProperty().set(false);
	}
	public void play() {
		model.playingProperty().set(true);
	}	
	@FXML
	public void changeMusic(ActionEvent e) {
		boolean nextMusic = e.getSource().equals(nextBtn) ||e.getSource().equals(miniNextBtn); //true = next music, false = back music
		
		boolean playing = previousChange();
		musicViewer = model.getMediaView(model.getSongIndex() + (nextMusic? 1: -1));
		
		currentChange(playing);
	}
	
	public boolean previousChange() {
		boolean playing = false;
		if(musicViewer.getMediaPlayer().getStatus()==MediaPlayer.Status.PLAYING){ //pause old song, play new song
			pause();
			playing = true;
		}
		model.getMediaView().getMediaPlayer().currentTimeProperty().removeListener(progressListener); //no-op if first time
		return playing;
	}
	public void currentChange(boolean playing) {
		MediaPlayer mp = model.getMediaView().getMediaPlayer();
		//if(mp.getStatus()!=MediaPlayer.Status.UNKNOWN) {  -will always be loaded beforehand; initial songs or setOnReady dragndrop
			mp.currentTimeProperty().addListener(progressListener);
		//}
		//updateValues(); //change values to new mediaPlayer so progress starts at the values of the new media	- binded
		if(playing) //play new music if old was still playing
				play();
		
	}
	@FXML
	public void rewindStart(MouseEvent e) {
		t = new Timeline(new KeyFrame(Duration.millis(10), ae-> { //update every centisecond
			model.getMediaView().getMediaPlayer().seek(model.getMediaView().getMediaPlayer().getCurrentTime().subtract(Duration.millis(500*multiplier)));
			multiplier+=0.05;
		}));
		t.setCycleCount(Timeline.INDEFINITE);
		t.play();
	}
	
	@FXML
	public void forwardStart(MouseEvent e) { //same as above but more verbose for clarity
		MediaPlayer mp = model.getMediaView().getMediaPlayer();
		
		t = new Timeline();
		t.setCycleCount(Timeline.INDEFINITE);
		KeyFrame kf = new KeyFrame(Duration.millis(10), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				mp.seek(mp.getCurrentTime().add(Duration.millis(500*multiplier)));
				multiplier += 0.05;
			}
		});
		t.getKeyFrames().add(kf);
		t.play();
	}
	@FXML
	public void stopRWFF(MouseEvent e) {
		t.stop();
		multiplier = 1;
	}
	
	@FXML
	public void dragOver(DragEvent e) {
		Dragboard db = e.getDragboard();
		if(db.hasFiles()) {
			boolean main = e.getSource().equals(mediaPane);
			dropPane.setVisible(main);
			miniDropPane.setVisible(!main);
			e.acceptTransferModes(TransferMode.COPY);
		} else {
			e.consume();
		}
	}
	@FXML
	public void dragDropped(DragEvent e) {
		Dragboard db = e.getDragboard();
		boolean success = false;
		ObservableList<String> unaccepted = FXCollections.observableArrayList();
		ObservableList<File> accepted = FXCollections.observableArrayList();
		if(db.hasFiles()) {
			success = true;
			for(File file: db.getFiles()) {
				String name = file.getName();
				if(name.toLowerCase().endsWith(".wav") || name.toLowerCase().endsWith(".mp3")||name.toLowerCase().endsWith(".mp4")||name.toLowerCase().endsWith(".flv")||name.toLowerCase().endsWith(".aiff")) 
					accepted.add(file);
				else
					unaccepted.add(name);
			}
			if(unaccepted.size()>0) {
				String names = "";
				for(String name: unaccepted) {
					names += name + ", ";
				}
				ButtonType customCancel = new ButtonType("go away", ButtonData.CANCEL_CLOSE);
				Alert alert = new Alert(AlertType.INFORMATION, "Files: "+names+"are not supported but the others will be added to the playlist!", ButtonType.OK, customCancel);		
				alert.setTitle("Some bad files");
				alert.initStyle(StageStyle.UTILITY);
				alert.setHeaderText("You can't trick me");
				alert.showAndWait();
			}
			for(File file: accepted) {
				model.addSong(file.getName().substring(0, file.getName().lastIndexOf('.')), file.toURI().toString(), true);
			}
			if(!accepted.isEmpty()) { //auto change to new added music
				
				String firstName = accepted.get(0).getName();
				boolean playing = previousChange();
				musicViewer = model.getMediaView(firstName.substring(0, firstName.lastIndexOf('.')));
				MediaPlayer mp = model.getMediaView().getMediaPlayer();
				System.out.println("1 "+mp.getStatus());
				
				if(model.getMediaView().getMediaPlayer().getStatus()==MediaPlayer.Status.UNKNOWN) {
						model.getMediaView().getMediaPlayer().setOnReady(()->{
							System.out.println("loaded and ready");
							model.musicDurationProperty().set(model.getMediaView().getMediaPlayer().getMedia().getDuration().toMillis());
							currentChange(playing);
							
						});
				} else { //if file was already loaded before
						currentChange(playing);
				}
				
			}
		}
		System.out.println(e.getSource().toString());
		dropPane.setVisible(false);
		miniDropPane.setVisible(false);
		e.setDropCompleted(success);
		e.consume();
	}
	@FXML public void dragExited(DragEvent e) {
		dropPane.setVisible(false);
		miniDropPane.setVisible(false);
	}
	@FXML	
	public void topPressed(MouseEvent e) {
		xOffset = e.getSceneX();
		yOffset = e.getSceneY();
	}
	@FXML
	public void topDragged(MouseEvent e) {
		if(stage==null)stage = (Stage) ((Node)e.getSource()).getScene().getWindow();
		stage.setX(e.getScreenX() - xOffset);
		stage.setY(e.getScreenY() - yOffset);
	}
	@FXML
	public void hidePlayer(ActionEvent e) {
		model.hiddenModeProperty().set(!model.hiddenModeProperty().get());
		mediaPane.setVisible(!mediaPane.isVisible());
	}
	@FXML
	public void startMarquee(MouseEvent e) {
		songNameScroll.play();
	}
	@FXML
	public void stopMarquee(MouseEvent e) {
		miniSongName.setText(model.currSongNameProperty().get());
		songNameScroll.stop();
	}
	@FXML
	public void winMinimize(ActionEvent e) {
		((Stage)((Node)e.getSource()).getScene().getWindow()).setIconified(true);
	}
	@FXML
	public void winClose(ActionEvent e) {
		Platform.exit();
	}
	
}
