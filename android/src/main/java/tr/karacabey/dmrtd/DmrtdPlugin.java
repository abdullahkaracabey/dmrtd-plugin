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


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
        channel = new MethodChannel(messenger, "dmrtd_plugin", StandardMethodCodec.INSTANCE,
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
            Log.i("NFC_MRZ_INPUT", "Received MRZ from Flutter: " + mrzData);
            mrzData = mrzData.replaceAll("-", "");
            Log.i("NFC_MRZ_INPUT", "MRZ after removing dashes: " + mrzData);
            MRZInfo mrzInfo = new MRZInfo(mrzData);

            String passportNumber = mrzInfo.getDocumentNumber();
            String expirationDate = mrzInfo.getDateOfExpiry();
            String birthDate = mrzInfo.getDateOfBirth();

            Log.i("NFC_BAC_KEY", "Creating BAC key from MRZ:");
            Log.i("NFC_BAC_KEY", "  Passport Number (raw): '" + passportNumber + "' (length: " + passportNumber.length() + ")");
            Log.i("NFC_BAC_KEY", "  Birth Date (raw): " + birthDate);
            Log.i("NFC_BAC_KEY", "  Expiry Date (raw): " + expirationDate);
            
            // Passport number should be exactly 9 characters, padded with '<' if needed
            // But JMRTD MRZInfo.getDocumentNumber() should already handle this
            // Let's verify the format
            if (passportNumber.length() < 9) {
                Log.w("NFC_BAC_KEY", "Warning: Passport number is shorter than 9 characters, padding with '<'");
                while (passportNumber.length() < 9) {
                    passportNumber = passportNumber + "<";
                }
            } else if (passportNumber.length() > 9) {
                Log.w("NFC_BAC_KEY", "Warning: Passport number is longer than 9 characters, truncating");
                passportNumber = passportNumber.substring(0, 9);
            }
            
            Log.i("NFC_BAC_KEY", "  Passport Number (after padding): '" + passportNumber + "' (length: " + passportNumber.length() + ")");

            // Format dates to ensure correct yyMMdd format for BAC
            birthDate = formatDateForBAC(birthDate);
            expirationDate = formatDateForBAC(expirationDate);

            Log.i("NFC_BAC_KEY", "  Birth Date (formatted): " + birthDate);
            Log.i("NFC_BAC_KEY", "  Expiry Date (formatted): " + expirationDate);
            
            // Log the concatenated string that will be used for BAC key generation
            String bacInput = passportNumber + birthDate + expirationDate;
            Log.i("NFC_BAC_KEY", "BAC Key Input String: '" + bacInput + "'");
            Log.i("NFC_BAC_KEY", "BAC Key Input Length: " + bacInput.length());

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

    /**
     * Format date from MRZ format (YYMMDD) to ensure correct format for BAC
     * MRZInfo returns dates as strings, we need to ensure they are in yyMMdd format
     */
    private String formatDateForBAC(String mrzDate) {
        if (mrzDate == null || mrzDate.length() != 6) {
            Log.w("NFC_BAC_KEY", "Invalid date format: " + mrzDate);
            return mrzDate; // Return as-is if invalid
        }
        
        // MRZ dates are already in YYMMDD format, but let's ensure it's properly formatted
        // by parsing and reformatting
        try {
            SimpleDateFormat mrzFormat = new SimpleDateFormat("yyMMdd", Locale.US);
            mrzFormat.setLenient(false);
            Date date = mrzFormat.parse(mrzDate);
            
            // Format back to yyMMdd to ensure consistency
            String formatted = mrzFormat.format(date);
            Log.i("NFC_BAC_KEY", "Date formatted: " + mrzDate + " -> " + formatted);
            return formatted;
        } catch (ParseException e) {
            Log.w("NFC_BAC_KEY", "Failed to parse date: " + mrzDate + ", using as-is");
            return mrzDate; // Return original if parsing fails
        }
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
