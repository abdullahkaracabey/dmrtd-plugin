package tr.karacabey.dmrtd;

import static android.nfc.NfcAdapter.getDefaultAdapter;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.jmrtd.BACKey;
import org.jmrtd.BACKeySpec;
import org.jmrtd.lds.icao.MRZInfo;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.StandardMethodCodec;
import tr.karacabey.dmrtd.model.EDocument;
import tr.karacabey.dmrtd.model.ReadResult;
import tr.karacabey.dmrtd.model.TestModel;

/**
 * DmrtdPlugin
 */
public class DmrtdPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private Activity activity;

    private boolean isOnAction=false;
    private EDocument readDocument;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        BinaryMessenger messenger = flutterPluginBinding.getBinaryMessenger();
        BinaryMessenger.TaskQueue taskQueue = messenger.makeBackgroundTaskQueue();
        channel = new MethodChannel(messenger, "dmrtd", StandardMethodCodec.INSTANCE,
                taskQueue);
        channel.setMethodCallHandler(this);


    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

        if (activity == null) {
            result.error("500", "Cannot call method when not attached to activity", null);
            return;
        }

        if (call.method.equals("read")) {

            if(readDocument!=null){
                readDocument=null;
                isOnAction=false;
            }
            NfcAdapter nfcAdapter = getDefaultAdapter(activity);
            String mrzData = (String) call.arguments;
            MRZInfo mrzInfo = new MRZInfo(mrzData);

            String passportNumber = mrzInfo.getDocumentNumber();
            String expirationDate = mrzInfo.getDateOfExpiry();
            String birthDate = mrzInfo.getDateOfBirth();

            BACKeySpec bacKey = new BACKey(passportNumber, birthDate, expirationDate);
            //result.success(mrzInfo.getDocumentNumber());

            if (nfcAdapter==null || nfcAdapter.isEnabled() != true && call.method != "getNFCAvailability") {
                result.error("nfc-not-available", "NFC not available", null);
                return;
            }


            nfcAdapter.enableReaderMode(activity,tag -> readDocument(tag, bacKey, result), NfcAdapter.FLAG_READER_NFC_A |
                    NfcAdapter.FLAG_READER_NFC_B |
                    NfcAdapter.FLAG_READER_NFC_F |
                    NfcAdapter.FLAG_READER_NFC_V |
                    NfcAdapter.FLAG_READER_NFC_BARCODE |
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK  |
                    NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS , null);

//            nfcAdapter.enableReaderMode(activity,tag -> new ReadTask(result, IsoDep.get(tag), bacKey).execute(), NfcAdapter.FLAG_READER_NFC_A |
//                    NfcAdapter.FLAG_READER_NFC_B |
//                    NfcAdapter.FLAG_READER_NFC_F |
//                    NfcAdapter.FLAG_READER_NFC_V |
//                    NfcAdapter.FLAG_READER_NFC_BARCODE |
//                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK  |
//                    NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS , null);
            //new ReadTask(IsoDep.get(nfcAdapter.ta), bacKey).execute();

            // result.success("Android " + android.os.Build.VERSION.RELEASE);
        }
        else {
            result.notImplemented();
        }
    }

    private void  sendMessage(String message){

        activity.runOnUiThread(() -> {
            try{
            channel.invokeMethod("documentReadStatus", message);
            }
            catch(Exception e){
                Log.e("readDocument", "error on send status change");
            }
        });


    }
    private void readDocument(Tag tag,BACKeySpec bacKey, Result result){

        Log.i("readDocument", "triggered");
        if(isOnAction==true)return;

        if(readDocument!=null) return;
        isOnAction=true;
        sendMessage("started");

        new TaskRunner<ReadResult>().executeAsync(new DocumentReadTask(activity, IsoDep.get(tag), bacKey, status -> sendMessage(status)), (data) -> {


            activity.runOnUiThread(() -> {
                try{
                    if(data.isSuccess()){

                        result.success(data.getDocument().toMap());
                        Log.i("readDocument", "success");
                    }else{

                        result.error(   "document-read-error", data.getMessage(), null);
                        Log.i("readDocument", "error");
                    }
                }
                catch(Exception e){
                    Log.e("readDocument", "error on send status change");
                }
            });
            readDocument=null;
            isOnAction=false;
            Log.i("readDocument", "finished");
        });
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        if (activity != null) return;
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

        activity=null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {

    }




}
