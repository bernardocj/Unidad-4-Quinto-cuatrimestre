package mx.edu.utng.wsmovie;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalFloat;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText etName;
    private EditText etSinopsis;
    private EditText etPrice;
    private ToggleButton toggleType;
    private Button btnSave;
    private Button btnList;

    private Movie movie = null;

    final String NAMESPACE =
            "http://ws.utng.edu.mx";
    final SoapSerializationEnvelope envelope =
            new SoapSerializationEnvelope(SoapEnvelope.VER11);
    static String URL =
            "http://172.16.12.53:8080/WSMovie/services/MovieWS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponents();
    }
    private void initComponents(){
        etName = (EditText)findViewById(R.id.tv_name);
        etSinopsis = (EditText)findViewById(R.id.tv_sipnosis);
        etPrice = (EditText)findViewById(R.id.tv_price);
        toggleType = (ToggleButton)findViewById(R.id.toogle_type);
        btnSave = (Button) findViewById(R.id.btn_save);
        btnList = (Button)findViewById(R.id.btn_list);
        btnSave.setOnClickListener(this);
        btnList.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_consume_w, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_settings){
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()== btnSave.getId()){
            try {
                if (getIntent().getExtras().getString("accion")
                        .equals("modificar")) {
                   TaskWSUpdate tarea = new TaskWSUpdate();
                    tarea.execute();
                }

            } catch (Exception e) {
                //Cuando no se haya mandado una accion por defecto es insertar.
                TaskWSInsert tarea = new TaskWSInsert();
                tarea.execute();
            }
        }
        if (btnList.getId() == v.getId()) {
            startActivity(new Intent(MainActivity.this, ListMovies.class));
        }
    }
    private class TaskWSInsert extends
            AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            boolean result = true;
            final String METHOD_NAME = "addMovie";
            final String SOAP_ACTION = NAMESPACE + "/" + METHOD_NAME;

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            movie = new Movie();
            movie.setProperty(0, 0);

          getData();

            PropertyInfo info = new PropertyInfo();
            info.setName("movie");
            info.setValue(movie);
            info.setType(movie.getClass());
            request.addProperty(info);
            envelope.setOutputSoapObject(request);
            envelope.addMapping(NAMESPACE, "Movie", Movie.class);

            /* Para serializar flotantes y otros tipos no cadenas o enteros*/
            MarshalFloat mf = new MarshalFloat();
            mf.register(envelope);

            HttpTransportSE transporte = new HttpTransportSE(URL);
            try {
                transporte.call(SOAP_ACTION, envelope);
                SoapPrimitive response =
                        (SoapPrimitive) envelope.getResponse();
                String res = response.toString();
                if (!res.equals("1")) {
                    result = false;
                }

            } catch (Exception e) {
                Log.e("Error ", e.getMessage());
                result = false;
            }


            return result;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            if(result){
                Toast.makeText(getApplicationContext(),
                        "Registro exitoso.",
                        Toast.LENGTH_SHORT).show();
                cleanBox();

            }else {
                Toast.makeText(getApplicationContext(),
                        "Error al insertar.",
                        Toast.LENGTH_SHORT).show();

            }
        }
    }//
    private void cleanBox(){
        etName.setText("");
        etSinopsis.setText("");
        etPrice.setText("");
        toggleType.getTextOff();

    }
    private class TaskWSUpdate extends
            AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            boolean result = true;

            final String METHOD_NAME = "updateMovie";
            final String SOAP_ACTION = NAMESPACE + "/" + METHOD_NAME;

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            movie = new Movie();
            movie.setProperty(0, getIntent().getExtras().getString("valor0"));
            getData();

            PropertyInfo info = new PropertyInfo();
            info.setName("movie");
            info.setValue(movie);
            info.setType(movie.getClass());

            request.addProperty(info);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);

            envelope.setOutputSoapObject(request);

            envelope.addMapping(NAMESPACE, "Movie", movie.getClass());

            MarshalFloat mf = new MarshalFloat();
            mf.register(envelope);

            HttpTransportSE transporte = new HttpTransportSE(URL);

            try {
                transporte.call(SOAP_ACTION,envelope);
                SoapPrimitive resultado_xml = (SoapPrimitive) envelope.getResponse();
                String res = resultado_xml.toString();

                if (!res.equals("1")) {
                    result = false;
                }

            } catch (HttpResponseException e) {
                Log.e("Error HTTP", e.toString());
            } catch (IOException e) {
                Log.e("Error IO", e.toString());
            } catch (XmlPullParserException e) {
                Log.e("Error XmlPullParser", e.toString());
            }


            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(getApplicationContext(), "Actualizado OK",
                        Toast.LENGTH_SHORT).show();
                        cleanBox();
                startActivity(new Intent(MainActivity.this, MainActivity.class));

            } else {
                Toast.makeText(getApplicationContext(), "Error al actualizar",
                        Toast.LENGTH_SHORT).show();

            }
        }
    }//
    private void getData() {
        movie.setProperty(1, etName.getText().toString());
        movie.setProperty(2, etSinopsis.getText().toString());
        movie.setProperty(3, Float.parseFloat(
                etPrice.getText().toString()));

        if (toggleType.isChecked()) {
                movie.setProperty(4, 2);
        } else {
            movie.setProperty(4, 1);
        }
    }//

    @Override
    protected void onResume() {
        super.onResume();
        Bundle datosRegreso = this.getIntent().getExtras();
        try {
            Log.i("Dato", datosRegreso.getString("valor4"));

            etName.setText(datosRegreso.getString("valor1"));
            etSinopsis.setText(datosRegreso.getString("valor2"));
            etPrice.setText(datosRegreso.getString("valor3"));
            if (datosRegreso.getString("valor4").equals("1")) {
                toggleType.setChecked(false);
            } else {
                toggleType.setChecked(true);
            }
        } catch (Exception e) {
            Log.e("Error al Recargar", e.toString());
        }
    }
}
