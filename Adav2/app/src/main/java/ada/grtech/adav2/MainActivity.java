package ada.grtech.adav2;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.speech.tts.Voice;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.content.Context;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;

import java.lang.Object;

import org.w3c.dom.Text;

import android.speech.tts.*;
import android.telephony.SmsMessage;

import java.util.Locale;
import java.util.Calendar;
import java.util.jar.Manifest;

import android.app.*;
import android.app.PendingIntent;
import android.widget.Toast;
import android.app.Notification;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract;
import android.content.IntentFilter;

public class MainActivity extends AppCompatActivity
        //implements NavigationView.OnNavigationItemSelectedListener
{

    TextView txtEscuchar;
    TextToSpeech ttsHabla;
    NotificationManager NmNotification;
    public static int codigo_de_repeticion_de_voz = 1234;
    private static final int codigo_de_notificacion = 1337;
    private final int LONG_DURATION = 5000;
    private final int SHORT_DURATION = 1200;
    private int contador = 0;
    private static SimpleCursorAdapter adaptador;
    private final int CHECK_CODE = 0x1;
    private BroadcastReceiver smsReceiver;
    private SmsMessage message;
    public static final String[] EVENT_PROJECTION = new String[]{

            Calendars._ID,
            Calendars.ACCOUNT_NAME,
            Calendars.CALENDAR_DISPLAY_NAME,
            Calendars.OWNER_ACCOUNT
    };
    public String[] PROJECTION= new String[]{
           CalendarContract.Reminders._ID,
            CalendarContract.Reminders.TITLE,
            CalendarContract.Reminders.DTSTART,
            CalendarContract.Reminders.DTEND
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtEscuchar = (TextView) findViewById(R.id.txtresultado);
        ttsHabla = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    ttsHabla.setLanguage(Locale.getDefault());
                }
            }
        });
        NmNotification = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        initializeSMSReceiver();
        registerSMSReceiver();


    }

    public void onbtnEscucharClick(View v) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Menciona algo");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 4);
        startActivityForResult(intent, codigo_de_repeticion_de_voz);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == codigo_de_repeticion_de_voz && resultCode == RESULT_OK) {
            String Repuesta = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
            txtEscuchar.setText(Repuesta);
            ttsHabla.speak(Repuesta, TextToSpeech.QUEUE_ADD, null);
        }
        if (requestCode == CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
            } else {
                Intent install = new Intent();
                install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(install);
            }
        }

    }

    public void onbtnEnviarClick(View v) {
        try {

            hablar(txtEscuchar.getText().toString());
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void hablar(String texto) {
        ttsHabla.speak(texto, TextToSpeech.QUEUE_ADD, null);
        notificar("ADA", texto, texto);
    }


    public void onbtnpruebaClick(View v) {
        try {
            Cursor cur =null;
            ContentResolver cr=getContentResolver();
            Uri uri=Calendars.CONTENT_URI;
           // CalendarContract.Reminders.
            //notificar("Buenos dias", "Brandon", "Tarea nueva es hora, de hacer algo");

            //ttsHabla.notify();
            /*


            String selection="((" + Calendars.ACCOUNT_NAME + "= ?) AND ("
                    + Calendars.ACCOUNT_TYPE + " = ?) AND (" + Calendars.OWNER_ACCOUNT + "=?))";
            String[] selectionArgs=new  String[]{"cristianrayo7@gmail.com","com.google",
            "cristianrayo7@gmail.com"};
            if (checkPermission("android.permission.READ_CALENDAR", getTaskId() ,android.os.Process.myPid())== PackageManager.PERMISSION_GRANTED){
                cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
            }

           */
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void notificar(String Titulo, String contenido, String ticket) {
        PendingIntent i = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class),
                0);
        Notification.Builder notiguilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.sprite).setTicker(ticket)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(Titulo)
                .setContentText(contenido)
                .setContentIntent(i);
        notiguilder.setDefaults(Notification.DEFAULT_ALL);
        NmNotification.notify(codigo_de_notificacion, notiguilder.getNotification());

    }

    public void tostada(String texto) {
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }

    private void initializeSMSReceiver() {
        smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    for (int i = 0; i < pdus.length; i++) {
                        byte[] pdu = (byte[]) pdus[i];
                        SmsMessage message = SmsMessage.createFromPdu(pdu);
                        String text = message.getDisplayMessageBody();
                        String sender = getContactName(message.getOriginatingAddress());
                        pause(LONG_DURATION);
                        hablar("Tienes un mensaje de" + sender + "!");
                        pause(SHORT_DURATION);
                        hablar(text);
                        //smsSender.setText("Message from " + sender);
                        //smsText.setText(text);
                    }
                }

            }
        };
    }

    private String getContactName(String phone) {
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        String projection[] = new String[]{ContactsContract.Data.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        } else {
            return "Un desconocido";
        }
    }

    private void pause(int duration) {
        ttsHabla.playSilence(duration, TextToSpeech.QUEUE_ADD, null);
    }

    private void registerSMSReceiver() {
        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, intentFilter);
    }
    private void setViewValue(Cursor cursor,int columna){
        long time=0;
        String formattedTime=null;
        time=cursor.getLong(columna);
        formattedTime= DateUtils.formatDateTime(this,time,DateUtils.FORMAT_ABBREV_RELATIVE);
        hablar(formattedTime);
    }



/*
private class CalendarQA extends ListActivity
 implements
    LoaderManager.LoaderCallbacks<Cursor>,
    SimpleCursorAdapter.ViewBinder{
    public  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0,null,this);
        setListAdapter(adaptador);
        daptador.setViewBinder(this);
    }
    public void onLoadFinishesd(Loader<Cursor> loader, Cursor cursor) {
        adaptador.swapCursor(cursor);
    }
    public void onLoaderReset(Loader<Cursor> loader,Cursor cursor){
        adaptador.swapCursor(null);
    }
    public Loader<Cursor> onCreateLoader(int loaderId,Bundle args){
        return (new CursorLoader(this,CalendarContract.Reminders.CONTENT_URI,
                PROJECTION,null,null,
                CalendarContract.Reminders.DTSTART));
    }
}*/
}
