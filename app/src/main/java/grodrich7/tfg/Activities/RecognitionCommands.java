package grodrich7.tfg.Activities;

import android.Manifest;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Locale;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import grodrich7.tfg.Controller.Controller;
import grodrich7.tfg.R;

import static grodrich7.tfg.Models.Constants.LANGUAGES_AVAILABLES;

public class RecognitionCommands implements RecognitionListener {
    private DrivingActivity drivingActivity;
    private static final String MENU_SEARCH = "menu";


    private SpeechRecognizer recognizer;
    private Controller controller;
    private String destinationCommand;
    private String parkingCommand;
    private String callsCommand;
    private String callToCommand;
    private String messageCommand;

    public RecognitionCommands(DrivingActivity drivingActivity){
        this.drivingActivity = drivingActivity;
        this.controller = Controller.getInstance();
        this.destinationCommand = drivingActivity.getString(R.string.destinationCommand);
        this.parkingCommand = drivingActivity.getString(R.string.parkingCommand);
        this.callsCommand = drivingActivity.getString(R.string.callsCommand);
        this.callToCommand = drivingActivity.getString(R.string.callToCommand);
        this.messageCommand = drivingActivity.getString(R.string.messageCommand);
    }

    public void startListening(){
        int audio = ContextCompat.checkSelfPermission(drivingActivity.getApplicationContext(),
                Manifest.permission.RECORD_AUDIO);
        boolean notifications = PreferenceManager.getDefaultSharedPreferences(drivingActivity.getApplicationContext())
                .getBoolean("voice_recognition", false);
        if (audio == 0 && notifications){
            new SetupTask(drivingActivity).execute();
        }
    }

    private class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<DrivingActivity> activityReference;
        SetupTask(DrivingActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }
        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                setupRecognizer(assetDir);
            } catch (IOException e) {
                return e;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Exception result) {
            if (result != null) {
                Toast.makeText(drivingActivity,"Failed to init recognizer " + result, Toast.LENGTH_LONG).show();
                Log.d("RECOGNITION", "Failed to init recognizer " + result.getMessage());
            } else {
                Log.d("RECOGNITION", "GOOD POST EXECUTE ");
                switchSearch(MENU_SEARCH);
            }
        }
    }

    private void switchSearch(String searchName) {
        recognizer.stop();
        recognizer.startListening(searchName);

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(this.destinationCommand)) {
            recognizer.startListening(searchName, 10000);
        }
        else
            recognizer.startListening(searchName);
    }

    public void pauseListening(){
        recognizer.stop();
    }

    public void restartListening(){
        recognizer.startListening(MENU_SEARCH);
    }


    private void setupRecognizer(File assetsDir) throws IOException {
        String locale = Locale.getDefault().getLanguage();
        if (!LANGUAGES_AVAILABLES.contains(locale)){
            locale = "en";
        }
        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, locale + "-ptm"))
                .setDictionary(new File(assetsDir, locale + ".dict"))
                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .getRecognizer();
        recognizer.addListener(this);

        File menuGrammar = new File(assetsDir, "menu-" + locale + ".gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
    }

    public void stopListening(){
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {
        //if (!recognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(MENU_SEARCH);
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        if (text.equals(callsCommand)){
            switchSearch(MENU_SEARCH);
            controller.updateAcceptCalls(controller.getDrivingData().isAcceptCalls() != null ?!controller.getDrivingData().isAcceptCalls() : true);
            drivingActivity.toggleCallIcon();
        }else if (text.equals(parkingCommand)){
            switchSearch(MENU_SEARCH);
            controller.updateParking(controller.getDrivingData().isSearchingParking() != null ?!controller.getDrivingData().isSearchingParking() : true);
            drivingActivity.toggleParkingIcon();
        }else if (text.equals(destinationCommand)){
            pauseListening();
            drivingActivity.startVoiceToTextService(1);
        }else if (text.equals(callToCommand)){
            drivingActivity.startVoiceToTextService(2);
        }else if (text.equals(messageCommand)){
            drivingActivity.startVoiceToTextService(3);
        }

        Log.d("RECOGNITION", text + "onPartialResult");
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        Log.d("RECOGNITION", "onResult");
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            Log.d("RECOGNITION", text);
            Toast.makeText(drivingActivity,text, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onError(Exception e) {
        //Toast.makeText(drivingActivity,e.getMessage(), Toast.LENGTH_LONG).show();
        Log.d("RECOGNITION", "onError");
    }

    @Override
    public void onTimeout() {
        switchSearch(MENU_SEARCH);
        Log.d("RECOGNITION", "onTimeout");
    }
}
