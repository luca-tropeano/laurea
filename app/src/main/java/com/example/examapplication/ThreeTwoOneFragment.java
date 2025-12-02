package com.example.examapplication;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.OutputStream;

public class ThreeTwoOneFragment extends Fragment {

    private EditText answer1EditText, answer2EditText, answer3EditText;
    private Button saveButton;

    // Definisci le domande come stringhe fisse
    private String QUESTION_1, QUESTION_2, QUESTION_3;

    public ThreeTwoOneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inizializza le stringhe con i valori effettivi dal contesto dell'applicazione
        Resources resources = getResources();
        QUESTION_1 = resources.getString(R.string.QUESTION_1);
        QUESTION_2 = resources.getString(R.string.QUESTION_2);
        QUESTION_3 = resources.getString(R.string.QUESTION_3);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_three_two_one, container, false);

        // Inizializza gli elementi del layout
        answer1EditText = view.findViewById(R.id.answer1EditText);
        answer2EditText = view.findViewById(R.id.answer2EditText);
        answer3EditText = view.findViewById(R.id.answer3EditText);

        saveButton = view.findViewById(R.id.saveButton);

        // Imposta il listener del pulsante di salvataggio
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileNameDialog();
            }
        });

        return view;
    }

    private void showFileNameDialog() {
        // Crea un dialog per richiedere il nome del file all'utente
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Nome del file");

        // Inserisci un'EditText nel dialog per consentire all'utente di inserire il nome del file
        final EditText fileNameEditText = new EditText(getActivity());
        builder.setView(fileNameEditText);

        // Aggiungi pulsanti "OK" e "Annulla" al dialog
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fileName = fileNameEditText.getText().toString();
                saveAnswersToFile(fileName);
            }
        });
        builder.setNegativeButton("Annulla", null);

        // Mostra il dialog
        builder.show();
    }

    private void saveAnswersToFile(String fileName) {
        // Ottieni le risposte dalle EditText
        String answer1 = answer1EditText != null ? answer1EditText.getText().toString() : "";
        String answer2 = answer2EditText != null ? answer2EditText.getText().toString() : "";
        String answer3 = answer3EditText != null ? answer3EditText.getText().toString() : "";

        // Ottieni i valori delle stringhe da strings.xml
        String question1 = getString(R.string.QUESTION_1);
        String question2 = getString(R.string.QUESTION_2);
        String question3 = getString(R.string.QUESTION_3);

        // Crea una stringa con le risposte
        String answersText = question1 + "\n\n" + answer1 +
                "\n\n\n" + question2 + "\n\n" + answer2 +
                "\n\n\n" + question3 + "\n\n" + answer3;

        // Crea un oggetto ContentValues per specificare i dettagli del file da creare
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");

        // Ottieni l'URI del file
        Uri contentUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL);

        // Inserisci il file utilizzando MediaStore
        Uri uri = getActivity().getContentResolver().insert(contentUri, contentValues);

        try {
            // Apri uno stream di output per scrivere nel file
            OutputStream outputStream = getActivity().getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                // Scrivi le risposte nel file
                outputStream.write(answersText.getBytes());
                outputStream.close();

                // Notifica all'utente che le risposte sono state salvate
                showMessage("Salvataggio completato", "Le risposte sono state salvate con successo nel file " + fileName);
            }

        } catch (IOException e) {
            e.printStackTrace();
            // Gestisci l'eccezione se si verifica un errore durante il salvataggio
            showMessage("Errore di salvataggio", "Si è verificato un errore durante il salvataggio delle risposte.");
        }
    }

    private void showMessage(String title, String message) {
        // Mostra un dialog con il titolo e il messaggio specificati
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}