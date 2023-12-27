package co.vuno.app.hativ.librarytest;

import android.Manifest;
import android.accounts.NetworkErrorException;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import co.vuno.app.hativ.hativlibrary.HativEcgMeasureManager;
import co.vuno.app.hativ.hativlibrary.HativEcgMeasureOptions;
import co.vuno.app.hativ.hativlibrary.model.ecg.EcgResult;
import co.vuno.app.hativ.hativlibrary.model.ecg.enumeration.AnalysisStatus;
import co.vuno.app.hativ.hativlibrary.model.ecg.enumeration.ErrorCode;
import co.vuno.app.hativ.hativlibrary.model.ecg.enumeration.MeasureMode;
import co.vuno.app.hativ.hativlibrary.util.bluetooth.HativPermissionException;
import co.vuno.app.hativ.hativlibrary.util.bluetooth.enumeration.ConnectStatus;
import co.vuno.app.hativ.hativlibrary.util.bluetooth.enumeration.MeasureStatus;
import co.vuno.app.hativ.hativlibrary.util.bluetooth.listener.HativStatusListener;
import co.vuno.app.hativ.hativlibrarytest.R;

public class MainActivity extends AppCompatActivity {

    HativEcgMeasureManager manager;
    HativEcgMeasureOptions options;
    String hativToken = "";

    private final String[] permissions = new String[]{
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();
    }

    private boolean checkPermissions() {
        List<String> deniedList = new ArrayList<>();
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                deniedList.add(p);
            }
        }
        if (deniedList.isEmpty()) {
            initBluetoothInstance(hativToken);
            return true;
        }else {
            return false;
        }
    }

    private void initBluetoothInstance(String token) {
        try {
            manager = HativEcgMeasureManager.getInstance(getApplicationContext(), token);

            //측정방법 설정, event-listener 등록.
            //연결된 기기에 대한 응답은 HativStatusListener로 전달받음.
            options = new HativEcgMeasureOptions(MeasureMode.LIMB, new HativStatusListener() {
                @Override
                public void onErrorEvent(ErrorCode error) {
                    System.out.println("[onErrorEvent]"+error.name());
                }

                @Override
                public void onMeasureEvent(MeasureStatus status, int secUntilFinished) {
                    System.out.println("[onMeasureEvent]"+status.name() + " : " + secUntilFinished);
                }

                @Override
                public void onConnectEvent(ConnectStatus status) {
                    System.out.println("[onConnectEvent]" + status.name());
                }

                @Override
                public void onMeasureResult(AnalysisStatus status, EcgResult result) {
                    if (result != null) {
                        System.out.println("[onMeasureResult]" + result.rhythm);
                    }else {
                        System.out.println("[onMeasureResult]" + status);
                    }
                }
            });

            //P30 디바이스를 검색 및 연결.
            manager.connectDevice(options);
        } catch (HativPermissionException e) {
            throw new RuntimeException(e);
        } catch (NetworkErrorException e) {
            throw new RuntimeException(e);
        }
    }
}